package net.runelite.client.plugins.runeutils;

import java.util.function.Function;
import lombok.Data;
import net.runelite.api.ItemContainer;

/**
 * Represents a profile for a single container type
 */
@Data
public class ProfileState
{
	private String name = "New Profile";
	private ContainerType containerType = ContainerType.INVENTORY;
	private ContainerSnapshot snapshot;
	private boolean enabled = true;
	private boolean collapsed = false;

	public ProfileState()
	{
		this.snapshot = new ContainerSnapshot(containerType);
	}

	public ProfileState(String name, ContainerType containerType)
	{
		this.name = name;
		this.containerType = containerType;
		this.snapshot = new ContainerSnapshot(containerType);
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
		if (!enabled || snapshot == null)
		{
			return false;
		}

		ItemContainer container = containerProvider.apply(containerType);
		return snapshot.validate(container, itemNameLookup);
	}

	/**
	 * Get the number of tracked items in this profile
	 */
	public int getItemCount()
	{
		return snapshot != null ? snapshot.getItemCount() : 0;
	}

	/**
	 * Get display name with container type
	 */
	public String getDisplayName()
	{
		return name + " (" + containerType.getDisplayName() + ")";
	}
}
