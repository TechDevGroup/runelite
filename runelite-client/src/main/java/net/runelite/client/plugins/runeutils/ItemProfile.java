package net.runelite.client.plugins.runeutils;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Represents a filter profile for inventory item highlighting
 */
@Data
public class ItemProfile
{
	private String name = "New Profile";
	private String filter = ""; // Legacy text filter
	private List<TrackedItem> trackedItems = new ArrayList<>();
	private boolean enabled = true;
	private ValidationMode validationMode = ValidationMode.ANY;

	public enum ValidationMode
	{
		ALL("Match All"),
		ANY("Match Any"),
		NONE("Match None");

		private final String displayName;

		ValidationMode(String displayName)
		{
			this.displayName = displayName;
		}

		@Override
		public String toString()
		{
			return displayName;
		}
	}

	/**
	 * Add a tracked item to this profile
	 */
	public void addTrackedItem(TrackedItem item)
	{
		trackedItems.add(item);
	}

	/**
	 * Remove a tracked item from this profile
	 */
	public void removeTrackedItem(TrackedItem item)
	{
		trackedItems.remove(item);
	}

	/**
	 * Check if an item matches this profile's filters
	 */
	public boolean matches(int itemId, String itemName)
	{
		if (!enabled)
		{
			return false;
		}

		int matchCount = 0;
		int totalChecks = 0;

		// Check tracked items (ID-based matching)
		if (!trackedItems.isEmpty())
		{
			for (TrackedItem tracked : trackedItems)
			{
				totalChecks++;
				if (tracked.matches(itemId, itemName))
				{
					matchCount++;
				}
			}
		}

		// Check legacy text filters (name-based matching)
		if (!filter.isEmpty())
		{
			String[] filters = filter.split(",");
			for (String f : filters)
			{
				String trimmed = f.trim().toLowerCase();
				if (!trimmed.isEmpty())
				{
					totalChecks++;
					if (itemName.toLowerCase().contains(trimmed))
					{
						matchCount++;
					}
				}
			}
		}

		if (totalChecks == 0)
		{
			return false;
		}

		switch (validationMode)
		{
			case ALL:
				return matchCount == totalChecks;
			case ANY:
				return matchCount > 0;
			case NONE:
				return matchCount == 0;
			default:
				return false;
		}
	}

	/**
	 * Legacy method for backwards compatibility
	 */
	public boolean matches(String itemName)
	{
		return matches(-1, itemName);
	}
}
