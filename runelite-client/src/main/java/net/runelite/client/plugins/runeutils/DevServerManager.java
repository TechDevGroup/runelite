package net.runelite.client.plugins.runeutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;

@Slf4j
public class DevServerManager
{
	private static final File DEV_SERVER_DIR = new File(RuneLite.RUNELITE_DIR, "runeutils/dev-server");
	private static final File VERSION_FILE = new File(DEV_SERVER_DIR, ".bundled-version");
	private static final String RESOURCE_PREFIX = "dev-server/";

	private final int port;
	private final String host;

	private Process serverProcess;
	private boolean processOwnedByUs = false;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
		r -> {
			Thread t = new Thread(r, "DevServerManager");
			t.setDaemon(true);
			return t;
		}
	);

	private volatile boolean running = false;
	private volatile boolean shutdownRequested = false;

	public DevServerManager(int port)
	{
		this.port = port;
		this.host = "localhost";
	}

	/**
	 * Start the dev server asynchronously.
	 * Returns a CompletableFuture that completes when server is healthy.
	 */
	public CompletableFuture<Boolean> start()
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				// Idempotent: skip launch if already running
				if (isHealthy())
				{
					log.info("[DevServerManager] Port {} already in use with healthy server, skipping launch", port);
					running = true;
					return true;
				}

				// Extract bundled resources if needed
				extractIfNeeded();

				// npm install if node_modules missing
				npmInstallIfNeeded();

				// Launch node process
				launchProcess();

				// Wait for health check
				boolean healthy = waitForHealth(30_000, 500);
				if (!healthy)
				{
					log.error("[DevServerManager] Server failed health check after 30s");
					stop();
					return false;
				}

				processOwnedByUs = true;
				running = true;
				log.info("[DevServerManager] Server is healthy on port {}", port);
				return true;
			}
			catch (Exception e)
			{
				log.error("[DevServerManager] Failed to start dev server", e);
				return false;
			}
		}, executor);
	}

	/**
	 * Stop the server process.
	 */
	public void stop()
	{
		shutdownRequested = true;
		running = false;

		if (processOwnedByUs && serverProcess != null && serverProcess.isAlive())
		{
			log.info("[DevServerManager] Stopping dev server process");
			serverProcess.destroy();

			try
			{
				if (!serverProcess.waitFor(5, TimeUnit.SECONDS))
				{
					log.warn("[DevServerManager] Force-killing dev server process");
					serverProcess.destroyForcibly();
				}
			}
			catch (InterruptedException e)
			{
				serverProcess.destroyForcibly();
				Thread.currentThread().interrupt();
			}
		}

		executor.shutdownNow();
		log.info("[DevServerManager] Stopped");
	}

	public boolean isRunning()
	{
		return running;
	}

	/**
	 * Check if the dev server health endpoint is responding.
	 */
	private boolean isHealthy()
	{
		try
		{
			HttpURLConnection conn = (HttpURLConnection)
				new URL("http://" + host + ":" + port + "/health").openConnection();
			conn.setConnectTimeout(2000);
			conn.setReadTimeout(2000);
			conn.setRequestMethod("GET");
			int code = conn.getResponseCode();
			conn.disconnect();
			return code == 200;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Extract bundled dev-server resources to disk if version changed or missing.
	 */
	private void extractIfNeeded() throws IOException
	{
		String bundledVersion = getBundledVersion();
		String extractedVersion = getExtractedVersion();

		if (bundledVersion.equals(extractedVersion) && new File(DEV_SERVER_DIR, "src/server.js").exists())
		{
			log.info("[DevServerManager] Dev server files up-to-date (version {})", extractedVersion);
			return;
		}

		log.info("[DevServerManager] Extracting dev server (bundled={}, extracted={})", bundledVersion, extractedVersion);

		// Clean existing extraction but preserve node_modules and data
		if (DEV_SERVER_DIR.exists())
		{
			cleanDirectoryPreserving(DEV_SERVER_DIR, "node_modules", "data");
		}

		DEV_SERVER_DIR.mkdirs();

		// Extract from JAR resources using manifest
		extractResourceTree(RESOURCE_PREFIX, DEV_SERVER_DIR);

		// Write version marker
		Files.writeString(VERSION_FILE.toPath(), bundledVersion);

		log.info("[DevServerManager] Extraction complete");
	}

	/**
	 * Extract all resources listed in the manifest file.
	 */
	private void extractResourceTree(String prefix, File targetDir) throws IOException
	{
		try (InputStream manifestStream = getClass().getClassLoader()
			.getResourceAsStream(prefix + ".resource-manifest"))
		{
			if (manifestStream == null)
			{
				throw new IOException("Resource manifest not found at " + prefix + ".resource-manifest");
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(manifestStream));
			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.isEmpty())
				{
					continue;
				}

				String resourcePath = prefix + line;
				File targetFile = new File(targetDir, line);
				targetFile.getParentFile().mkdirs();

				try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath))
				{
					if (in != null)
					{
						Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
					else
					{
						log.warn("[DevServerManager] Resource not found: {}", resourcePath);
					}
				}
			}
		}
	}

	/**
	 * Run npm install if node_modules doesn't exist.
	 */
	private void npmInstallIfNeeded() throws IOException, InterruptedException
	{
		File nodeModules = new File(DEV_SERVER_DIR, "node_modules");
		if (nodeModules.exists() && nodeModules.isDirectory())
		{
			log.info("[DevServerManager] node_modules exists, skipping npm install");
			return;
		}

		log.info("[DevServerManager] Running npm install (first run setup)...");

		ProcessBuilder pb = new ProcessBuilder(getNpmCommand(), "install", "--production")
			.directory(DEV_SERVER_DIR)
			.redirectErrorStream(true);

		Process npm = pb.start();

		// Log npm output
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(npm.getInputStream())))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				log.info("[npm] {}", line);
			}
		}

		int exitCode = npm.waitFor();
		if (exitCode != 0)
		{
			throw new IOException("npm install failed with exit code " + exitCode);
		}

		log.info("[DevServerManager] npm install complete");
	}

	/**
	 * Launch the Node.js server process.
	 */
	private void launchProcess() throws IOException
	{
		File serverJs = new File(DEV_SERVER_DIR, "src/server.js");
		if (!serverJs.exists())
		{
			throw new IOException("server.js not found at " + serverJs.getAbsolutePath());
		}

		String nodeCommand = getNodeCommand();

		ProcessBuilder pb = new ProcessBuilder(nodeCommand, serverJs.getAbsolutePath())
			.directory(DEV_SERVER_DIR)
			.redirectErrorStream(true);

		pb.environment().put("PORT", String.valueOf(port));

		serverProcess = pb.start();

		// Forward stdout/stderr to RuneLite log on a daemon thread
		Thread logThread = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(serverProcess.getInputStream())))
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					log.info("[dev-server] {}", line);
				}
			}
			catch (IOException e)
			{
				if (!shutdownRequested)
				{
					log.error("[DevServerManager] Error reading server output", e);
				}
			}
		}, "DevServer-Log");
		logThread.setDaemon(true);
		logThread.start();

		log.info("[DevServerManager] Launched node process (PID: {})", serverProcess.pid());
	}

	/**
	 * Poll the health endpoint until it responds or timeout.
	 */
	private boolean waitForHealth(long timeoutMs, long intervalMs)
	{
		long deadline = System.currentTimeMillis() + timeoutMs;

		while (System.currentTimeMillis() < deadline && !shutdownRequested)
		{
			if (isHealthy())
			{
				return true;
			}

			// Check if process died
			if (serverProcess != null && !serverProcess.isAlive())
			{
				log.error("[DevServerManager] Server process exited with code {}", serverProcess.exitValue());
				return false;
			}

			try
			{
				Thread.sleep(intervalMs);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return false;
			}
		}

		return false;
	}

	private String getBundledVersion()
	{
		try (InputStream is = getClass().getClassLoader()
			.getResourceAsStream(RESOURCE_PREFIX + "package.json"))
		{
			if (is == null)
			{
				return "unknown";
			}
			String json = new String(is.readAllBytes());
			int idx = json.indexOf("\"version\"");
			if (idx == -1)
			{
				return "unknown";
			}
			int start = json.indexOf("\"", idx + 9) + 1;
			int end = json.indexOf("\"", start);
			return json.substring(start, end);
		}
		catch (Exception e)
		{
			return "unknown";
		}
	}

	private String getExtractedVersion()
	{
		try
		{
			return Files.readString(VERSION_FILE.toPath()).trim();
		}
		catch (Exception e)
		{
			return "";
		}
	}

	private void cleanDirectoryPreserving(File dir, String... preserve)
	{
		java.util.Set<String> preserveSet = java.util.Set.of(preserve);

		File[] children = dir.listFiles();
		if (children == null)
		{
			return;
		}

		for (File child : children)
		{
			if (preserveSet.contains(child.getName()))
			{
				continue;
			}
			deleteRecursive(child);
		}
	}

	private void deleteRecursive(File file)
	{
		if (file.isDirectory())
		{
			File[] children = file.listFiles();
			if (children != null)
			{
				for (File child : children)
				{
					deleteRecursive(child);
				}
			}
		}
		file.delete();
	}

	private String getNpmCommand()
	{
		return System.getProperty("os.name").toLowerCase().contains("win") ? "npm.cmd" : "npm";
	}

	private String getNodeCommand()
	{
		return "node";
	}
}
