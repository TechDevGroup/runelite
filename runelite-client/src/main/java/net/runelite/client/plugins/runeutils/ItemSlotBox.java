package net.runelite.client.plugins.runeutils;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.AsyncBufferedImage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Item slot following loot tracker pattern - simple JPanel with JLabel
 */
public class ItemSlotBox extends JPanel
{
	private final ItemManager itemManager;
	private final ItemGridPanel parentGrid;
	private final int slotIndex;

	private TrackedItemState itemState;
	private final JLabel imageLabel;

	public ItemSlotBox(ItemManager itemManager, ItemGridPanel parentGrid, int slotIndex)
	{
		this.itemManager = itemManager;
		this.parentGrid = parentGrid;
		this.slotIndex = slotIndex;

		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setLayout(new BorderLayout());

		// Create centered image label - loot tracker pattern
		imageLabel = new JLabel();
		imageLabel.setVerticalAlignment(SwingConstants.CENTER);
		imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(imageLabel, BorderLayout.CENTER);

		// Right-click for context menu
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
				{
					showContextMenu(e);
				}
			}
		});
	}

	public void setItemState(TrackedItemState state)
	{
		this.itemState = state;
		updateDisplay();
	}

	private void updateDisplay()
	{
		if (itemState == null || itemState.getItemId() <= 0)
		{
			imageLabel.setIcon(null);
			imageLabel.setToolTipText(null);
			return;
		}

		// Get item image with quantity overlay - loot tracker pattern
		AsyncBufferedImage itemImage = itemManager.getImage(
			itemState.getItemId(),
			itemState.getQuantity(),
			itemState.getQuantity() > 1
		);
		itemImage.addTo(imageLabel);

		// Build tooltip
		StringBuilder tooltip = new StringBuilder();
		tooltip.append("<html>");
		tooltip.append(itemState.getItemName());

		if (itemState.getQuantity() > 1)
		{
			tooltip.append(" x ").append(itemState.getQuantity());
		}

		// Show condition
		String condition = itemState.getQuantityCondition().name();
		if (itemState.getQuantityCondition() != QuantityCondition.ANY)
		{
			tooltip.append("<br>").append(condition);
			tooltip.append(": ").append(itemState.getQuantity());
			if (itemState.getQuantityCondition() == QuantityCondition.BETWEEN)
			{
				tooltip.append(" - ").append(itemState.getQuantityMax());
			}
		}

		if (itemState.hasValidationFlag(ValidationFlag.REQUIRE_POSITION))
		{
			tooltip.append("<br>Position: Slot ").append(itemState.getSlot() + 1);
		}

		tooltip.append("</html>");
		imageLabel.setToolTipText(tooltip.toString());
	}

	public TrackedItemState getItemState()
	{
		return itemState;
	}

	public int getSlotIndex()
	{
		return slotIndex;
	}

	private void showContextMenu(MouseEvent e)
	{
		if (itemState == null || itemState.getItemId() <= 0)
		{
			return;
		}

		ItemContextMenu menu = new ItemContextMenu(
			itemState,
			updatedState -> {
				itemState = updatedState;
				parentGrid.notifyItemChanged(slotIndex, updatedState);
				updateDisplay();
			},
			() -> {
				itemState = null;
				parentGrid.removeItem(slotIndex);
				updateDisplay();
			},
			null,  // No slot selection for grid view
			null   // No profile for grid view
		);

		menu.show(this, e.getX(), e.getY());
	}
}
