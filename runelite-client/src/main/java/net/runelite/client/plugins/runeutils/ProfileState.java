package net.runelite.client.plugins.runeutils;

import java.util.function.Function;
import lombok.Data;
import net.runelite.api.Client;
import net.runelite.api.ItemContainer;

/**
 * Represents a profile for a single container type
 */
@Data
public class ProfileState
{
	private String id;
	private String name = "New Profile";
	private ContainerType containerType = ContainerType.INVENTORY;
	private ContainerSnapshot snapshot;
	private boolean enabled = true;
	private boolean collapsed = false;
	private boolean prioritized = false;
	private InterfaceState requiredInterfaceState = InterfaceState.ANY;
	private boolean previewMode = false;

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
	 * Check if this profile should be rendered based on interface state
	 * In preview mode, always returns true regardless of interface state
	 */
	public boolean shouldRender(Client client)
	{
		if (!enabled)
		{
			return false;
		}

		if (previewMode)
		{
			return true;
		}

		return requiredInterfaceState.isActive(client);
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
