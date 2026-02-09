package net.runelite.client.plugins.runeutils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
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
	private final RuneUtilsConfig config;

	@Inject
	private ItemManager itemManager;

	public RuneUtilsOverlay(Client client, RuneUtilsPlugin plugin, RuneUtilsPanel panel, RuneUtilsConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.panel = panel;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enableInventoryHighlighting())
		{
			return null;
		}

		// Check if itemManager is available
		if (itemManager == null)
		{
			return null;
		}

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

		// Get colors from config
		Color fillColor = config.highlightColor();
		Color borderColor = config.highlightBorderColor();

		// Iterate through items and highlight matches
		for (int i = 0; i < Math.min(items.length, 28); i++)
		{
			Item item = items[i];
			if (item == null || item.getId() == -1)
			{
				continue;
			}

			// Get item details
			int itemId = item.getId();
			var itemComposition = itemManager.getItemComposition(itemId);
			if (itemComposition == null)
			{
				continue;
			}
			String itemName = itemComposition.getName();
			if (itemName == null)
			{
				continue;
			}

			// Check if item matches any profile (legacy)
			boolean matchesLegacy = false;
			for (ItemProfile profile : panel.getProfiles())
			{
				if (profile.matches(itemId, itemName))
				{
					matchesLegacy = true;
					break;
				}
			}

			// Check if item matches any ProfileState
			boolean matchesProfileState = matchesProfileState(itemId, itemName, item.getQuantity(), i);

			if (matchesLegacy || matchesProfileState)
			{
				// Calculate slot position
				int col = i % columns;
				int row = i / columns;
				int x = bounds.x + (col * slotWidth);
				int y = bounds.y + (row * slotHeight);

				// Draw highlight
				graphics.setColor(fillColor);
				graphics.fillRect(x, y, slotWidth, slotHeight);

				// Draw border
				graphics.setColor(borderColor);
				graphics.setStroke(new BasicStroke(2));
				graphics.drawRect(x, y, slotWidth, slotHeight);
			}
		}

		return null;
	}

	/**
	 * Check if item matches any ProfileState
	 */
	private boolean matchesProfileState(int itemId, String itemName, int quantity, int slot)
	{
		for (ProfileState profile : panel.getProfileStates())
		{
			if (!profile.isEnabled())
			{
				continue;
			}

			ContainerSnapshot invSnapshot = profile.getContainerSnapshot(ContainerType.INVENTORY);
			if (invSnapshot == null)
			{
				continue;
			}

			// Check position-specific items
			TrackedItemState positionState = invSnapshot.getPositionSpecificStates().get(slot);
			if (positionState != null && positionState.matches(itemId, itemName, quantity, slot))
			{
				return true;
			}

			// Check position-agnostic items
			for (TrackedItemState state : invSnapshot.getPositionAgnosticStates().values())
			{
				if (state.matches(itemId, itemName, quantity, null))
				{
					return true;
				}
			}
		}

		return false;
	}

	public void onInventoryChanged()
	{
		// Can be used for additional processing when inventory changes
	}
}
