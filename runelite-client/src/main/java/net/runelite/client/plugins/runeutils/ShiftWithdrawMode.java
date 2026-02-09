package net.runelite.client.plugins.runeutils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.MenuAction;

@Getter
@RequiredArgsConstructor
public enum ShiftWithdrawMode
{
	WITHDRAW_1("Withdraw-1", MenuAction.CC_OP, 2),
	WITHDRAW_5("Withdraw-5", MenuAction.CC_OP, 3),
	WITHDRAW_10("Withdraw-10", MenuAction.CC_OP, 4),
	WITHDRAW_X("Withdraw-X", MenuAction.CC_OP, 5),
	WITHDRAW_ALL("Withdraw-All", MenuAction.CC_OP_LOW_PRIORITY, 6),
	WITHDRAW_ALL_BUT_ONE("Withdraw-All-but-1", MenuAction.CC_OP_LOW_PRIORITY, 7),
	OFF("Off", MenuAction.CC_OP, 0);

	private final String name;
	private final MenuAction menuAction;
	private final int identifier;

	@Override
	public String toString()
	{
		return name;
	}
}
