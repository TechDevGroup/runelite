package net.runelite.client.plugins.runeutils;

import lombok.Value;

/**
 * Widget information for rendering container overlays
 */
@Value
public class ContainerWidgetInfo
{
	int groupId;
	int childId;
	int maxSlots;

	/**
	 * Get widget info for a container type
	 */
	public static ContainerWidgetInfo forContainer(ContainerType containerType)
	{
		switch (containerType)
		{
			case INVENTORY:
				return new ContainerWidgetInfo(149, 0, 28);
			case BANK:
				return new ContainerWidgetInfo(12, 13, 816);
			case EQUIPMENT:
				return new ContainerWidgetInfo(387, 0, 14);
			default:
				return null;
		}
	}
}
