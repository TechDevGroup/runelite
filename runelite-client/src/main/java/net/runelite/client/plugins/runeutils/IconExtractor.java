package net.runelite.client.plugins.runeutils;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Extracts item icons from RuneLite and uploads to dev server
 * Follows coding conventions: early returns, single responsibility
 */
@Slf4j
public class IconExtractor
{
	private final ItemManager itemManager;
	private final DevServerClient devClient;
	private final ExecutorService executor;

	public IconExtractor(ItemManager itemManager, DevServerClient devClient)
	{
		this.itemManager = itemManager;
		this.devClient = devClient;
		this.executor = Executors.newSingleThreadExecutor();
	}

	public void syncIcon(int itemId, String itemName)
	{
		if (!devClient.isConnected())
		{
			return;
		}

		executor.submit(() -> extractAndUpload(itemId, itemName));
	}

	public void syncAllIcons(Client client)
	{
		log.info("[IconExtractor] Starting full icon sync");

		executor.submit(() -> {
			int synced = 0;
			int failed = 0;

			for (int itemId = 0; itemId < 30000; itemId++)
			{
				try
				{
					String name = itemManager.getItemComposition(itemId).getName();
					if (name == null || name.equals("null"))
					{
						continue;
					}

					boolean success = extractAndUpload(itemId, name);
					if (success)
					{
						synced++;
					}
					else
					{
						failed++;
					}

					if (synced % 100 == 0)
					{
						log.info("[IconExtractor] Progress: {} synced, {} failed", synced, failed);
					}
				}
				catch (Exception e)
				{
					failed++;
				}
			}

			log.info("[IconExtractor] Sync complete: {} synced, {} failed", synced, failed);
		});
	}

	private boolean extractAndUpload(int itemId, String itemName)
	{
		AsyncBufferedImage asyncImage = itemManager.getImage(itemId);
		if (asyncImage == null)
		{
			return false;
		}

		// Wait for pixel data to load via onLoaded callback
		CompletableFuture<BufferedImage> future = new CompletableFuture<>();
		asyncImage.onLoaded(() -> future.complete(asyncImage));

		try
		{
			BufferedImage image = future.get(5, TimeUnit.SECONDS);
			if (image == null)
			{
				return false;
			}

			String base64 = encodeImage(image);
			if (base64 == null)
			{
				return false;
			}

			uploadIcon(itemId, itemName, base64);
			return true;
		}
		catch (Exception e)
		{
			log.debug("[IconExtractor] Timeout waiting for icon {}: {}", itemId, e.getMessage());
			return false;
		}
	}

	private String encodeImage(BufferedImage image)
	{
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
		{
			ImageIO.write(image, "PNG", baos);
			byte[] bytes = baos.toByteArray();
			return Base64.getEncoder().encodeToString(bytes);
		}
		catch (IOException e)
		{
			log.error("[IconExtractor] Failed to encode image", e);
			return null;
		}
	}

	private void uploadIcon(int itemId, String itemName, String base64)
	{
		JsonObject data = new JsonObject();
		data.addProperty("itemId", itemId);
		data.addProperty("name", itemName);
		data.addProperty("blob", base64);

		devClient.sendMessage("icon_upload", data);
		log.debug("[IconExtractor] Uploaded icon: {} ({})", itemName, itemId);
	}

	public void shutdown()
	{
		executor.shutdown();
	}
}
