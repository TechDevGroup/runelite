package net.runelite.client.plugins.runeutils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShiftDepositMode
{
	DEPOSIT_1("Deposit-1", 3, 2),
	DEPOSIT_5("Deposit-5", 4, 3),
	DEPOSIT_10("Deposit-10", 5, 4),
	DEPOSIT_X("Deposit-X", 6, 5),
	DEPOSIT_ALL("Deposit-All", 8, 6),
	OFF("Off", 0, 0);

	private final String name;
	private final int identifier;
	private final int identifierDepositBox;

	@Override
	public String toString()
	{
		return name;
	}
}
