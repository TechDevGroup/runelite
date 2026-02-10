package net.runelite.client.plugins.runeutils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages artifacts from dev server as source of truth
 * Replaces hardcoded logic with artifact-driven implementation
 * Follows coding conventions: O(1) lookups, early returns, single responsibility
 */
@Slf4j
public class ArtifactManager
{
	private final DevServerClient devClient;
	private final JSEngine jsEngine;
	private final Gson gson = new Gson();

	// O(1) artifact lookups
	private final Map<String, ProfileState> profilesById = new ConcurrentHashMap<>();
	private final Map<String, ProfileState> profilesByName = new ConcurrentHashMap<>();
	private final Map<String, String> jsModules = new ConcurrentHashMap<>();
	private volatile String profilesChecksum;

	public ArtifactManager(DevServerClient devClient, JSEngine jsEngine)
	{
		this.devClient = devClient;
		this.jsEngine = jsEngine;
		registerHandlers();
		log.info("[ArtifactManager] Initialized");
	}

	private void registerHandlers()
	{
		devClient.on("artifact_updated", this::handleArtifactUpdate);
		devClient.on("artifact_deleted", this::handleArtifactDelete);
		devClient.on("module_update", this::handleModuleUpdate);
		devClient.on("profile_update", this::handleProfileUpdateMessage);
		devClient.on("profile_deleted", this::handleProfileDeleted);
		devClient.on("profiles_checksum", this::handleProfilesChecksum);
		devClient.on("connected", this::handleConnected);
	}

	private void handleConnected(JsonObject message)
	{
		log.info("[ArtifactManager] Dev server connected, syncing profiles");
		JsonObject request = new JsonObject();
		request.addProperty("action", "get_profiles");
		// Include checksum so server can skip if we're already up to date
		if (profilesChecksum != null)
		{
			request.addProperty("checksum", profilesChecksum);
		}
		devClient.sendMessage("command", request);
	}

	private void handleProfilesChecksum(JsonObject message)
	{
		if (message.has("checksum"))
		{
			profilesChecksum = message.get("checksum").getAsString();
			log.info("[ArtifactManager] Profiles checksum: {}", profilesChecksum);
		}
	}

	private void handleProfileDeleted(JsonObject message)
	{
		if (!message.has("data"))
		{
			return;
		}

		JsonObject data = message.getAsJsonObject("data");
		if (!data.has("id"))
		{
			return;
		}

		String id = data.get("id").getAsString();
		ProfileState profile = profilesById.remove(id);
		if (profile != null)
		{
			profilesByName.remove(profile.getName());
			log.info("[ArtifactManager] Profile deleted via server: {} (ID: {})", profile.getName(), id);
		}
	}

	private void handleProfileUpdateMessage(JsonObject message)
	{
		if (!message.has("data"))
		{
			return;
		}

		JsonObject profileData = message.getAsJsonObject("data");
		ProfileState profile = gson.fromJson(profileData, ProfileState.class);

		if (profile == null || profile.getName() == null)
		{
			log.warn("[ArtifactManager] Failed to parse profile from update");
			return;
		}

		// Use ID as primary key, fallback to name for legacy profiles
		String profileId = profile.getId() != null ? profile.getId() : profile.getName();
		profilesById.put(profileId, profile);
		profilesByName.put(profile.getName(), profile);

		log.info("[ArtifactManager] Profile updated: {} (ID: {}, enabled: {})", profile.getName(), profileId, profile.isEnabled());
	}

	/**
	 * Sync all artifacts from dev server
	 */
	public void syncAll()
	{
		if (!devClient.isConnected())
		{
			log.warn("[ArtifactManager] Cannot sync - not connected");
			return;
		}

		JsonObject request = new JsonObject();
		request.addProperty("action", "get_all_artifacts");
		devClient.sendMessage("command", request);

		log.info("[ArtifactManager] Requested artifact sync");
	}

	/**
	 * Handle artifact update from dev server
	 */
	private void handleArtifactUpdate(JsonObject message)
	{
		if (!message.has("data"))
		{
			return;
		}

		JsonObject data = message.getAsJsonObject("data");

		if (!data.has("type"))
		{
			return;
		}

		String type = data.get("type").getAsString();

		switch (type)
		{
			case "profile":
				handleProfileArtifact(data);
				break;
			case "js_module":
				handleJSModuleArtifact(data);
				break;
			case "config":
				handleConfigArtifact(data);
				break;
			default:
				log.debug("[ArtifactManager] Unknown artifact type: {}", type);
		}
	}

	/**
	 * Handle artifact deletion
	 */
	private void handleArtifactDelete(JsonObject message)
	{
		if (!message.has("data"))
		{
			return;
		}

		JsonObject data = message.getAsJsonObject("data");

		if (!data.has("id"))
		{
			return;
		}

		String id = data.get("id").getAsString();

		// Check if profile
		ProfileState profile = profilesById.remove(id);
		if (profile != null)
		{
			profilesByName.remove(profile.getName());
			log.info("[ArtifactManager] Profile deleted: {}", profile.getName());
		}

		// Check if JS module
		String module = jsModules.remove(id);
		if (module != null)
		{
			log.info("[ArtifactManager] JS module deleted: {}", id);
		}
	}

	/**
	 * Handle profile artifact update
	 */
	private void handleProfileArtifact(JsonObject data)
	{
		try
		{
			ProfileState profile = gson.fromJson(data, ProfileState.class);

			if (profile == null)
			{
				return;
			}

			// Use ID as primary key, fallback to name for legacy profiles
			String profileId = profile.getId() != null ? profile.getId() : profile.getName();

			// O(1) updates
			profilesById.put(profileId, profile);
			profilesByName.put(profile.getName(), profile);

			log.info("[ArtifactManager] Profile updated: {} (ID: {})", profile.getName(), profileId);
		}
		catch (Exception e)
		{
			log.error("[ArtifactManager] Failed to parse profile artifact", e);
		}
	}

	/**
	 * Handle JavaScript module artifact
	 */
	private void handleJSModuleArtifact(JsonObject data)
	{
		if (!data.has("id") || !data.has("code"))
		{
			return;
		}

		String id = data.get("id").getAsString();
		String code = data.get("code").getAsString();

		jsModules.put(id, code);
		jsEngine.loadModule(id, code);

		log.info("[ArtifactManager] JS module loaded: {}", id);
	}

	/**
	 * Handle module update specifically
	 */
	private void handleModuleUpdate(JsonObject message)
	{
		if (!message.has("data"))
		{
			return;
		}

		JsonObject data = message.getAsJsonObject("data");
		handleJSModuleArtifact(data);
	}

	/**
	 * Handle config artifact
	 */
	private void handleConfigArtifact(JsonObject data)
	{
		if (!data.has("config"))
		{
			return;
		}

		String configJson = data.get("config").toString();
		jsEngine.setGlobal("config", jsEngine.fromJSON(configJson));

		log.info("[ArtifactManager] Config updated");
	}

	/**
	 * Register a locally-created or modified profile so it isn't wiped
	 * when the next server sync replaces the panel list.
	 */
	public void updateLocalProfile(ProfileState profile)
	{
		if (profile == null || profile.getName() == null)
		{
			return;
		}

		String profileId = profile.getId() != null ? profile.getId() : profile.getName();
		profilesById.put(profileId, profile);
		profilesByName.put(profile.getName(), profile);
		log.debug("[ArtifactManager] Local profile registered: {} (ID: {})", profile.getName(), profileId);
	}

	/**
	 * Get profile by name (O(1))
	 */
	public ProfileState getProfile(String name)
	{
		return profilesByName.get(name);
	}

	/**
	 * Get all profiles, deduplicated by name.
	 * Uses profilesByName to avoid duplicates from id/name key overlap in profilesById.
	 */
	public List<ProfileState> getAllProfiles()
	{
		return List.copyOf(profilesByName.values());
	}

	/**
	 * Execute JS function with profile context
	 * @param functionName Function to call
	 * @param profile Profile data
	 * @param gameState Current game state
	 * @return Result from JS execution
	 */
	public Object executeProfileLogic(String functionName, ProfileState profile, Object gameState)
	{
		if (functionName == null || profile == null)
		{
			return null;
		}

		// Set context
		jsEngine.setGlobal("profile", jsEngine.toJSON(profile));
		jsEngine.setGlobal("gameState", jsEngine.toJSON(gameState));

		// Call function
		return jsEngine.call(functionName);
	}

	/**
	 * Execute validation logic for profile
	 * @param profile Profile to validate
	 * @param gameState Current game state
	 * @return Validation result (Map with matched/mismatched slots)
	 */
	public Map<String, Object> validateProfile(ProfileState profile, Object gameState)
	{
		Object result = executeProfileLogic("validateProfile", profile, gameState);

		if (result instanceof Map)
		{
			return (Map<String, Object>) result;
		}

		return Map.of();
	}

	/**
	 * Check if artifact system is ready
	 */
	public boolean isReady()
	{
		return devClient.isConnected() && !profilesById.isEmpty();
	}

	/**
	 * Get profile count
	 */
	public int getProfileCount()
	{
		return profilesById.size();
	}

	/**
	 * Get loaded JS module count
	 */
	public int getModuleCount()
	{
		return jsModules.size();
	}
}
