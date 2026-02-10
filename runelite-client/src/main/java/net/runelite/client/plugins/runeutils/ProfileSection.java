package net.runelite.client.plugins.runeutils;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Simplified profile section with list layout
 */
public class ProfileSection extends JPanel
{
	private final ProfileState profile;
	private final ItemManager itemManager;
	private final Runnable onDataChanged;
	private final JPanel itemsContainer;
	private JLabel nameLabel;
	private ProfileOrbIndicator orbIndicator;
	private final RuneUtilsPlugin plugin;

	public ProfileSection(ProfileState profile, ItemManager itemManager, Runnable onDataChanged, RuneUtilsPlugin plugin)
	{
		this.profile = profile;
		this.itemManager = itemManager;
		this.onDataChanged = onDataChanged;
		this.plugin = plugin;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Initialize items container first so it can be referenced in collapse button
		itemsContainer = new JPanel();
		itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
		itemsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Header bar
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Left side: collapse arrow + profile name
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		leftPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		// Collapse/expand arrow
		JButton collapseButton = new JButton(profile.isCollapsed() ? "▶" : "▼");
		collapseButton.setFont(new Font("Dialog", Font.PLAIN, 10));
		collapseButton.setPreferredSize(new Dimension(16, 16));
		collapseButton.setFocusPainted(false);
		collapseButton.setBorderPainted(false);
		collapseButton.setContentAreaFilled(false);
		collapseButton.setForeground(Color.WHITE);
		collapseButton.addActionListener(e -> {
			profile.setCollapsed(!profile.isCollapsed());
			collapseButton.setText(profile.isCollapsed() ? "▶" : "▼");
			itemsContainer.setVisible(!profile.isCollapsed());
			onDataChanged.run();
		});
		leftPanel.add(collapseButton);

		// Profile name with right-click menu
		nameLabel = new JLabel(profile.getName());
		nameLabel.setFont(FontManager.getRunescapeBoldFont());
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		nameLabel.setToolTipText("Right-click for options");
		nameLabel.addMouseListener(new java.awt.event.MouseAdapter()
		{
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
				{
					showContextMenu(e);
				}
			}
		});
		leftPanel.add(nameLabel);

		// Container type badge
		JLabel containerBadge = new JLabel("[" + profile.getContainerType().getDisplayName().substring(0, 3).toUpperCase() + "]");
		containerBadge.setFont(FontManager.getRunescapeSmallFont());
		containerBadge.setForeground(getContainerColor(profile.getContainerType()));
		containerBadge.setToolTipText("Container: " + profile.getContainerType().getDisplayName());
		leftPanel.add(containerBadge);

		// Orb indicator
		orbIndicator = new ProfileOrbIndicator(profile.isEnabled(), this::toggleEnabled);
		leftPanel.add(orbIndicator);

		headerPanel.add(leftPanel, BorderLayout.WEST);

		// Right side: delete button only (orb moved to left side)
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		// Delete button
		JButton deleteButton = new JButton("×");
		deleteButton.setFont(new Font("Dialog", Font.BOLD, 18));
		deleteButton.setPreferredSize(new Dimension(20, 20));
		deleteButton.setForeground(Color.RED);
		deleteButton.setFocusPainted(false);
		deleteButton.setToolTipText("Delete profile");
		deleteButton.addActionListener(e -> confirmDelete());
		buttonPanel.add(deleteButton);

		headerPanel.add(buttonPanel, BorderLayout.EAST);
		add(headerPanel, BorderLayout.NORTH);

		// Set initial visibility and add items container
		itemsContainer.setVisible(!profile.isCollapsed());
		add(itemsContainer, BorderLayout.CENTER);

		// Separator at bottom
		JSeparator separator = new JSeparator();
		separator.setForeground(ColorScheme.DARK_GRAY_COLOR);
		add(separator, BorderLayout.SOUTH);

		refreshItems();
	}


	private void refreshItems()
	{
		itemsContainer.removeAll();

		if (profile.getSnapshot() == null || profile.getSnapshot().getAllItemStates().isEmpty())
		{
			JLabel emptyLabel = new JLabel("No items");
			emptyLabel.setFont(FontManager.getRunescapeSmallFont());
			emptyLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			emptyLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
			itemsContainer.add(emptyLabel);
		}
		else
		{
			for (TrackedItemState itemState : profile.getSnapshot().getAllItemStates().values())
			{
				ProfileItemRow row = new ProfileItemRow(
					itemState,
					itemManager,
					updatedState -> onItemUpdate(itemState, updatedState),
					() -> onItemRemove(itemState),
					plugin != null ? plugin.getSlotSelectionState() : null,
					profile
				);
				itemsContainer.add(row);
			}
		}

		itemsContainer.revalidate();
		itemsContainer.repaint();
	}

	private void onItemUpdate(TrackedItemState oldState, TrackedItemState updatedState)
	{
		if (profile.getSnapshot() != null)
		{
			// Remove old state and add updated state
			ContainerSnapshot newSnapshot = new ContainerSnapshot(profile.getSnapshot().getContainerType());

			for (TrackedItemState state : profile.getSnapshot().getAllItemStates().values())
			{
				if (state == oldState)
				{
					newSnapshot.addItemState(updatedState);
				}
				else
				{
					newSnapshot.addItemState(state);
				}
			}

			profile.setSnapshot(newSnapshot);
			refreshItems();
			onDataChanged.run();
		}
	}

	private void onItemRemove(TrackedItemState itemState)
	{
		if (profile.getSnapshot() != null)
		{
			ContainerSnapshot newSnapshot = new ContainerSnapshot(profile.getSnapshot().getContainerType());

			// Copy all items except the one being removed
			for (TrackedItemState state : profile.getSnapshot().getAllItemStates().values())
			{
				if (state != itemState)
				{
					newSnapshot.addItemState(state);
				}
			}

			profile.setSnapshot(newSnapshot);
			refreshItems();
			onDataChanged.run();
		}
	}

	private void showContextMenu(java.awt.event.MouseEvent e)
	{
		ProfileContextMenu menu = new ProfileContextMenu(
			profile,
			this::onProfileUpdate,
			this::confirmDelete
		);
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	private void toggleEnabled()
	{
		profile.setEnabled(!profile.isEnabled());
		orbIndicator.setOrbEnabled(profile.isEnabled());
		onDataChanged.run();
	}

	private void onProfileUpdate(ProfileState updatedProfile)
	{
		nameLabel.setText(updatedProfile.getName());
		orbIndicator.setOrbEnabled(updatedProfile.isEnabled());
		onDataChanged.run();
	}

	private void confirmDelete()
	{
		Component parentWindow = SwingUtilities.getWindowAncestor(this);
		int result = JOptionPane.showConfirmDialog(
			parentWindow,
			"Delete profile \"" + profile.getName() + "\"?",
			"Confirm Delete",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE
		);

		if (result == JOptionPane.YES_OPTION)
		{
			Container parent = getParent();
			if (parent != null)
			{
				parent.remove(this);
				parent.revalidate();
				parent.repaint();
			}
			onDataChanged.run();
		}
	}

	private Color getContainerColor(ContainerType containerType)
	{
		switch (containerType)
		{
			case INVENTORY:
				return new Color(100, 200, 255);
			case BANK:
				return new Color(255, 200, 100);
			case EQUIPMENT:
				return new Color(200, 100, 255);
			case BANK_INVENTORY:
				return new Color(150, 255, 150);
			default:
				return Color.LIGHT_GRAY;
		}
	}
}
