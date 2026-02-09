package net.runelite.client.plugins.runeutils;

import lombok.Data;

/**
 * Represents a tracked item with ID, name, and count
 */
@Data
public class TrackedItem
{
	private int itemId;
	private String itemName;
	private int count;
	private boolean countEnabled;

	public TrackedItem(int itemId, String itemName)
	{
		this.itemId = itemId;
		this.itemName = itemName;
		this.count = 1;
		this.countEnabled = false;
	}

	public TrackedItem(int itemId, String itemName, int count)
	{
		this.itemId = itemId;
		this.itemName = itemName;
		this.count = count;
		this.countEnabled = true;
	}

	/**
	 * Check if this item matches by ID or name
	 */
	public boolean matches(int id, String name)
	{
		if (itemId > 0 && itemId == id)
		{
			return true;
		}

		if (itemName != null && !itemName.isEmpty() && name != null)
		{
			return name.toLowerCase().contains(itemName.toLowerCase());
		}

		return false;
	}

	@Override
	public String toString()
	{
		if (countEnabled)
		{
			return String.format("%s (ID: %d, Count: %d)", itemName, itemId, count);
		}
		return String.format("%s (ID: %d)", itemName, itemId);
	}
}
