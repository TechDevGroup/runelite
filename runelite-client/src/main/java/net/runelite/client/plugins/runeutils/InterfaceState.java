package net.runelite.client.plugins.runeutils;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;

/**
 * Represents interface states that can be required for profile activation
 */
@Getter
public enum InterfaceState
{
	ANY("Always Active", null),
	BANK_OPEN("Bank Open", 12),
	EQUIPMENT_OPEN("Equipment Open", 387),
	SHOP_OPEN("Shop Open", 300),
	TRADING_POST_OPEN("Trading Post Open", 335),
	GRAND_EXCHANGE_OPEN("Grand Exchange Open", 465);

	private final String displayName;
	private final Integer widgetId;

	InterfaceState(String displayName, Integer widgetId)
	{
		this.displayName = displayName;
		this.widgetId = widgetId;
	}

	/**
	 * Check if this interface state is currently active in the game
	 */
	public boolean isActive(Client client)
	{
		if (this == ANY)
		{
			return true;
		}

		if (widgetId == null)
		{
			return false;
		}

		Widget widget = client.getWidget(widgetId, 0);
		return widget != null && !widget.isHidden();
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
