package net.runelite.client.plugins.runeutils;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfileCardPanel extends JPanel
{
	private static final String EXPAND_ICON = "▼";
	private static final String COLLAPSE_ICON = "▶";

	private final ItemManager itemManager;
	private final Runnable onDataChanged;

	private ProfileState profile;
	private boolean expanded;

	private JButton toggleButton;
	private JLabel nameLabel;
	private JLabel itemCountLabel;
	private JToggleButton enableButton;
	private JPanel contentPanel;
	private ItemGridPanel gridPanel;

	public ProfileCardPanel(ItemManager itemManager, ProfileState profile, Runnable onDataChanged)
	{
		this.itemManager = itemManager;
		this.profile = profile;
		this.onDataChanged = onDataChanged;
		this.expanded = true;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(BorderFactory.createLineBorder(ColorScheme.DARK_GRAY_COLOR, 1));

		add(buildHeaderPanel());
		contentPanel = buildContentPanel();
		add(contentPanel);

		updateItemCount();
	}

	private JPanel buildHeaderPanel()
	{
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BorderLayout(2, 0));
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		leftPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		toggleButton = new JButton(EXPAND_ICON);
		toggleButton.setFont(FontManager.getRunescapeSmallFont());
		toggleButton.setFocusPainted(false);
		toggleButton.setPreferredSize(new Dimension(20, 20));
		toggleButton.addActionListener(e -> toggleExpanded());
		leftPanel.add(toggleButton);

		// Use HTML for text wrapping in label
		String displayName = profile.getDisplayName();
		nameLabel = new JLabel("<html><div style='width:100px'>" + displayName + "</div></html>");
		nameLabel.setFont(FontManager.getRunescapeSmallFont());
		nameLabel.setForeground(Color.WHITE);
		nameLabel.addMouseListener(new java.awt.event.MouseAdapter()
		{
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					showNameEditDialog();
				}
			}
		});
		leftPanel.add(nameLabel);

		itemCountLabel = new JLabel();
		itemCountLabel.setFont(FontManager.getRunescapeSmallFont());
		itemCountLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		leftPanel.add(itemCountLabel);

		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
		rightPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		enableButton = new JToggleButton(profile.isEnabled() ? "On" : "Off");
		enableButton.setSelected(profile.isEnabled());
		enableButton.setFont(FontManager.getRunescapeSmallFont());
		enableButton.setFocusPainted(false);
		enableButton.setPreferredSize(new Dimension(40, 20));
		enableButton.addActionListener(e -> {
			profile.setEnabled(enableButton.isSelected());
			enableButton.setText(profile.isEnabled() ? "On" : "Off");
			notifyDataChanged();
		});
		rightPanel.add(enableButton);

		JButton deleteButton = new JButton("X");
		deleteButton.setFont(FontManager.getRunescapeSmallFont());
		deleteButton.setForeground(Color.RED);
		deleteButton.setFocusPainted(false);
		deleteButton.setPreferredSize(new Dimension(20, 20));
		deleteButton.addActionListener(e -> confirmDelete());
		rightPanel.add(deleteButton);

		headerPanel.add(leftPanel, BorderLayout.WEST);
		headerPanel.add(rightPanel, BorderLayout.EAST);

		return headerPanel;
	}

	private JPanel buildContentPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		gridPanel = new ItemGridPanel(
			itemManager,
			profile.getContainerType(),
			this::notifyDataChanged
		);
		gridPanel.setSnapshot(profile.getSnapshot());

		// Different layout based on container type
		if (profile.getContainerType() == ContainerType.BANK)
		{
			JScrollPane scrollPane = new JScrollPane(gridPanel);
			scrollPane.setPreferredSize(new Dimension(0, 400));
			scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);
			panel.add(scrollPane, BorderLayout.CENTER);
		}
		else
		{
			// Grid fills available width, SquareGridLayout handles aspect ratio
			panel.add(gridPanel, BorderLayout.NORTH);
		}

		return panel;
	}

	private void toggleExpanded()
	{
		expanded = !expanded;
		contentPanel.setVisible(expanded);
		toggleButton.setText(expanded ? EXPAND_ICON : COLLAPSE_ICON);
		revalidate();
		repaint();
	}

	private void showNameEditDialog()
	{
		Component parentWindow = SwingUtilities.getWindowAncestor(this);
		String newName = JOptionPane.showInputDialog(
			parentWindow,
			"Enter new profile name:",
			profile.getName()
		);

		if (newName != null && !newName.trim().isEmpty())
		{
			profile.setName(newName.trim());
			nameLabel.setText("<html><div style='width:100px'>" + profile.getDisplayName() + "</div></html>");
			notifyDataChanged();
		}
	}

	private void confirmDelete()
	{
		Component parentWindow = SwingUtilities.getWindowAncestor(this);
		int result = JOptionPane.showConfirmDialog(
			parentWindow,
			"Are you sure you want to delete profile \"" + profile.getName() + "\"?",
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
			notifyDataChanged();
		}
	}

	private void updateItemCount()
	{
		int itemCount = profile.getItemCount();
		itemCountLabel.setText("(" + itemCount + " items)");
	}

	private void notifyDataChanged()
	{
		updateItemCount();

		// Update profile snapshot from grid
		if (gridPanel != null)
		{
			ContainerSnapshot snapshot = gridPanel.getSnapshot();
			if (snapshot != null)
			{
				profile.setSnapshot(snapshot);
			}
		}

		if (onDataChanged != null)
		{
			onDataChanged.run();
		}
	}

	public ProfileState getProfile()
	{
		// Sync snapshot from grid
		if (gridPanel != null)
		{
			ContainerSnapshot snapshot = gridPanel.getSnapshot();
			if (snapshot != null)
			{
				profile.setSnapshot(snapshot);
			}
		}

		return profile;
	}
}
