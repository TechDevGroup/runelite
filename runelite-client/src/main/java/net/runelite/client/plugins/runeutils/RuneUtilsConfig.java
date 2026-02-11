package net.runelite.client.plugins.runeutils;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("runeutils")
public interface RuneUtilsConfig extends Config
{
	@ConfigSection(
		name = "Menu Manipulation",
		description = "Settings for right-click menu priority",
		position = 0
	)
	String menuSection = "menu";

	@ConfigSection(
		name = "Inventory Highlighting",
		description = "Settings for inventory item highlighting",
		position = 1
	)
	String inventorySection = "inventory";

	@ConfigSection(
		name = "Dev Server",
		description = "Settings for the embedded dev server",
		position = 2
	)
	String devServerSection = "devServer";

	// Menu Manipulation Settings
	@ConfigItem(
		keyName = "enableMenuManipulation",
		name = "Enable Menu Manipulation",
		description = "Enable right-click menu priority modification",
		section = menuSection,
		position = 0
	)
	default boolean enableMenuManipulation()
	{
		return true;
	}

	@ConfigItem(
		keyName = "shiftDepositPriority",
		name = "Shift Deposit Priority",
		description = "Menu option to prioritize when shift-clicking items in your inventory while bank is open",
		section = menuSection,
		position = 1
	)
	default ShiftDepositMode shiftDepositPriority()
	{
		return ShiftDepositMode.DEPOSIT_ALL;
	}

	@ConfigItem(
		keyName = "shiftWithdrawPriority",
		name = "Shift Withdraw Priority",
		description = "Menu option to prioritize when shift-clicking items in bank",
		section = menuSection,
		position = 2
	)
	default ShiftWithdrawMode shiftWithdrawPriority()
	{
		return ShiftWithdrawMode.WITHDRAW_ALL;
	}

	@ConfigItem(
		keyName = "enableSyntheticMenus",
		name = "Enable Synthetic Menu Options",
		description = "Add custom menu options for profile management (e.g., 'Add to Profile', 'Create Profile')",
		section = menuSection,
		position = 3
	)
	default boolean enableSyntheticMenus()
	{
		return true;
	}

	// Inventory Highlighting Settings
	@ConfigItem(
		keyName = "enableInventoryHighlighting",
		name = "Enable Inventory Highlighting",
		description = "Enable inventory item highlighting",
		section = inventorySection,
		position = 0
	)
	default boolean enableInventoryHighlighting()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight Color",
		description = "Color for highlighting matching items",
		section = inventorySection,
		position = 1
	)
	default Color highlightColor()
	{
		return new Color(255, 255, 0, 100);
	}

	@ConfigItem(
		keyName = "highlightBorderColor",
		name = "Highlight Border Color",
		description = "Border color for highlighting matching items",
		section = inventorySection,
		position = 2
	)
	default Color highlightBorderColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = "slotMatchColor",
		name = "Slot Match Color",
		description = "Color for inventory slots that match profile requirements",
		section = inventorySection,
		position = 3
	)
	default Color slotMatchColor()
	{
		return new Color(0, 255, 0, 80);
	}

	@ConfigItem(
		keyName = "slotMismatchColor",
		name = "Slot Mismatch Color",
		description = "Color for inventory slots that don't match profile requirements",
		section = inventorySection,
		position = 4
	)
	default Color slotMismatchColor()
	{
		return new Color(255, 0, 0, 80);
	}

	@ConfigItem(
		keyName = "slotSelectionColor",
		name = "Slot Selection Color",
		description = "Color for slot selection mode overlay",
		section = inventorySection,
		position = 5
	)
	default Color slotSelectionColor()
	{
		return new Color(255, 255, 0, 120);
	}

	// Dev Server Settings
	@ConfigItem(
		keyName = "enableDevServer",
		name = "Enable Dev Server",
		description = "Automatically start the dev server when the plugin loads",
		section = devServerSection,
		position = 0
	)
	default boolean enableDevServer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "devServerPort",
		name = "Dev Server Port",
		description = "Port for the dev server HTTP and WebSocket",
		section = devServerSection,
		position = 1
	)
	default int devServerPort()
	{
		return 3000;
	}

	// Profile persistence (hidden from UI)
	@ConfigItem(
		keyName = "profilesData",
		name = "",
		description = "",
		hidden = true
	)
	default String profilesData()
	{
		return "[]";
	}

	@ConfigItem(
		keyName = "profilesData",
		name = "",
		description = "",
		hidden = true
	)
	void setProfilesData(String profilesJson);
}
