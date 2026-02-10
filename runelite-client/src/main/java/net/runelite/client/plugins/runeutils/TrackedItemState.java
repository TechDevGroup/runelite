package net.runelite.client.plugins.runeutils;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;

/**
 * Represents the state of a tracked item including quantity conditions and validation flags
 */
@Data
public class TrackedItemState
{
	private int itemId;
	private String itemName;
	private Integer slot;
	private int quantity;
	private int quantityMax;
	private QuantityCondition quantityCondition = QuantityCondition.EXACT;
	private Set<ValidationFlag> validationFlags = new HashSet<>();
	private Set<Integer> alternateItemIds = new HashSet<>();
	private Set<String> alternateItemNames = new HashSet<>();

	public TrackedItemState(int itemId, String itemName)
	{
		this.itemId = itemId;
		this.itemName = itemName;
		this.quantity = 1;
		this.quantityMax = 1;
		this.slot = null;
	}

	/**
	 * Check if this tracked state accepts the given item ID (primary or alternate)
	 */
	public boolean acceptsItemId(int id)
	{
		if (itemId == id)
		{
			return true;
		}
		Set<Integer> alts = getAlternateItemIds();
		return alts != null && alts.contains(id);
	}

	/**
	 * Get all accepted item IDs (primary + alternates)
	 */
	public Set<Integer> getAllAcceptedItemIds()
	{
		Set<Integer> ids = new HashSet<>();
		ids.add(itemId);
		Set<Integer> alts = getAlternateItemIds();
		if (alts != null)
		{
			ids.addAll(alts);
		}
		return ids;
	}

	/**
	 * Add an alternate accepted item
	 */
	public void addAlternate(int altItemId, String altItemName)
	{
		getAlternateItemIds().add(altItemId);
		getAlternateItemNames().add(altItemName);
	}

	/**
	 * Remove an alternate accepted item
	 */
	public void removeAlternate(int altItemId, String altItemName)
	{
		getAlternateItemIds().remove(altItemId);
		getAlternateItemNames().remove(altItemName);
	}

	// Null-safe getters for Gson deserialization of old profiles
	public Set<Integer> getAlternateItemIds()
	{
		if (alternateItemIds == null)
		{
			alternateItemIds = new HashSet<>();
		}
		return alternateItemIds;
	}

	public Set<String> getAlternateItemNames()
	{
		if (alternateItemNames == null)
		{
			alternateItemNames = new HashSet<>();
		}
		return alternateItemNames;
	}

	/**
	 * Convert a legacy TrackedItem to the new TrackedItemState format
	 *
	 * @param legacy the legacy TrackedItem
	 * @return a new TrackedItemState
	 */
	public static TrackedItemState fromLegacyTrackedItem(TrackedItem legacy)
	{
		TrackedItemState state = new TrackedItemState(legacy.getItemId(), legacy.getItemName());
		state.quantity = legacy.getCount();
		state.quantityMax = legacy.getCount();

		if (legacy.isCountEnabled())
		{
			state.quantityCondition = QuantityCondition.EXACT;
		}
		else
		{
			state.quantityCondition = QuantityCondition.ANY;
		}

		return state;
	}

	/**
	 * Check if this item state matches the actual item
	 *
	 * @param actualItemId the actual item ID
	 * @param actualItemName the actual item name
	 * @param actualQuantity the actual quantity
	 * @param actualSlot the actual slot position
	 * @return true if the item matches
	 */
	public boolean matches(int actualItemId, String actualItemName, int actualQuantity, Integer actualSlot)
	{
		// Check ID match (primary or alternate)
		if (itemId > 0 && !acceptsItemId(actualItemId))
		{
			return false;
		}

		// Check name match only for primary item (alternates matched by ID are authoritative)
		boolean isAlternateMatch = itemId > 0 && itemId != actualItemId && acceptsItemId(actualItemId);
		if (!isAlternateMatch && itemName != null && !itemName.isEmpty() && actualItemName != null)
		{
			if (!actualItemName.toLowerCase().contains(itemName.toLowerCase()))
			{
				return false;
			}
		}

		// Check slot if REQUIRE_POSITION flag is set
		if (hasValidationFlag(ValidationFlag.REQUIRE_POSITION) && slot != null)
		{
			if (actualSlot == null || !slot.equals(actualSlot))
			{
				return false;
			}
		}

		// Check quantity unless IGNORE_QUANTITY flag is set
		if (!hasValidationFlag(ValidationFlag.IGNORE_QUANTITY))
		{
			if (!quantityCondition.validate(actualQuantity, quantity, quantityMax))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Add a validation flag
	 */
	public void addValidationFlag(ValidationFlag flag)
	{
		validationFlags.add(flag);
	}

	/**
	 * Remove a validation flag
	 */
	public void removeValidationFlag(ValidationFlag flag)
	{
		validationFlags.remove(flag);
	}

	/**
	 * Check if a validation flag is set
	 */
	public boolean hasValidationFlag(ValidationFlag flag)
	{
		return validationFlags.contains(flag);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(itemName).append(" (ID: ").append(itemId).append(")");

		int altCount = getAlternateItemIds().size();
		if (altCount > 0)
		{
			sb.append(" (+").append(altCount).append(" variants)");
		}

		if (slot != null)
		{
			sb.append(" [Slot: ").append(slot).append("]");
		}

		sb.append(" ").append(quantityCondition.getSymbol()).append(" ");

		if (quantityCondition == QuantityCondition.BETWEEN)
		{
			sb.append(quantity).append("-").append(quantityMax);
		}
		else if (quantityCondition != QuantityCondition.ANY)
		{
			sb.append(quantity);
		}

		return sb.toString();
	}
}
