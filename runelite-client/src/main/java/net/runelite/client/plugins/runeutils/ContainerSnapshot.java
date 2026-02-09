package net.runelite.client.plugins.runeutils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

/**
 * Represents a snapshot of a container's state at a point in time
 */
@Data
public class ContainerSnapshot
{
	private ContainerType containerType;
	private Map<Integer, TrackedItemState> slotStates = new HashMap<>();
	private boolean enforceEmptySlots = false;

	public ContainerSnapshot(ContainerType containerType)
	{
		this.containerType = containerType;
	}

	/**
	 * Add an item state with position requirement
	 *
	 * @param slot the slot position
	 * @param itemState the item state
	 */
	public void addItemState(int slot, TrackedItemState itemState)
	{
		itemState.setSlot(slot);
		itemState.addValidationFlag(ValidationFlag.REQUIRE_POSITION);
		slotStates.put(slot, itemState);
	}

	/**
	 * Add an item state without position requirement
	 * If an item with the same ID already exists, this is a no-op (idempotent)
	 *
	 * @param itemState the item state
	 */
	public void addItemState(TrackedItemState itemState)
	{
		// Check if item ID already exists - if so, no-op (idempotent)
		if (hasItemId(itemState.getItemId()))
		{
			return;
		}

		// Use negative slot counter for position-agnostic items
		int negativeSlot = -1;
		while (slotStates.containsKey(negativeSlot))
		{
			negativeSlot--;
		}
		itemState.setSlot(negativeSlot);
		slotStates.put(negativeSlot, itemState);
	}

	/**
	 * Check if an item ID already exists in this snapshot
	 */
	public boolean hasItemId(int itemId)
	{
		return slotStates.values().stream()
			.anyMatch(state -> state.getItemId() == itemId);
	}

	/**
	 * Remove an item by its item ID
	 */
	public void removeItemByItemId(int itemId)
	{
		slotStates.entrySet().removeIf(entry -> entry.getValue().getItemId() == itemId);
	}

	/**
	 * Remove an item state by slot
	 */
	public void removeItemState(int slot)
	{
		slotStates.remove(slot);
	}

	/**
	 * Get all item states
	 */
	public Map<Integer, TrackedItemState> getAllItemStates()
	{
		return new HashMap<>(slotStates);
	}

	/**
	 * Get only position-specific states (slot >= 0)
	 */
	public Map<Integer, TrackedItemState> getPositionSpecificStates()
	{
		return slotStates.entrySet().stream()
			.filter(entry -> entry.getKey() >= 0)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Get only position-agnostic states (slot < 0)
	 */
	public Map<Integer, TrackedItemState> getPositionAgnosticStates()
	{
		return slotStates.entrySet().stream()
			.filter(entry -> entry.getKey() < 0)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Validate the container against this snapshot
	 *
	 * @param itemContainer the item container to validate
	 * @param itemNameLookup function to lookup item names by ID
	 * @return true if the container matches this snapshot
	 */
	public boolean validate(ItemContainer itemContainer, Function<Integer, String> itemNameLookup)
	{
		if (itemContainer == null)
		{
			return false;
		}

		Item[] items = itemContainer.getItems();
		Map<Integer, Boolean> matchedSlots = new HashMap<>();

		// Pass 1: Validate position-specific items
		Map<Integer, TrackedItemState> positionSpecific = getPositionSpecificStates();
		for (Map.Entry<Integer, TrackedItemState> entry : positionSpecific.entrySet())
		{
			int slot = entry.getKey();
			TrackedItemState state = entry.getValue();

			if (slot >= items.length)
			{
				return false;
			}

			Item item = items[slot];
			int itemId = item.getId();
			int quantity = item.getQuantity();
			String itemName = itemNameLookup.apply(itemId);

			if (!state.matches(itemId, itemName, quantity, slot))
			{
				return false;
			}

			matchedSlots.put(slot, true);
		}

		// Pass 2: Validate position-agnostic items
		Map<Integer, TrackedItemState> positionAgnostic = getPositionAgnosticStates();
		for (TrackedItemState state : positionAgnostic.values())
		{
			boolean found = false;

			for (int slot = 0; slot < items.length; slot++)
			{
				// Skip already matched slots
				if (matchedSlots.containsKey(slot))
				{
					continue;
				}

				Item item = items[slot];
				int itemId = item.getId();
				int quantity = item.getQuantity();
				String itemName = itemNameLookup.apply(itemId);

				if (state.matches(itemId, itemName, quantity, slot))
				{
					matchedSlots.put(slot, true);
					found = true;
					break;
				}
			}

			if (!found)
			{
				return false;
			}
		}

		// If enforcing empty slots, check that all unmatched slots are empty
		if (enforceEmptySlots)
		{
			for (int slot = 0; slot < items.length; slot++)
			{
				if (!matchedSlots.containsKey(slot))
				{
					Item item = items[slot];
					if (item.getId() != -1 && item.getQuantity() > 0)
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Capture a snapshot from a container
	 *
	 * @param type the container type
	 * @param container the item container
	 * @param itemNameLookup function to lookup item names by ID
	 * @return a new ContainerSnapshot
	 */
	public static ContainerSnapshot captureFromContainer(ContainerType type, ItemContainer container, Function<Integer, String> itemNameLookup)
	{
		ContainerSnapshot snapshot = new ContainerSnapshot(type);

		if (container == null)
		{
			return snapshot;
		}

		Item[] items = container.getItems();
		for (int slot = 0; slot < items.length; slot++)
		{
			Item item = items[slot];
			int itemId = item.getId();
			int quantity = item.getQuantity();

			// Skip empty slots
			if (itemId == -1 || quantity <= 0)
			{
				continue;
			}

			String itemName = itemNameLookup.apply(itemId);
			TrackedItemState state = new TrackedItemState(itemId, itemName);
			state.setQuantity(quantity);
			state.setQuantityMax(quantity);
			state.setQuantityCondition(QuantityCondition.EXACT);

			snapshot.addItemState(slot, state);
		}

		return snapshot;
	}

	/**
	 * Clear all item states
	 */
	public void clear()
	{
		slotStates.clear();
	}

	/**
	 * Get the total number of tracked items
	 */
	public int getItemCount()
	{
		return slotStates.size();
	}
}
