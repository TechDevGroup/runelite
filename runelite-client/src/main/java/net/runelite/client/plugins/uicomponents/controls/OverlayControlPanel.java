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
package net.runelite.client.plugins.uicomponents.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.plugins.uicomponents.overlay.CollapseMode;
import net.runelite.client.plugins.uicomponents.overlay.DockableOverlay;
import net.runelite.client.ui.ColorScheme;

/**
 * Control panel for managing a dockable overlay from the sidebar.
 * Provides toggles, controls, and configuration UI.
 */
public class OverlayControlPanel extends JPanel
{
	private final DockableOverlay overlay;
	private final JCheckBox visibilityToggle;
	private final JCheckBox pinToggle;
	private final JButton expandButton;
	private final JButton minimizeButton;
	private final JButton collapseButton;

	public OverlayControlPanel(DockableOverlay overlay)
	{
		this.overlay = overlay;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BRAND_ORANGE),
			new EmptyBorder(4, 4, 4, 4)
		));

		// Header with overlay title
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JLabel titleLabel = new JLabel(overlay.getConfig().getTitle());
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(titleLabel.getFont().deriveFont(12f));
		header.add(titleLabel, BorderLayout.WEST);

		// Visibility toggle
		visibilityToggle = new JCheckBox("Show");
		visibilityToggle.setSelected(overlay.getCollapseMode() != CollapseMode.HIDDEN);
		visibilityToggle.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		visibilityToggle.setForeground(Color.WHITE);
		visibilityToggle.addActionListener(e -> toggleVisibility());
		header.add(visibilityToggle, BorderLayout.EAST);

		add(header, BorderLayout.NORTH);

		// Controls panel
		JPanel controlsPanel = new JPanel(new GridLayout(0, 1, 0, 2));
		controlsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		controlsPanel.setBorder(new EmptyBorder(4, 0, 0, 0));

		// Pin toggle (if overlay supports pinning)
		if (overlay.getConfig().isPinnable())
		{
			pinToggle = new JCheckBox("Pinned");
			pinToggle.setSelected(overlay.isPinned());
			pinToggle.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			pinToggle.setForeground(Color.WHITE);
			pinToggle.addActionListener(e -> togglePin());
			controlsPanel.add(pinToggle);
		}
		else
		{
			pinToggle = null;
		}

		// Collapse mode buttons
		if (overlay.getConfig().isCollapsible())
		{
			JPanel modePanel = new JPanel(new GridLayout(1, 3, 2, 0));
			modePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

			expandButton = createModeButton("Expand");
			expandButton.addActionListener(e -> setCollapseMode(CollapseMode.EXPANDED));

			minimizeButton = createModeButton("Minimize");
			minimizeButton.addActionListener(e -> setCollapseMode(CollapseMode.MINIMIZED));

			collapseButton = createModeButton("Collapse");
			collapseButton.addActionListener(e -> setCollapseMode(CollapseMode.COLLAPSED));

			modePanel.add(expandButton);
			modePanel.add(minimizeButton);
			modePanel.add(collapseButton);

			controlsPanel.add(modePanel);
		}
		else
		{
			expandButton = null;
			minimizeButton = null;
			collapseButton = null;
		}

		add(controlsPanel, BorderLayout.CENTER);

		updateButtonStates();
	}

	private JButton createModeButton(String text)
	{
		JButton button = new JButton(text);
		button.setFocusable(false);
		return button;
	}

	private void toggleVisibility()
	{
		SwingUtilities.invokeLater(() ->
		{
			if (visibilityToggle.isSelected())
			{
				overlay.show();
			}
			else
			{
				overlay.hide();
			}
			updateButtonStates();
		});
	}

	private void togglePin()
	{
		SwingUtilities.invokeLater(() ->
		{
			overlay.togglePin();
			if (pinToggle != null)
			{
				pinToggle.setSelected(overlay.isPinned());
			}
		});
	}

	private void setCollapseMode(CollapseMode mode)
	{
		SwingUtilities.invokeLater(() ->
		{
			overlay.setCollapseMode(mode);
			if (mode != CollapseMode.HIDDEN)
			{
				visibilityToggle.setSelected(true);
			}
			updateButtonStates();
		});
	}

	private void updateButtonStates()
	{
		CollapseMode currentMode = overlay.getCollapseMode();

		if (expandButton != null)
		{
			expandButton.setBackground(currentMode == CollapseMode.EXPANDED
				? ColorScheme.BRAND_ORANGE
				: ColorScheme.DARK_GRAY_COLOR);
		}

		if (minimizeButton != null)
		{
			minimizeButton.setBackground(currentMode == CollapseMode.MINIMIZED
				? ColorScheme.BRAND_ORANGE
				: ColorScheme.DARK_GRAY_COLOR);
		}

		if (collapseButton != null)
		{
			collapseButton.setBackground(currentMode == CollapseMode.COLLAPSED
				? ColorScheme.BRAND_ORANGE
				: ColorScheme.DARK_GRAY_COLOR);
		}
	}

	/**
	 * Refresh the control panel to reflect current overlay state
	 */
	public void refresh()
	{
		SwingUtilities.invokeLater(() ->
		{
			visibilityToggle.setSelected(overlay.getCollapseMode() != CollapseMode.HIDDEN);
			if (pinToggle != null)
			{
				pinToggle.setSelected(overlay.isPinned());
			}
			updateButtonStates();
		});
	}
}
