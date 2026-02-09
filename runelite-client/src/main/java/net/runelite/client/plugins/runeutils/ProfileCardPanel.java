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
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.DARK_GRAY_COLOR, 1),
			new EmptyBorder(5, 5, 5, 5)
		));

		add(buildHeaderPanel());
		contentPanel = buildContentPanel();
		add(contentPanel);

		updateItemCount();
	}

	private JPanel buildHeaderPanel()
	{
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BorderLayout(5, 0));
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		leftPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		toggleButton = new JButton(EXPAND_ICON);
		toggleButton.setFont(FontManager.getRunescapeSmallFont());
		toggleButton.setFocusPainted(false);
		toggleButton.setPreferredSize(new Dimension(30, 25));
		toggleButton.addActionListener(e -> toggleExpanded());
		leftPanel.add(toggleButton);

		nameLabel = new JLabel(profile.getDisplayName());
		nameLabel.setFont(FontManager.getRunescapeBoldFont());
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

		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		rightPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		enableButton = new JToggleButton(profile.isEnabled() ? "Enabled" : "Disabled");
		enableButton.setSelected(profile.isEnabled());
		enableButton.setFont(FontManager.getRunescapeSmallFont());
		enableButton.setFocusPainted(false);
		enableButton.setPreferredSize(new Dimension(80, 25));
		enableButton.addActionListener(e -> {
			profile.setEnabled(enableButton.isSelected());
			enableButton.setText(profile.isEnabled() ? "Enabled" : "Disabled");
			notifyDataChanged();
		});
		rightPanel.add(enableButton);

		JButton deleteButton = new JButton("Delete");
		deleteButton.setFont(FontManager.getRunescapeSmallFont());
		deleteButton.setForeground(Color.RED);
		deleteButton.setFocusPainted(false);
		deleteButton.setPreferredSize(new Dimension(70, 25));
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
		panel.setBorder(new EmptyBorder(5, 0, 0, 0));

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
			// Use BorderLayout.CENTER to let the grid fill available space
			JPanel wrapperPanel = new JPanel(new BorderLayout());
			wrapperPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
			wrapperPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
			wrapperPanel.add(gridPanel, BorderLayout.CENTER);
			panel.add(wrapperPanel, BorderLayout.CENTER);
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
		String newName = JOptionPane.showInputDialog(
			this,
			"Enter new profile name:",
			profile.getName()
		);

		if (newName != null && !newName.trim().isEmpty())
		{
			profile.setName(newName.trim());
			nameLabel.setText(profile.getDisplayName());
			notifyDataChanged();
		}
	}

	private void confirmDelete()
	{
		int result = JOptionPane.showConfirmDialog(
			this,
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
