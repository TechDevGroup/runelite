package net.runelite.client.plugins.runeutils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;

/**
 * Configuration panel for Rune Utils plugin
 */
public class RuneUtilsPanel extends PluginPanel
{
	private final List<ProfileState> profileStates;
	private final JPanel profilesContainer;
	private final PluginErrorPanel noProfilesPanel;
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
		profilesContainer.setLayout(new GridBagLayout());
		profilesContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Empty state panel
		noProfilesPanel = new PluginErrorPanel();
		noProfilesPanel.setContent("No Profiles",
			"Right-click an inventory item and select 'Create Profile'");

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

		if (profileStates.isEmpty())
		{
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 1;
			constraints.weighty = 1;
			profilesContainer.add(noProfilesPanel, constraints);
		}
		else
		{
			// Use simplified list layout
			profilesContainer.setLayout(new BoxLayout(profilesContainer, BoxLayout.Y_AXIS));

			for (ProfileState profileState : profileStates)
			{
				ProfileSection section = new ProfileSection(profileState, itemManager, this::onDataChanged, plugin);
				profilesContainer.add(section);
			}

			// Add filler at the bottom
			profilesContainer.add(Box.createVerticalGlue());
		}

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
