package net.runelite.client.plugins.runeutils;

import lombok.Getter;
import net.runelite.api.InventoryID;

/**
 * Represents different container types that can be tracked
 */
@Getter
public enum ContainerType
{
	INVENTORY("Inventory", InventoryID.INVENTORY.getId()),
	BANK("Bank", InventoryID.BANK.getId()),
	EQUIPMENT("Equipment", InventoryID.EQUIPMENT.getId()),
	BANK_INVENTORY("Bank + Inventory", -1);

	private final String displayName;
	private final int inventoryId;

	ContainerType(String displayName, int inventoryId)
	{
		this.displayName = displayName;
		this.inventoryId = inventoryId;
	}

	/**
	 * Check if this container type is a composite of multiple containers
	 */
	public boolean isComposite()
	{
		return this == BANK_INVENTORY;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
