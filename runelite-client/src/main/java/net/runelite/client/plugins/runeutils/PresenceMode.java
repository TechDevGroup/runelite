package net.runelite.client.plugins.runeutils;

import lombok.Getter;

@Getter
public enum PresenceMode
{
	ALL("All Required"),
	ANY("Any Required"),
	AT_LEAST_N("At Least N");

	private final String displayName;

	PresenceMode(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
