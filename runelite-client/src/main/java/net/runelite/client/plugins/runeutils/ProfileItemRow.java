package net.runelite.client.plugins.runeutils;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Compact row showing single item - alternative layout
 */
public class ProfileItemRow extends JPanel
{
	private final TrackedItemState itemState;
	private final ItemManager itemManager;
	private final Consumer<TrackedItemState> onUpdate;
	private final Runnable onRemove;
	private final SlotSelectionState slotSelectionState;
	private final ProfileState profile;

	public ProfileItemRow(TrackedItemState itemState, ItemManager itemManager, Consumer<TrackedItemState> onUpdate, Runnable onRemove, SlotSelectionState slotSelectionState, ProfileState profile)
	{
		this.itemState = itemState;
		this.itemManager = itemManager;
		this.onUpdate = onUpdate;
		this.onRemove = onRemove;
		this.slotSelectionState = slotSelectionState;
		this.profile = profile;

		setLayout(new BorderLayout(5, 0));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

		// Set fixed height to match icon height (32px) + padding (2+2)
		setPreferredSize(new Dimension(0, 36));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

		// Item icon (32x32)
		JLabel iconLabel = new JLabel();
		iconLabel.setPreferredSize(new Dimension(32, 32));
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);

		AsyncBufferedImage itemImage = itemManager.getImage(
			itemState.getItemId(),
			itemState.getQuantity(),
			false // No quantity on icon, we'll show it in text
		);
		itemImage.addTo(iconLabel);

		add(iconLabel, BorderLayout.WEST);

		// Item info panel
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		// Item name
		JLabel nameLabel = new JLabel(itemState.getItemName());
		nameLabel.setFont(FontManager.getRunescapeSmallFont());
		nameLabel.setForeground(Color.WHITE);
		infoPanel.add(nameLabel);

		// Quantity condition
		if (itemState.getQuantityCondition() != QuantityCondition.ANY)
		{
			String conditionText = itemState.getQuantityCondition().getSymbol() + " " + itemState.getQuantity();
			if (itemState.getQuantityCondition() == QuantityCondition.BETWEEN)
			{
				conditionText += "-" + itemState.getQuantityMax();
			}

			JLabel conditionLabel = new JLabel(conditionText);
			conditionLabel.setFont(FontManager.getRunescapeSmallFont());
			conditionLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			infoPanel.add(conditionLabel);
		}

		add(infoPanel, BorderLayout.CENTER);

		// Delete button
		JButton deleteButton = new JButton("×");
		deleteButton.setFont(new Font("Dialog", Font.BOLD, 16));
		deleteButton.setPreferredSize(new Dimension(24, 24));
		deleteButton.setForeground(Color.RED);
		deleteButton.setFocusPainted(false);
		deleteButton.addActionListener(e -> onRemove.run());
		add(deleteButton, BorderLayout.EAST);

		// Right-click for options
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

		// Hover effect
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
				infoPanel.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				setBackground(ColorScheme.DARKER_GRAY_COLOR);
				infoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
		});
	}

	private void showContextMenu(MouseEvent e)
	{
		ItemContextMenu menu = new ItemContextMenu(
			itemState,
			onUpdate,
			onRemove,
			slotSelectionState,
			profile
		);
		menu.show(this, e.getX(), e.getY());
	}
}
