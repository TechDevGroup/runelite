package net.runelite.client.plugins.runeutils;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.Data;
import net.runelite.api.ItemContainer;

/**
 * Represents a complete profile state with multiple container snapshots
 */
@Data
public class ProfileState
{
	private String name = "New Profile";
	private Map<ContainerType, ContainerSnapshot> containerSnapshots = new EnumMap<>(ContainerType.class);
	private boolean enabled = true;
	private ValidationMode validationMode = ValidationMode.ALL;

	/**
	 * Validation mode for multiple container snapshots
	 */
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
	 * Set a container snapshot for a specific container type
	 */
	public void setContainerSnapshot(ContainerType type, ContainerSnapshot snapshot)
	{
		containerSnapshots.put(type, snapshot);
	}

	/**
	 * Get a container snapshot for a specific container type
	 */
	public ContainerSnapshot getContainerSnapshot(ContainerType type)
	{
		return containerSnapshots.get(type);
	}

	/**
	 * Remove a container snapshot
	 */
	public void removeContainerSnapshot(ContainerType type)
	{
		containerSnapshots.remove(type);
	}

	/**
	 * Check if this profile has any container snapshots
	 */
	public boolean hasContainerSnapshots()
	{
		return !containerSnapshots.isEmpty();
	}

	/**
	 * Get the set of tracked container types
	 */
	public Set<ContainerType> getTrackedContainers()
	{
		return containerSnapshots.keySet();
	}

	/**
	 * Validate the profile against current game state
	 *
	 * @param containerProvider function to get ItemContainer by ContainerType
	 * @param itemNameLookup function to lookup item names by ID
	 * @return true if the profile matches
	 */
	public boolean validate(Function<ContainerType, ItemContainer> containerProvider, Function<Integer, String> itemNameLookup)
	{
		if (!enabled || containerSnapshots.isEmpty())
		{
			return false;
		}

		int matchCount = 0;
		int totalChecks = containerSnapshots.size();

		for (Map.Entry<ContainerType, ContainerSnapshot> entry : containerSnapshots.entrySet())
		{
			ContainerType type = entry.getKey();
			ContainerSnapshot snapshot = entry.getValue();

			ItemContainer container = containerProvider.apply(type);
			if (snapshot.validate(container, itemNameLookup))
			{
				matchCount++;
			}
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
	 * Convert a legacy ItemProfile to the new ProfileState format
	 *
	 * @param legacy the legacy ItemProfile
	 * @return a new ProfileState
	 */
	public static ProfileState fromLegacyItemProfile(ItemProfile legacy)
	{
		ProfileState state = new ProfileState();
		state.name = legacy.getName();
		state.enabled = legacy.isEnabled();

		// Map legacy validation modes
		switch (legacy.getValidationMode())
		{
			case ALL:
				state.validationMode = ValidationMode.ALL;
				break;
			case ANY:
				state.validationMode = ValidationMode.ANY;
				break;
			case NONE:
				state.validationMode = ValidationMode.NONE;
				break;
		}

		// Convert legacy tracked items to inventory snapshot
		if (!legacy.getTrackedItems().isEmpty())
		{
			ContainerSnapshot inventorySnapshot = new ContainerSnapshot(ContainerType.INVENTORY);

			for (TrackedItem legacyItem : legacy.getTrackedItems())
			{
				TrackedItemState itemState = TrackedItemState.fromLegacyTrackedItem(legacyItem);
				inventorySnapshot.addItemState(itemState);
			}

			state.setContainerSnapshot(ContainerType.INVENTORY, inventorySnapshot);
		}

		return state;
	}

	/**
	 * Get the total number of tracked items across all containers
	 */
	public int getTotalTrackedItemCount()
	{
		int total = 0;
		for (ContainerSnapshot snapshot : containerSnapshots.values())
		{
			total += snapshot.getItemCount();
		}
		return total;
	}

	/**
	 * Clear all container snapshots
	 */
	public void clearAllSnapshots()
	{
		containerSnapshots.clear();
	}
}
