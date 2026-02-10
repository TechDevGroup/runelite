package net.runelite.client.plugins.runeutils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.KeyCode;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import java.awt.Point;
import java.awt.event.MouseEvent;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

/**
 * RuneUtils Plugin - Advanced menu manipulation and inventory highlighting
 *
 * Features:
 * - Right-click menu priority modification with modifier keys
 * - Inventory item highlighting with fuzzy matching
 * - Profile-based filter system
 * - Custom on-screen indicators
 */
@PluginDescriptor(
	name = "Rune Utils",
	description = "Advanced menu manipulation and inventory highlighting utilities",
	tags = {"menu", "inventory", "highlight", "utility"}
)
@Singleton
@Slf4j
public class RuneUtilsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private RuneUtilsConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private RuneUtilsPanel panel;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private MouseManager mouseManager;

	private NavigationButton navButton;
	private RuneUtilsOverlay inventoryOverlay;
	private InventoryEdgeOverlay rightEdgeOverlay;
	private final Gson gson = new Gson();
	private final SlotSelectionState slotSelectionState = new SlotSelectionState();

	// Dev server components
	private DevServerClient devClient;
	private HotReloadManager hotReloadManager;
	private GameStateStreamer gameStateStreamer;
	private IconExtractor iconExtractor;
	private JSEngine jsEngine;
	private ArtifactManager artifactManager;
	private final MouseListener mouseListener = new MouseListener()
	{
		@Override
		public MouseEvent mouseClicked(MouseEvent event)
		{
			if (event.getButton() == MouseEvent.BUTTON1)
			{
				if (slotSelectionState.isActive())
				{
					if (inventoryOverlay != null)
					{
						inventoryOverlay.handleClick(event.getX(), event.getY());
					}
					return event;
				}

				handleEdgeTabClick(event);
			}
			return event;
		}

		@Override
		public MouseEvent mousePressed(MouseEvent event)
		{
			return event;
		}

		@Override
		public MouseEvent mouseReleased(MouseEvent event)
		{
			return event;
		}

		@Override
		public MouseEvent mouseEntered(MouseEvent event)
		{
			return event;
		}

		@Override
		public MouseEvent mouseExited(MouseEvent event)
		{
			return event;
		}

		@Override
		public MouseEvent mouseDragged(MouseEvent event)
		{
			return event;
		}

		@Override
		public MouseEvent mouseMoved(MouseEvent event)
		{
			handleEdgeTabHover(event);
			return event;
		}
	};

	@Provides
	RuneUtilsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuneUtilsConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Rune Utils started!");

		// Set up panel references
		panel.setPlugin(this);
		panel.setSaveCallback(this::saveProfiles);

		// Add sidebar panel
		navButton = NavigationButton.builder()
			.tooltip("Rune Utils")
			.icon(ImageUtil.loadImageResource(getClass(), "/net/runelite/client/plugins/config/config_icon.png"))
			.priority(100)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		// Create and register inventory overlay
		inventoryOverlay = new RuneUtilsOverlay(client, this, panel, config, slotSelectionState, itemManager);
		overlayManager.add(inventoryOverlay);


		rightEdgeOverlay = new InventoryEdgeOverlay(client, panel.getProfileStates(), InventoryEdgeOverlay.EdgeSide.RIGHT);
		overlayManager.add(rightEdgeOverlay);

		// Load saved profiles
		loadProfiles();

		// Register mouse listener for slot selection and edge tabs
		mouseManager.registerMouseListener(mouseListener);

		// Initialize dev server integration
		initDevServer();

		panel.log("Plugin started successfully");
	}

	private void initDevServer()
	{
		try
		{
			String devServerUrl = System.getProperty("runeutils.devserver", "ws://localhost:3000/ws");
			devClient = new DevServerClient(devServerUrl);

			// Initialize JS engine
			jsEngine = new JSEngine();
			log.info("[RuneUtils] JS Engine initialized");

			// Initialize artifact manager
			artifactManager = new ArtifactManager(devClient, jsEngine);
			log.info("[RuneUtils] Artifact Manager initialized");

			iconExtractor = new IconExtractor(itemManager, devClient);
			hotReloadManager = new HotReloadManager(devClient, panel, artifactManager);
			gameStateStreamer = new GameStateStreamer(client, itemManager, devClient, iconExtractor);

			devClient.on("command", this::handleDevCommand);
			devClient.on("connected", this::handleDevServerConnected);
			// Note: Profile sync happens via ArtifactManager's profile_update handler
			// which populates the artifact manager's state. We'll implement a
			// change-tracking queue system to sync only when needed.

			devClient.connect();
			hotReloadManager.start();

			log.info("[DevServer] Initializing connection to {}", devServerUrl);
		}
		catch (Exception e)
		{
			log.warn("[DevServer] Failed to connect (dev mode disabled): {}", e.getMessage());
		}
	}

	private void handleDevServerConnected(com.google.gson.JsonObject message)
	{
		log.info("[DevServer] Connected, pushing local profiles to server");

		// Register all locally-loaded profiles with ArtifactManager and push to server.
		// This prevents HotReloadManager from wiping locally-loaded profiles when
		// the server responds with its own set of profiles.
		java.util.List<ProfileState> localProfiles = panel.getProfileStates();
		for (ProfileState profile : localProfiles)
		{
			syncProfileToDevServer(profile);
		}
		log.info("[DevServer] Pushed {} local profiles to server", localProfiles.size());
	}

	private void handleDevCommand(com.google.gson.JsonObject message)
	{
		if (!message.has("data"))
		{
			return;
		}

		com.google.gson.JsonObject data = message.getAsJsonObject("data");
		if (!data.has("action"))
		{
			return;
		}

		String action = data.get("action").getAsString();

		if ("sync_icons".equals(action))
		{
			log.info("[DevServer] Icon sync requested");
			iconExtractor.syncAllIcons(client);
		}
		else if ("snapshot_container".equals(action))
		{
			String containerStr = data.has("containerType") ? data.get("containerType").getAsString() : "INVENTORY";
			ContainerType ct;
			try
			{
				ct = ContainerType.valueOf(containerStr);
			}
			catch (IllegalArgumentException e)
			{
				ct = ContainerType.INVENTORY;
			}
			log.info("[DevServer] Snapshot requested for {}", ct);
			snapshotContainerAsProfile(ct, SnapshotMode.AGNOSTIC_ANY_QTY);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Rune Utils stopped!");

		// Shutdown dev server components
		shutdownDevServer();

		// Remove sidebar panel
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}

		// Remove overlays
		if (inventoryOverlay != null)
		{
			overlayManager.remove(inventoryOverlay);
			inventoryOverlay = null;
		}

		if (rightEdgeOverlay != null)
		{
			overlayManager.remove(rightEdgeOverlay);
			rightEdgeOverlay = null;
		}

		// Unregister mouse listener
		mouseManager.unregisterMouseListener(mouseListener);
	}

	private void shutdownDevServer()
	{
		if (hotReloadManager != null)
		{
			hotReloadManager.stop();
		}

		if (iconExtractor != null)
		{
			iconExtractor.shutdown();
		}

		if (jsEngine != null)
		{
			jsEngine.close();
			log.info("[RuneUtils] JS Engine closed");
		}

		if (devClient != null)
		{
			devClient.close();
		}
	}

	@Subscribe
	public void onPostMenuSort(PostMenuSort event)
	{
		// Menu manipulation logic for shift-click deposit and withdraw
		if (!config.enableMenuManipulation())
		{
			return;
		}

		// Check if shift is pressed
		boolean shiftPressed = client.isKeyPressed(KeyCode.KC_SHIFT);
		if (!shiftPressed)
		{
			return;
		}

		Menu menu = client.getMenu();
		MenuEntry[] menuEntries = menu.getMenuEntries();

		// Find and swap bank deposit/withdraw options
		for (int i = 0; i < menuEntries.length; i++)
		{
			MenuEntry menuEntry = menuEntries[i];
			MenuAction type = menuEntry.getType();

			// Check if this is a bank inventory item (can be CC_OP or CC_OP_LOW_PRIORITY)
			if (type == MenuAction.CC_OP || type == MenuAction.CC_OP_LOW_PRIORITY)
			{
				int widgetGroupId = WidgetUtil.componentToInterface(menuEntry.getParam1());
				boolean isBankInventory = widgetGroupId == InterfaceID.BANKSIDE;
				boolean isDepositBoxInventory = widgetGroupId == InterfaceID.BANK_DEPOSITBOX;

				// Handle deposit options for inventory items
				if (isBankInventory || isDepositBoxInventory)
				{
					// Deposit-op 2 is the default for bank, op 1 for deposit box
					int currentDefaultOp = isDepositBoxInventory ? 1 : 2;

					if (menuEntry.getIdentifier() == currentDefaultOp
						&& (menuEntry.getOption().startsWith("Deposit-") || menuEntry.getOption().startsWith("Store")))
					{
						// Get the configured deposit mode and swap to it
						ShiftDepositMode depositMode = config.shiftDepositPriority();
						if (depositMode != ShiftDepositMode.OFF)
						{
							int targetOpId = isDepositBoxInventory ? depositMode.getIdentifierDepositBox() : depositMode.getIdentifier();
							// Op IDs >= 6 use CC_OP_LOW_PRIORITY instead of CC_OP
							MenuAction targetAction = targetOpId >= 6 ? MenuAction.CC_OP_LOW_PRIORITY : MenuAction.CC_OP;
							swapMenuEntry(menu, menuEntries, i, targetOpId, targetAction);
						}
						break;
					}
				}
			}
		}

		// Handle withdraw options for bank items - check for CC_OP with withdraw option
		for (int i = 0; i < menuEntries.length; i++)
		{
			MenuEntry menuEntry = menuEntries[i];

			if (menuEntry.getType() == MenuAction.CC_OP
				&& menuEntry.getIdentifier() == 1
				&& menuEntry.getOption().startsWith("Withdraw-"))
			{
				// Get the configured withdraw mode and swap to it
				ShiftWithdrawMode withdrawMode = config.shiftWithdrawPriority();
				if (withdrawMode != ShiftWithdrawMode.OFF)
				{
					int targetOpId = withdrawMode.getIdentifier();
					MenuAction targetAction = withdrawMode.getMenuAction();
					swapMenuEntry(menu, menuEntries, i, targetOpId, targetAction);
				}
				break;
			}
		}
	}

	private void swapMenuEntry(Menu menu, MenuEntry[] menuEntries, int currentIndex, int targetIdentifier, MenuAction targetAction)
	{
		// Find the menu entry that matches our target identifier and action type
		for (int i = 0; i < menuEntries.length; i++)
		{
			MenuEntry entry = menuEntries[i];

			if (entry.getType() == targetAction && entry.getIdentifier() == targetIdentifier)
			{
				// Swap the entries
				if (i != currentIndex)
				{
					// Swap in the array
					MenuEntry temp = menuEntries[currentIndex];
					menuEntries[currentIndex] = menuEntries[i];
					menuEntries[i] = temp;

					// Update the menu
					menu.setMenuEntries(menuEntries);
				}
				break;
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (inventoryOverlay != null)
		{
			inventoryOverlay.onInventoryChanged();
		}

		if (gameStateStreamer != null)
		{
			gameStateStreamer.streamState();
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		if (!config.enableSyntheticMenus())
		{
			return;
		}

		MenuEntry[] entries = event.getMenuEntries();

		// Check if this is an item menu (inventory or bank)
		// Look for "Drop", "Use" (inventory), or "Withdraw" (bank) options
		for (MenuEntry entry : entries)
		{
			String option = entry.getOption();

			// Check if this is an item (inventory has Drop/Use, bank has Withdraw-)
			if ((option.equals("Drop") || option.equals("Use") || option.startsWith("Withdraw-"))
				&& !entry.getTarget().isEmpty())
			{
				// Add "Add to Profile" options for each existing compatible profile
				java.util.List<ProfileState> compatibleProfiles = getCompatibleProfiles(entry);
				for (ProfileState profile : compatibleProfiles)
				{
					client.createMenuEntry(0)
						.setOption("Add to: " + profile.getName())
						.setTarget(entry.getTarget())
						.setParam0(entry.getParam0())
						.setParam1(entry.getParam1())
						.setType(MenuAction.RUNELITE)
						.onClick(e -> handleAddToProfile(e, profile));
				}

				// Create a new menu entry for "Create Profile" at the bottom (index 0)
				// Using index 0 makes it appear last in the menu (menus are built bottom-up)
				client.createMenuEntry(0)
					.setOption("Create Profile")
					.setTarget(entry.getTarget())
					.setParam0(entry.getParam0())
					.setParam1(entry.getParam1())
					.setType(MenuAction.RUNELITE)
					.onClick(this::handleCreateProfile);

				// Snapshot Inventory menu entry
				client.createMenuEntry(0)
					.setOption("Snapshot Inventory")
					.setTarget("")
					.setType(MenuAction.RUNELITE)
					.onClick(e -> snapshotContainerAsProfile(ContainerType.INVENTORY, SnapshotMode.AGNOSTIC_ANY_QTY));

				// Only add once per menu
				break;
			}
		}
	}

	private void handleCreateProfile(MenuEntry entry)
	{
		// Determine which container type based on the menu option
		ContainerType containerType = null;
		ItemContainer container = null;

		String option = entry.getOption();
		log.info("handleCreateProfile called with option: {}", option);

		// Check if it's an inventory item (has Drop or Use in the original menu)
		if (option.equals("Create Profile"))
		{
			// Need to determine from widget or other menu entries
			// For now, check both inventory and bank
			ItemContainer inv = client.getItemContainer(InventoryID.INVENTORY);
			ItemContainer bank = client.getItemContainer(InventoryID.BANK);

			int slot = entry.getParam0();

			// Try inventory first
			if (inv != null && slot >= 0 && slot < inv.size())
			{
				Item item = inv.getItems()[slot];
				if (item != null && item.getId() != -1)
				{
					containerType = ContainerType.INVENTORY;
					container = inv;
				}
			}

			// Try bank if inventory didn't match
			if (containerType == null && bank != null && slot >= 0 && slot < bank.size())
			{
				Item item = bank.getItems()[slot];
				if (item != null && item.getId() != -1)
				{
					containerType = ContainerType.BANK;
					container = bank;
				}
			}
		}

		if (containerType == null || container == null)
		{
			log.warn("Could not determine container type for profile creation");
			return;
		}

		int slot = entry.getParam0();
		if (slot < 0 || slot >= container.size())
		{
			return;
		}

		Item item = container.getItems()[slot];
		if (item == null || item.getId() == -1)
		{
			return;
		}

		// Get item name
		String itemName = itemManager.getItemComposition(item.getId()).getName();
		if (itemName == null)
		{
			itemName = "Unknown Item";
		}

		// Create profile for this specific container with only the clicked item
		ProfileState profile = new ProfileState(itemName + " Profile", containerType);

		// Create empty snapshot and add only the clicked item
		ContainerSnapshot snapshot = new ContainerSnapshot(containerType);
		TrackedItemState itemState = new TrackedItemState(item.getId(), itemName);
		itemState.setQuantity(item.getQuantity());
		itemState.setQuantityMax(item.getQuantity());
		itemState.setQuantityCondition(QuantityCondition.ANY);

		// Add item without position requirement (position-agnostic)
		snapshot.addItemState(itemState);
		profile.setSnapshot(snapshot);

		// Add to panel and persist
		panel.addProfileState(profile);
		saveProfiles();
		syncProfileToDevServer(profile);
		panel.log("Created " + containerType.getDisplayName() + " profile: " + profile.getName());
	}

	/**
	 * Snapshot an entire container as a new profile
	 */
	public void snapshotContainerAsProfile(ContainerType containerType, SnapshotMode mode)
	{
		net.runelite.api.ItemContainer container = client.getItemContainer(containerType.getInventoryId());
		if (container == null)
		{
			log.warn("[Snapshot] Container {} not available", containerType);
			return;
		}

		java.util.function.Function<Integer, String> nameLookup = id -> {
			var comp = itemManager.getItemComposition(id);
			return comp != null ? comp.getName() : "Item " + id;
		};

		ContainerSnapshot snapshot = ContainerSnapshot.captureFromContainer(
			containerType, container, nameLookup, mode
		);

		String timestamp = String.valueOf(System.currentTimeMillis() % 100000);
		ProfileState profile = new ProfileState(
			containerType.getDisplayName() + " Snapshot " + timestamp,
			containerType
		);
		profile.setSnapshot(snapshot);

		panel.addProfileState(profile);
		saveProfiles();
		syncProfileToDevServer(profile);
		panel.log("Snapshot " + containerType.getDisplayName() + ": " + snapshot.getItemCount() + " items (" + mode.getDisplayName() + ")");
		log.info("[Snapshot] Captured {} items from {} using mode {}", snapshot.getItemCount(), containerType, mode);
	}

	/**
	 * Get profiles compatible with the menu entry's container type
	 */
	private java.util.List<ProfileState> getCompatibleProfiles(MenuEntry entry)
	{
		ContainerType containerType = determineContainerType(entry);
		if (containerType == null)
		{
			return java.util.Collections.emptyList();
		}

		return panel.getProfileStates().stream()
			.filter(p -> p.getContainerType() == containerType)
			.collect(java.util.stream.Collectors.toList());
	}

	/**
	 * Determine container type from menu entry
	 */
	private ContainerType determineContainerType(MenuEntry entry)
	{
		ItemContainer inv = client.getItemContainer(InventoryID.INVENTORY);
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);

		int slot = entry.getParam0();

		// Try inventory first
		if (inv != null && slot >= 0 && slot < inv.size())
		{
			Item item = inv.getItems()[slot];
			if (item != null && item.getId() != -1)
			{
				return ContainerType.INVENTORY;
			}
		}

		// Try bank if inventory didn't match
		if (bank != null && slot >= 0 && slot < bank.size())
		{
			Item item = bank.getItems()[slot];
			if (item != null && item.getId() != -1)
			{
				return ContainerType.BANK;
			}
		}

		return null;
	}

	/**
	 * Handle adding item to an existing profile
	 */
	private void handleAddToProfile(MenuEntry entry, ProfileState profile)
	{
		ContainerType containerType = profile.getContainerType();
		ItemContainer container = client.getItemContainer(containerType.getInventoryId());

		if (container == null)
		{
			log.warn("Container not available for profile");
			return;
		}

		int slot = entry.getParam0();
		if (slot < 0 || slot >= container.size())
		{
			return;
		}

		Item item = container.getItems()[slot];
		if (item == null || item.getId() == -1)
		{
			return;
		}

		// Get item name
		String itemName = itemManager.getItemComposition(item.getId()).getName();
		if (itemName == null)
		{
			itemName = "Unknown Item";
		}

		// Add item to existing profile's snapshot
		ContainerSnapshot snapshot = profile.getSnapshot();
		if (snapshot == null)
		{
			snapshot = new ContainerSnapshot(containerType);
			profile.setSnapshot(snapshot);
		}

		TrackedItemState itemState = new TrackedItemState(item.getId(), itemName);
		itemState.setQuantity(item.getQuantity());
		itemState.setQuantityMax(item.getQuantity());
		itemState.setQuantityCondition(QuantityCondition.ANY);

		// Add item without position requirement (position-agnostic)
		snapshot.addItemState(itemState);

		// Refresh UI, persist, and sync to dev server
		panel.rebuild();
		saveProfiles();
		syncProfileToDevServer(profile);
		panel.log("Added " + itemName + " to profile: " + profile.getName());
	}

	/**
	 * Get item name from item ID using ItemManager
	 */
	private Function<Integer, String> getItemNameLookup()
	{
		return (itemId) -> {
			try {
				return itemManager.getItemComposition(itemId).getName();
			} catch (Exception e) {
				return "Unknown";
			}
		};
	}

	/**
	 * Get ItemContainer by ContainerType
	 */
	private Function<ContainerType, ItemContainer> getContainerProvider()
	{
		return (type) -> {
			if (type.isComposite()) {
				return null; // Handle in Phase 3
			}
			return client.getItemContainer(type.getInventoryId());
		};
	}

	/**
	 * Get the slot selection state manager
	 */
	public SlotSelectionState getSlotSelectionState()
	{
		return slotSelectionState;
	}

	/**
	 * Get the artifact manager for accessing dev server data
	 */
	public ArtifactManager getArtifactManager()
	{
		return artifactManager;
	}

	/**
	 * Handle click on edge tab overlay
	 */
	private void handleEdgeTabClick(MouseEvent event)
	{
		if (rightEdgeOverlay == null)
		{
			return;
		}

		Point clickPoint = event.getPoint();

		Integer rightIndex = rightEdgeOverlay.getProfileIndexAt(clickPoint);
		if (rightIndex != null)
		{
			ProfileState profile = rightEdgeOverlay.getProfileAt(rightIndex);
			if (profile != null)
			{
				profile.setEnabled(!profile.isEnabled());
				panel.rebuild();
				saveProfiles();
				syncProfileToDevServer(profile);
			}
		}
	}

	/**
	 * Handle hover on edge tab overlay for preview
	 */
	private void handleEdgeTabHover(MouseEvent event)
	{
		if (rightEdgeOverlay == null || inventoryOverlay == null)
		{
			return;
		}

		Point hoverPoint = event.getPoint();

		Integer rightIndex = rightEdgeOverlay.getProfileIndexAt(hoverPoint);
		if (rightIndex != null)
		{
			rightEdgeOverlay.setHoveredProfileIndex(rightIndex);
			ProfileState hoveredProfile = rightEdgeOverlay.getHoveredProfile();
			inventoryOverlay.setHoveredProfile(hoveredProfile);
			return;
		}

		rightEdgeOverlay.setHoveredProfileIndex(null);
		inventoryOverlay.setHoveredProfile(null);
	}

	/**
	 * Push profile update to dev server so all clients sync in real time.
	 * Also registers the profile with ArtifactManager so it isn't wiped
	 * when the next external update triggers a panel sync.
	 */
	private void syncProfileToDevServer(ProfileState profile)
	{
		// Keep ArtifactManager aware of locally-modified profiles
		if (artifactManager != null)
		{
			artifactManager.updateLocalProfile(profile);
		}

		if (devClient == null || !devClient.isConnected())
		{
			return;
		}

		com.google.gson.JsonObject profileData = gson.toJsonTree(profile).getAsJsonObject();
		devClient.sendMessage("profile_update", profileData);
		log.debug("[DevServer] Synced profile: {}", profile.getName());
	}

	/**
	 * Save profiles to config
	 */
	public void saveProfiles()
	{
		try
		{
			java.util.List<ProfileState> profiles = panel.getProfileStates();
			String json = gson.toJson(profiles);
			configManager.setConfiguration("runeutils", "profilesData", json);
			log.debug("Saved {} profiles", profiles.size());
		}
		catch (Exception e)
		{
			log.error("Failed to save profiles", e);
		}
	}

	/**
	 * Load profiles from config
	 */
	private void loadProfiles()
	{
		try
		{
			String json = configManager.getConfiguration("runeutils", "profilesData");
			if (json != null && !json.isEmpty() && !json.equals("[]"))
			{
				Type listType = new TypeToken<ArrayList<ProfileState>>(){}.getType();
				java.util.List<ProfileState> profiles = gson.fromJson(json, listType);
				if (profiles != null)
				{
					for (ProfileState profile : profiles)
					{
						panel.addProfileState(profile);
					}
					log.info("Loaded {} profiles", profiles.size());
				}
			}
		}
		catch (Exception e)
		{
			log.error("Failed to load profiles", e);
		}
	}
}
