package net.runelite.client.plugins.runeutils;

import lombok.Getter;

/**
 * Flags that modify how item validation is performed
 */
@Getter
public enum ValidationFlag
{
	NONE("None"),
	REQUIRE_POSITION("Require Position"),
	REQUIRE_EQUIPPED("Require Equipped"),
	ALLOW_NOTED("Allow Noted"),
	ALLOW_PLACEHOLDER("Allow Placeholder"),
	IGNORE_QUANTITY("Ignore Quantity");

	private final String displayName;

	ValidationFlag(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
