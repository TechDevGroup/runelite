package net.runelite.client.plugins.runeutils;

import lombok.Data;

/**
 * State manager for slot selection mode
 */
@Data
public class SlotSelectionState
{
	private boolean active = false;
	private TrackedItemState itemBeingConfigured = null;
	private ProfileState profileBeingConfigured = null;
	private java.util.function.Consumer<Integer> onSlotSelected = null;

	public void enterSelectionMode(TrackedItemState item, ProfileState profile, java.util.function.Consumer<Integer> callback)
	{
		System.out.println("[SlotSelectionState] Entering selection mode for item: " + item.getItemName());
		this.active = true;
		this.itemBeingConfigured = item;
		this.profileBeingConfigured = profile;
		this.onSlotSelected = callback;
		System.out.println("[SlotSelectionState] Active state: " + this.active);
	}

	public void exitSelectionMode()
	{
		this.active = false;
		this.itemBeingConfigured = null;
		this.profileBeingConfigured = null;
		this.onSlotSelected = null;
	}

	public void selectSlot(int slot)
	{
		if (active && onSlotSelected != null)
		{
			onSlotSelected.accept(slot);
			exitSelectionMode();
		}
	}
}
