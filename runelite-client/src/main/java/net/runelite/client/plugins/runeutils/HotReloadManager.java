package net.runelite.client.plugins.runeutils;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Manages hot-reload of profiles without plugin restart.
 * Syncs panel state from ArtifactManager (single source of truth).
 */
@Slf4j
public class HotReloadManager
{
	private final DevServerClient devClient;
	private final RuneUtilsPanel panel;
	private final ArtifactManager artifactManager;
	private boolean enabled = false;

	public HotReloadManager(DevServerClient devClient, RuneUtilsPanel panel, ArtifactManager artifactManager)
	{
		this.devClient = devClient;
		this.panel = panel;
		this.artifactManager = artifactManager;
	}

	public void start()
	{
		if (enabled)
		{
			return;
		}

		enabled = true;
		registerHandlers();
		log.info("[HotReload] Manager started");
	}

	public void stop()
	{
		if (!enabled)
		{
			return;
		}

		enabled = false;
		log.info("[HotReload] Manager stopped");
	}

	private void registerHandlers()
	{
		devClient.on("profile_update", this::handleProfileUpdate);
		devClient.on("profile_deleted", this::handleProfileDelete);
		devClient.on("config_update", this::handleConfigUpdate);
	}

	private void handleProfileUpdate(JsonObject message)
	{
		// ArtifactManager already parsed and stored this profile by ID.
		// Just sync panel from ArtifactManager's deduplicated state.
		syncPanelFromArtifactManager();
	}

	private void handleProfileDelete(JsonObject message)
	{
		// ArtifactManager handles deletion. Sync panel.
		syncPanelFromArtifactManager();
	}

	private void handleConfigUpdate(JsonObject message)
	{
		log.info("[HotReload] Config update received");
		syncPanelFromArtifactManager();
	}

	private void syncPanelFromArtifactManager()
	{
		if (artifactManager == null)
		{
			return;
		}

		List<ProfileState> serverProfiles = artifactManager.getAllProfiles();

		// Dispatch list mutation + rebuild to Swing EDT
		SwingUtilities.invokeLater(() ->
		{
			List<ProfileState> panelProfiles = panel.getProfileStates();
			panelProfiles.clear();
			panelProfiles.addAll(serverProfiles);
			panel.rebuild();
			log.debug("[HotReload] Panel synced: {} profiles from ArtifactManager", serverProfiles.size());
		});
	}
}
