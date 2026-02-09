package net.runelite.client.plugins.runeutils;

import com.google.inject.Provides;
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

	private NavigationButton navButton;
	private RuneUtilsOverlay inventoryOverlay;

	@Provides
	RuneUtilsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuneUtilsConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Rune Utils started!");

		// Add sidebar panel
		navButton = NavigationButton.builder()
			.tooltip("Rune Utils")
			.icon(ImageUtil.loadImageResource(getClass(), "/net/runelite/client/plugins/config/config_icon.png"))
			.priority(100)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		// Create and register inventory overlay
		inventoryOverlay = new RuneUtilsOverlay(client, this, panel, config);
		overlayManager.add(inventoryOverlay);

		panel.log("Plugin started successfully");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Rune Utils stopped!");

		// Remove sidebar panel
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}

		// Remove overlay
		if (inventoryOverlay != null)
		{
			overlayManager.remove(inventoryOverlay);
			inventoryOverlay = null;
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
		// Inventory change detection for highlighting
		// The overlay will handle the actual highlighting
		if (inventoryOverlay != null)
		{
			inventoryOverlay.onInventoryChanged();
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
				// Create a new menu entry for "Create Profile" at the bottom (index 0)
				// Using index 0 makes it appear last in the menu (menus are built bottom-up)
				client.createMenuEntry(0)
					.setOption("Create Profile")
					.setTarget(entry.getTarget())
					.setParam0(entry.getParam0())
					.setParam1(entry.getParam1())
					.setType(MenuAction.RUNELITE)
					.onClick(this::handleCreateProfile);

				// Only add once per menu
				break;
			}
		}
	}

	private void handleCreateProfile(MenuEntry entry)
	{
		// Get clicked item
		Widget inventoryWidget = client.getWidget(ComponentID.INVENTORY_CONTAINER);
		if (inventoryWidget == null)
		{
			return;
		}

		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null)
		{
			return;
		}

		// Find the item that was clicked
		int slot = entry.getParam0();
		if (slot < 0 || slot >= inventory.size())
		{
			return;
		}

		Item item = inventory.getItems()[slot];
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

		// Capture current container states
		ProfileState profile = captureCurrentContainerState(itemName + " Profile");

		// Add to panel
		panel.addProfileState(profile);
		panel.log("Created profile: " + profile.getName());
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
	 * Capture current state of all containers
	 */
	private ProfileState captureCurrentContainerState(String profileName)
	{
		ProfileState profile = new ProfileState();
		profile.setName(profileName);

		Function<Integer, String> itemNameLookup = getItemNameLookup();

		// Capture inventory
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null)
		{
			ContainerSnapshot invSnapshot = ContainerSnapshot.captureFromContainer(
				ContainerType.INVENTORY,
				inventory,
				itemNameLookup
			);
			if (invSnapshot.getItemCount() > 0)
			{
				profile.setContainerSnapshot(ContainerType.INVENTORY, invSnapshot);
			}
		}

		// Capture equipment
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment != null)
		{
			ContainerSnapshot eqSnapshot = ContainerSnapshot.captureFromContainer(
				ContainerType.EQUIPMENT,
				equipment,
				itemNameLookup
			);
			if (eqSnapshot.getItemCount() > 0)
			{
				profile.setContainerSnapshot(ContainerType.EQUIPMENT, eqSnapshot);
			}
		}

		// Capture bank if open
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank != null)
		{
			ContainerSnapshot bankSnapshot = ContainerSnapshot.captureFromContainer(
				ContainerType.BANK,
				bank,
				itemNameLookup
			);
			if (bankSnapshot.getItemCount() > 0)
			{
				profile.setContainerSnapshot(ContainerType.BANK, bankSnapshot);
			}
		}

		return profile;
	}
}
