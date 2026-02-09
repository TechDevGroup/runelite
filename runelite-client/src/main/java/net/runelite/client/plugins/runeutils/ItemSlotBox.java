package net.runelite.client.plugins.runeutils;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class ItemSlotBox extends JPanel
{
	private static final int SLOT_SIZE = 36;
	private static final int LOCK_ICON_SIZE = 8;
	private static final Color QUANTITY_COLOR = Color.YELLOW;
	private static final Color QUANTITY_SHADOW = Color.BLACK;
	private static final Color DRAG_HIGHLIGHT = new Color(255, 255, 0, 80);

	private static ItemSlotBox dragSource = null;

	private final ItemManager itemManager;
	private final ItemGridPanel parentGrid;
	private final int slotIndex;

	private TrackedItemState itemState;
	private AsyncBufferedImage itemImage;
	private boolean isDragTarget = false;

	public ItemSlotBox(ItemManager itemManager, ItemGridPanel parentGrid, int slotIndex)
	{
		this.itemManager = itemManager;
		this.parentGrid = parentGrid;
		this.slotIndex = slotIndex;

		setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
		setMinimumSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
		setMaximumSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(BorderFactory.createLineBorder(ColorScheme.DARK_GRAY_COLOR, 1));

		ItemSlotMouseAdapter mouseAdapter = new ItemSlotMouseAdapter();
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}

	public void setItemState(TrackedItemState state)
	{
		this.itemState = state;

		if (state != null && state.getItemId() > 0)
		{
			itemImage = itemManager.getImage(state.getItemId());

			if (itemImage != null)
			{
				// Use callback approach since addTo() doesn't support JPanel
				itemImage.onLoaded(() -> repaint());
			}
		}
		else
		{
			itemImage = null;
		}

		repaint();
	}

	public TrackedItemState getItemState()
	{
		return itemState;
	}

	public int getSlotIndex()
	{
		return slotIndex;
	}

	public void setDragTarget(boolean isDragTarget)
	{
		this.isDragTarget = isDragTarget;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (isDragTarget)
		{
			g2d.setColor(DRAG_HIGHLIGHT);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}

		if (itemState == null || itemState.getItemId() <= 0)
		{
			return;
		}

		if (itemImage != null)
		{
			g2d.drawImage(itemImage, 0, 0, SLOT_SIZE, SLOT_SIZE, null);
		}

		if (itemState.getQuantity() > 1)
		{
			drawQuantityText(g2d);
		}

		drawConditionIndicator(g2d);

		if (itemState.hasValidationFlag(ValidationFlag.REQUIRE_POSITION))
		{
			drawLockIcon(g2d);
		}
	}

	private void drawQuantityText(Graphics2D g2d)
	{
		String quantityStr = formatQuantity(itemState.getQuantity());

		g2d.setFont(FontManager.getRunescapeSmallFont());
		FontMetrics fm = g2d.getFontMetrics();
		int textWidth = fm.stringWidth(quantityStr);
		int textHeight = fm.getHeight();

		int x = getWidth() - textWidth - 2;
		int y = getHeight() - 2;

		g2d.setColor(QUANTITY_SHADOW);
		g2d.drawString(quantityStr, x + 1, y + 1);

		g2d.setColor(QUANTITY_COLOR);
		g2d.drawString(quantityStr, x, y);
	}

	private void drawConditionIndicator(Graphics2D g2d)
	{
		String symbol = itemState.getQuantityCondition().getSymbol();

		g2d.setFont(FontManager.getRunescapeSmallFont());
		FontMetrics fm = g2d.getFontMetrics();

		int x = 2;
		int y = fm.getHeight();

		g2d.setColor(QUANTITY_SHADOW);
		g2d.drawString(symbol, x + 1, y + 1);

		g2d.setColor(Color.WHITE);
		g2d.drawString(symbol, x, y);
	}

	private void drawLockIcon(Graphics2D g2d)
	{
		int x = getWidth() - LOCK_ICON_SIZE - 2;
		int y = 2;

		g2d.setColor(QUANTITY_SHADOW);
		g2d.fillRect(x + 1, y + 1, LOCK_ICON_SIZE, LOCK_ICON_SIZE);

		g2d.setColor(Color.WHITE);
		g2d.fillRect(x, y, LOCK_ICON_SIZE, LOCK_ICON_SIZE);

		g2d.setColor(QUANTITY_SHADOW);
		g2d.fillRect(x + 2, y + 2, LOCK_ICON_SIZE - 4, LOCK_ICON_SIZE - 4);
	}

	private String formatQuantity(int quantity)
	{
		if (quantity >= 10_000_000)
		{
			return (quantity / 1_000_000) + "M";
		}
		else if (quantity >= 100_000)
		{
			return (quantity / 1_000) + "K";
		}
		else
		{
			return String.valueOf(quantity);
		}
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
				repaint();
			},
			() -> {
				itemState = null;
				itemImage = null;
				parentGrid.removeItem(slotIndex);
				repaint();
			}
		);

		menu.show(this, e.getX(), e.getY());
	}

	private class ItemSlotMouseAdapter extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			if (SwingUtilities.isLeftMouseButton(e) && itemState != null && itemState.getItemId() > 0)
			{
				dragSource = ItemSlotBox.this;
			}
			else if (SwingUtilities.isRightMouseButton(e))
			{
				showContextMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (SwingUtilities.isLeftMouseButton(e) && dragSource != null)
			{
				if (dragSource != ItemSlotBox.this)
				{
					parentGrid.swapItems(dragSource.slotIndex, ItemSlotBox.this.slotIndex);
				}

				dragSource = null;
				isDragTarget = false;
				repaint();
			}
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			if (dragSource != null && dragSource != ItemSlotBox.this)
			{
				isDragTarget = true;
				repaint();
			}
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			if (isDragTarget)
			{
				isDragTarget = false;
				repaint();
			}
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			if (dragSource == ItemSlotBox.this)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}
	}
}
