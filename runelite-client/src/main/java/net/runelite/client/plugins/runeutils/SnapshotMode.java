package net.runelite.client.plugins.runeutils;

import lombok.Getter;

@Getter
public enum SnapshotMode
{
	POSITION_EXACT("Exact Positions", true, true),
	POSITION_ANY_QTY("Positions, Any Qty", true, false),
	AGNOSTIC_EXACT("Any Position, Exact Qty", false, true),
	AGNOSTIC_ANY_QTY("Any Position, Any Qty", false, false);

	private final String displayName;
	private final boolean positionSpecific;
	private final boolean exactQuantity;

	SnapshotMode(String displayName, boolean positionSpecific, boolean exactQuantity)
	{
		this.displayName = displayName;
		this.positionSpecific = positionSpecific;
		this.exactQuantity = exactQuantity;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
