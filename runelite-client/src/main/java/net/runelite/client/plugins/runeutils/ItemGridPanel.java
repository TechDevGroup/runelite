package net.runelite.client.plugins.runeutils;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ItemGridPanel extends JPanel
{
	private final ItemManager itemManager;
	private final ContainerType containerType;
	private final Runnable onDataChanged;

	private final List<ItemSlotBox> slots;
	private ContainerSnapshot snapshot;

	public ItemGridPanel(ItemManager itemManager, ContainerType containerType, Runnable onDataChanged)
	{
		this.itemManager = itemManager;
		this.containerType = containerType;
		this.onDataChanged = onDataChanged;
		this.slots = new ArrayList<>();

		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setupLayout();
		initializeSlots();
	}

	private void setupLayout()
	{
		int rows, cols;

		switch (containerType)
		{
			case INVENTORY:
				rows = 7;
				cols = 4;
				break;
			case EQUIPMENT:
				rows = 7;
				cols = 2;
				break;
			case BANK:
				// Reduce bank to reasonable size - use only what's needed
				rows = 13;  // Reduced from 100
				cols = 8;
				break;
			default:
				rows = 7;
				cols = 4;
				break;
		}

		setLayout(new GridLayout(rows, cols, 1, 1));
	}

	private void initializeSlots()
	{
		int slotCount = getGridCapacity();

		// Create slots
		for (int i = 0; i < slotCount; i++)
		{
			ItemSlotBox slotBox = new ItemSlotBox(itemManager, this, i);
			slots.add(slotBox);
			add(slotBox);
		}

		// Only set preferred size for bank (needs scrolling)
		// Other containers will expand to fill available space
		if (containerType == ContainerType.BANK)
		{
			setPreferredSize(calculateMinimumSize());
		}
	}

	private Dimension calculateMinimumSize()
	{
		int slotSize = 36; // ItemSlotBox.SLOT_SIZE
		int gap = 1;
		int cols = 8;
		int rows = 13;

		int width = (slotSize + gap) * cols + gap;
		int height = (slotSize + gap) * rows + gap;
		return new Dimension(width, height);
	}

	private int getGridCapacity()
	{
		switch (containerType)
		{
			case INVENTORY:
				return 28;
			case EQUIPMENT:
				return 14;
			case BANK:
				return 104;  // Reduced from 800 (13 rows x 8 cols)
			default:
				return 28;
		}
	}

	public void setSnapshot(ContainerSnapshot snapshot)
	{
		this.snapshot = snapshot;
		refresh();
	}

	public ContainerSnapshot getSnapshot()
	{
		return snapshot;
	}

	public void refresh()
	{
		if (snapshot == null)
		{
			for (ItemSlotBox slot : slots)
			{
				slot.setItemState(null);
			}
			return;
		}

		boolean[] slotFilled = new boolean[slots.size()];

		for (TrackedItemState itemState : snapshot.getAllItemStates().values())
		{
			if (itemState.getSlot() >= 0 && itemState.getSlot() < slots.size())
			{
				if (itemState.hasValidationFlag(ValidationFlag.REQUIRE_POSITION))
				{
					slots.get(itemState.getSlot()).setItemState(itemState);
					slotFilled[itemState.getSlot()] = true;
				}
			}
		}

		Queue<TrackedItemState> positionAgnosticItems = new LinkedList<>();
		for (TrackedItemState itemState : snapshot.getAllItemStates().values())
		{
			if (itemState.getSlot() < 0 || !itemState.hasValidationFlag(ValidationFlag.REQUIRE_POSITION))
			{
				positionAgnosticItems.offer(itemState);
			}
		}

		for (int i = 0; i < slots.size(); i++)
		{
			if (!slotFilled[i])
			{
				TrackedItemState nextItem = positionAgnosticItems.poll();
				slots.get(i).setItemState(nextItem);
			}
		}
	}

	public void swapItems(int fromSlot, int toSlot)
	{
		if (snapshot == null || fromSlot < 0 || toSlot < 0 || fromSlot >= slots.size() || toSlot >= slots.size())
		{
			return;
		}

		TrackedItemState fromState = slots.get(fromSlot).getItemState();
		TrackedItemState toState = slots.get(toSlot).getItemState();

		if (fromState == null && toState == null)
		{
			return;
		}

		// Update slots directly without full refresh
		slots.get(fromSlot).setItemState(toState);
		slots.get(toSlot).setItemState(fromState);

		// Update snapshot
		ContainerSnapshot newSnapshot = new ContainerSnapshot(snapshot.getContainerType());

		// Copy all existing items except the ones being swapped
		for (Map.Entry<Integer, TrackedItemState> entry : snapshot.getAllItemStates().entrySet())
		{
			TrackedItemState state = entry.getValue();
			if (state != fromState && state != toState)
			{
				if (state.getSlot() >= 0)
				{
					newSnapshot.addItemState(state.getSlot(), state);
				}
				else
				{
					newSnapshot.addItemState(state);
				}
			}
		}

		// Add swapped items with updated positions
		if (fromState != null)
		{
			TrackedItemState updatedFrom = new TrackedItemState(fromState.getItemId(), fromState.getItemName());
			updatedFrom.setQuantity(fromState.getQuantity());
			updatedFrom.setQuantityMax(fromState.getQuantityMax());
			updatedFrom.setQuantityCondition(fromState.getQuantityCondition());
			for (ValidationFlag flag : fromState.getValidationFlags())
			{
				updatedFrom.addValidationFlag(flag);
			}
			updatedFrom.addValidationFlag(ValidationFlag.REQUIRE_POSITION);
			newSnapshot.addItemState(toSlot, updatedFrom);
		}

		if (toState != null)
		{
			TrackedItemState updatedTo = new TrackedItemState(toState.getItemId(), toState.getItemName());
			updatedTo.setQuantity(toState.getQuantity());
			updatedTo.setQuantityMax(toState.getQuantityMax());
			updatedTo.setQuantityCondition(toState.getQuantityCondition());
			for (ValidationFlag flag : toState.getValidationFlags())
			{
				updatedTo.addValidationFlag(flag);
			}
			updatedTo.addValidationFlag(ValidationFlag.REQUIRE_POSITION);
			newSnapshot.addItemState(fromSlot, updatedTo);
		}

		snapshot = newSnapshot;
		// Only repaint the affected slots instead of full refresh
		slots.get(fromSlot).repaint();
		slots.get(toSlot).repaint();
		notifyDataChanged();
	}

	public void notifyItemChanged(int slotIndex, TrackedItemState updatedState)
	{
		if (snapshot == null || slotIndex < 0 || slotIndex >= slots.size())
		{
			return;
		}

		// Update the slot directly
		slots.get(slotIndex).setItemState(updatedState);

		ContainerSnapshot newSnapshot = new ContainerSnapshot(snapshot.getContainerType());
		TrackedItemState oldState = slots.get(slotIndex).getItemState();

		// Copy all existing items except the one being updated
		for (Map.Entry<Integer, TrackedItemState> entry : snapshot.getAllItemStates().entrySet())
		{
			TrackedItemState state = entry.getValue();
			if (state != oldState)
			{
				if (state.getSlot() >= 0)
				{
					newSnapshot.addItemState(state.getSlot(), state);
				}
				else
				{
					newSnapshot.addItemState(state);
				}
			}
		}

		// Add the updated item
		if (updatedState.getSlot() >= 0)
		{
			newSnapshot.addItemState(updatedState.getSlot(), updatedState);
		}
		else
		{
			newSnapshot.addItemState(updatedState);
		}

		snapshot = newSnapshot;
		// Only repaint the affected slot
		slots.get(slotIndex).repaint();
		notifyDataChanged();
	}

	public void removeItem(int slotIndex)
	{
		if (snapshot == null)
		{
			return;
		}

		ContainerSnapshot newSnapshot = new ContainerSnapshot(snapshot.getContainerType());
		TrackedItemState oldState = slots.get(slotIndex).getItemState();

		// Copy all existing items except the one being removed
		for (Map.Entry<Integer, TrackedItemState> entry : snapshot.getAllItemStates().entrySet())
		{
			TrackedItemState state = entry.getValue();
			if (state != oldState)
			{
				if (state.getSlot() >= 0)
				{
					newSnapshot.addItemState(state.getSlot(), state);
				}
				else
				{
					newSnapshot.addItemState(state);
				}
			}
		}

		snapshot = newSnapshot;
		refresh();
		notifyDataChanged();
	}

	private void notifyDataChanged()
	{
		if (onDataChanged != null)
		{
			onDataChanged.run();
		}
	}
}
