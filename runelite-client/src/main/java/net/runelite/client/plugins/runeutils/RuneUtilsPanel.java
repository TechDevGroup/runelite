/*
 * Copyright (c) 2024, TechDevGroup
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.runeutils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.ColorScheme;

/**
 * Configuration panel for Rune Utils plugin
 */
public class RuneUtilsPanel extends PluginPanel
{
	private final JTextArea logArea;
	private final JPanel profilesPanel;
	private final List<ItemProfile> profiles;

	@Inject
	public RuneUtilsPanel(RuneUtilsConfig config)
	{
		super(false);
		this.profiles = new ArrayList<>();

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Main content panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Title
		JLabel titleLabel = new JLabel("Rune Utils Configuration");
		titleLabel.setForeground(ColorScheme.BRAND_ORANGE);
		mainPanel.add(titleLabel);

		// Profiles section
		JLabel profilesLabel = new JLabel("Item Filter Profiles:");
		profilesLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
		mainPanel.add(profilesLabel);

		profilesPanel = new JPanel();
		profilesPanel.setLayout(new BoxLayout(profilesPanel, BoxLayout.Y_AXIS));
		profilesPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		mainPanel.add(profilesPanel);

		// Add profile button
		JButton addProfileButton = new JButton("Add Profile");
		addProfileButton.addActionListener(e -> addNewProfile());
		mainPanel.add(addProfileButton);

		// Log area
		JLabel logLabel = new JLabel("Activity Log:");
		logLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
		mainPanel.add(logLabel);

		logArea = new JTextArea(5, 20);
		logArea.setEditable(false);
		logArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		logArea.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		JScrollPane logScrollPane = new JScrollPane(logArea);
		logScrollPane.setPreferredSize(new Dimension(0, 100));
		mainPanel.add(logScrollPane);

		add(mainPanel, BorderLayout.CENTER);

		// Add initial profile
		addNewProfile();
	}

	private void addNewProfile()
	{
		ItemProfile profile = new ItemProfile();
		profiles.add(profile);

		JPanel profilePanel = new JPanel();
		profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
		profilePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		profilePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		// Profile name
		JPanel namePanel = new JPanel(new BorderLayout());
		namePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		namePanel.add(new JLabel("Profile Name:"), BorderLayout.WEST);
		JTextField nameField = new JTextField(profile.getName());
		nameField.addActionListener(e -> profile.setName(nameField.getText()));
		namePanel.add(nameField, BorderLayout.CENTER);
		profilePanel.add(namePanel);

		// Item filter
		JPanel filterPanel = new JPanel(new BorderLayout());
		filterPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		filterPanel.add(new JLabel("Item Filter (comma separated):"), BorderLayout.NORTH);
		JTextField filterField = new JTextField(profile.getFilter());
		filterField.addActionListener(e -> profile.setFilter(filterField.getText()));
		filterPanel.add(filterField, BorderLayout.CENTER);
		profilePanel.add(filterPanel);

		// Enable checkbox and remove button
		JPanel controlPanel = new JPanel(new BorderLayout());
		controlPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JButton enableButton = new JButton(profile.isEnabled() ? "Enabled" : "Disabled");
		enableButton.addActionListener(e -> {
			profile.setEnabled(!profile.isEnabled());
			enableButton.setText(profile.isEnabled() ? "Enabled" : "Disabled");
		});
		controlPanel.add(enableButton, BorderLayout.WEST);

		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(e -> {
			profiles.remove(profile);
			profilesPanel.remove(profilePanel);
			profilesPanel.revalidate();
			profilesPanel.repaint();
		});
		controlPanel.add(removeButton, BorderLayout.EAST);
		profilePanel.add(controlPanel);

		profilesPanel.add(profilePanel);
		profilesPanel.revalidate();
		profilesPanel.repaint();
	}

	public void log(String message)
	{
		logArea.append(message + "\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	public List<ItemProfile> getProfiles()
	{
		return profiles;
	}
}
