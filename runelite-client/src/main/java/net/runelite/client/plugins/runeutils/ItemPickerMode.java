package net.runelite.client.plugins.runeutils;

/**
 * Picker mode for selecting items in-game
 */
public enum ItemPickerMode
{
	DISABLED("Off"),
	INVENTORY("Inventory"),
	GROUND("Ground Items"),
	OBJECTS("Objects"),
	NPCS("NPCs");

	private final String displayName;

	ItemPickerMode(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
