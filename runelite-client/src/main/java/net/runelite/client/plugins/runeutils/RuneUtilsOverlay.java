/*
 * Copyright (c) 2024, TechDevGroup
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.runeutils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Overlay for highlighting inventory items based on configured profiles
 */
public class RuneUtilsOverlay extends Overlay
{
	private final Client client;
	private final RuneUtilsPlugin plugin;
	private final RuneUtilsPanel panel;

	@Inject
	private ItemManager itemManager;

	public RuneUtilsOverlay(Client client, RuneUtilsPlugin plugin, RuneUtilsPanel panel)
	{
		this.client = client;
		this.plugin = plugin;
		this.panel = panel;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		// Get inventory
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null)
		{
			return null;
		}

		Item[] items = inventory.getItems();
		if (items == null)
		{
			return null;
		}

		// Get inventory widget for positioning
		Widget inventoryWidget = client.getWidget(149, 0);
		if (inventoryWidget == null)
		{
			return null;
		}

		// Calculate slot dimensions
		Rectangle bounds = inventoryWidget.getBounds();
		int columns = 4;
		int rows = 7;
		int slotWidth = bounds.width / columns;
		int slotHeight = bounds.height / rows;

		// Iterate through items and highlight matches
		for (int i = 0; i < Math.min(items.length, 28); i++)
		{
			Item item = items[i];
			if (item == null || item.getId() == -1)
			{
				continue;
			}

			// Get item name
			String itemName = itemManager.getItemComposition(item.getId()).getName();

			// Check if item matches any profile
			for (ItemProfile profile : panel.getProfiles())
			{
				if (profile.matches(itemName))
				{
					// Calculate slot position
					int col = i % columns;
					int row = i / columns;
					int x = bounds.x + (col * slotWidth);
					int y = bounds.y + (row * slotHeight);

					// Draw highlight
					graphics.setColor(new Color(255, 255, 0, 100));
					graphics.fillRect(x, y, slotWidth, slotHeight);

					// Draw border
					graphics.setColor(Color.YELLOW);
					graphics.setStroke(new BasicStroke(2));
					graphics.drawRect(x, y, slotWidth, slotHeight);

					break; // Only highlight once per item
				}
			}
		}

		return null;
	}

	public void onInventoryChanged()
	{
		// Can be used for additional processing when inventory changes
	}
}
