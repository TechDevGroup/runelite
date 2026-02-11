package net.runelite.client.plugins.runeutils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

/**
 * Configuration panel for Rune Utils plugin
 */
public class RuneUtilsPanel extends PluginPanel
{
	private final List<ProfileState> profileStates;
	private final JPanel profilesContainer;
	private final JPanel noProfilesPanel;
	private final ItemManager itemManager;
	private Runnable onSaveCallback;
	private RuneUtilsPlugin plugin;

	@Inject
	public RuneUtilsPanel(RuneUtilsConfig config, ItemManager itemManager)
	{
		super(false);
		this.itemManager = itemManager;
		this.profileStates = new ArrayList<>();

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Header - no padding, anchored to edge
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JLabel title = new JLabel("Item Profiles");
		title.setForeground(Color.WHITE);
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setBorder(new EmptyBorder(5, 5, 5, 5));
		headerPanel.add(title, BorderLayout.WEST);

		add(headerPanel, BorderLayout.NORTH);

		// Profiles container with scrollpane
		profilesContainer = new JPanel();
		profilesContainer.setLayout(new BoxLayout(profilesContainer, BoxLayout.Y_AXIS));
		profilesContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Empty state panel — styled like a profile entry, left-anchored
		noProfilesPanel = new JPanel(new BorderLayout());
		noProfilesPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel emptyHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		emptyHeader.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		emptyHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel emptyTitle = new JLabel("No Profiles");
		emptyTitle.setForeground(Color.WHITE);
		emptyTitle.setFont(FontManager.getRunescapeBoldFont());
		emptyHeader.add(emptyTitle);

		noProfilesPanel.add(emptyHeader, BorderLayout.NORTH);

		JLabel emptyHint = new JLabel("Right-click an inventory item and select 'Create Profile'");
		emptyHint.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		emptyHint.setFont(FontManager.getRunescapeSmallFont());
		emptyHint.setBorder(new EmptyBorder(8, 5, 8, 5));
		noProfilesPanel.add(emptyHint, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane(profilesContainer);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

		rebuild();
	}

	public void rebuild()
	{
		// Self-route to EDT if called from WebSocket or game thread
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(this::rebuild);
			return;
		}

		profilesContainer.removeAll();
		profilesContainer.setLayout(new BoxLayout(profilesContainer, BoxLayout.Y_AXIS));

		if (profileStates.isEmpty())
		{
			profilesContainer.add(noProfilesPanel);
		}
		else
		{
			for (ProfileState profileState : profileStates)
			{
				ProfileSection section = new ProfileSection(profileState, itemManager, this::onDataChanged, plugin);
				profilesContainer.add(section);
			}
		}

		// Push content to top
		profilesContainer.add(Box.createVerticalGlue());

		profilesContainer.revalidate();
		profilesContainer.repaint();
	}

	private void onDataChanged()
	{
		rebuild();
		if (onSaveCallback != null)
		{
			onSaveCallback.run();
		}
	}

	public void setSaveCallback(Runnable callback)
	{
		this.onSaveCallback = callback;
	}

	public void setPlugin(RuneUtilsPlugin plugin)
	{
		this.plugin = plugin;
	}

	public RuneUtilsPlugin getPlugin()
	{
		return plugin;
	}

	public void log(String message)
	{
		// Can be used for logging if needed
	}

	public List<ItemProfile> getProfiles()
	{
		// Return empty list for backward compatibility
		return new ArrayList<>();
	}

	public void addProfileState(ProfileState profile)
	{
		profileStates.add(profile);
		onDataChanged();
	}

	public List<ProfileState> getProfileStates()
	{
		return profileStates;
	}
}
