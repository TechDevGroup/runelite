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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

	@Inject
	public RuneUtilsPanel(RuneUtilsConfig config, ItemManager itemManager)
	{
		super(false);
		this.itemManager = itemManager;
		this.profileStates = new ArrayList<>();

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Header
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel title = new JLabel("Item Profiles");
		title.setForeground(Color.WHITE);
		title.setFont(FontManager.getRunescapeBoldFont());
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

	private void rebuild()
	{
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
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 1;
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.insets = new Insets(5, 5, 5, 5);

			// Add ProfileState cards using ProfileCardPanel
			for (ProfileState profileState : profileStates)
			{
				ProfileCardPanel card = new ProfileCardPanel(itemManager, profileState, this::rebuild);
				profilesContainer.add(card, constraints);
				constraints.gridy++;
			}

			// Add filler at the bottom
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weighty = 1;
			JPanel filler = new JPanel();
			filler.setBackground(ColorScheme.DARK_GRAY_COLOR);
			profilesContainer.add(filler, constraints);
		}

		profilesContainer.revalidate();
		profilesContainer.repaint();
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
		rebuild();
	}

	public List<ProfileState> getProfileStates()
	{
		return profileStates;
	}
}
