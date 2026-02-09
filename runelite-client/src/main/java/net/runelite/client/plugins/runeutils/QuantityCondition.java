package net.runelite.client.plugins.runeutils;

import lombok.Getter;

/**
 * Defines conditions for validating item quantities
 */
@Getter
public enum QuantityCondition
{
	EXACT("Exact", "="),
	AT_LEAST("At Least", ">="),
	AT_MOST("At Most", "<="),
	BETWEEN("Between", ".."),
	ANY("Any", "*");

	private final String displayName;
	private final String symbol;

	QuantityCondition(String displayName, String symbol)
	{
		this.displayName = displayName;
		this.symbol = symbol;
	}

	/**
	 * Validate if the actual quantity meets this condition
	 *
	 * @param actual the actual quantity
	 * @param expected the expected quantity
	 * @param max the maximum quantity (used for BETWEEN)
	 * @return true if the condition is satisfied
	 */
	public boolean validate(int actual, int expected, int max)
	{
		switch (this)
		{
			case EXACT:
				return actual == expected;
			case AT_LEAST:
				return actual >= expected;
			case AT_MOST:
				return actual <= expected;
			case BETWEEN:
				return actual >= expected && actual <= max;
			case ANY:
				return actual > 0;
			default:
				return false;
		}
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
