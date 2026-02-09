/*
 * Copyright (c) 2024, TestAuthor <test@example.com>
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
package net.runelite.client.plugins.testplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import lombok.Getter;
import net.runelite.client.plugins.uicomponents.builders.UIComponents;
import net.runelite.client.plugins.uicomponents.components.DevConsoleComponent;
import net.runelite.client.plugins.uicomponents.components.HTMLComponent;
import net.runelite.client.plugins.uicomponents.config.DevConsoleConfig;
import net.runelite.client.plugins.uicomponents.controls.OverlayControlPanel;
import net.runelite.client.plugins.uicomponents.layouts.VerticalBoxLayout;
import net.runelite.client.plugins.uicomponents.overlay.DockableOverlay;
import net.runelite.client.ui.PluginPanel;

/**
 * Sidebar panel for Test Plugin with dev console.
 * Demonstrates config-first UI component usage with fluent layout builders.
 */
public class TestPluginPanel extends PluginPanel
{
	@Getter
	private final DevConsoleComponent devConsole;

	private JPanel overlayControlsContainer;
	private JCheckBox htmlPanelToggle;
	private Consumer<Boolean> htmlPanelToggleCallback;

	@Inject
	public TestPluginPanel(TestPluginConfig config)
	{
		super(false); // No default padding
		setLayout(new BorderLayout());
		setBorder(null); // Remove border

		// Build UI using fluent layout builder with no padding
		JPanel mainPanel = VerticalBoxLayout.builder()
			.defaultGap(0)
			.autoGap(false)
			.build()
			// Overlay controls section header
			.add(UIComponents.label()
				.text("Overlay Controls")
				.bold()
				.build())
			.getPanel();

		// Overlay controls container (will be populated by plugin)
		overlayControlsContainer = new JPanel();
		overlayControlsContainer.setLayout(new BorderLayout());
		overlayControlsContainer.setBorder(null);
		mainPanel.add(overlayControlsContainer);

		// HTML Panel toggle
		htmlPanelToggle = new JCheckBox("Show HTML Graph Panel");
		htmlPanelToggle.setSelected(false);
		htmlPanelToggle.addActionListener(e -> {
			if (htmlPanelToggleCallback != null)
			{
				htmlPanelToggleCallback.accept(htmlPanelToggle.isSelected());
			}
		});
		mainPanel.add(htmlPanelToggle);

		// Dev Console (config-first)
		DevConsoleConfig consoleConfig = DevConsoleConfig.builder()
			.maxLines(config.consoleMaxLines())
			.showTimestamp(config.consoleShowTimestamp())
			.showLogLevel(true)
			.autoscroll(true)
			.fontSize(config.consoleFontSize())
			.build();

		devConsole = new DevConsoleComponent(consoleConfig);
		JPanel consolePanel = (JPanel) devConsole.getComponent();
		consolePanel.setPreferredSize(new Dimension(0, 300));
		consolePanel.setBorder(null);

		// Control buttons using fluent builder with no padding
		JPanel controlPanel = VerticalBoxLayout.builder()
			.defaultGap(2)
			.autoGap(false)
			.build()
			.add(UIComponents.button()
				.text("Clear Console")
				.onClick(() -> devConsole.clear())
				.build())
			.add(UIComponents.button()
				.text("Test Info")
				.onClick(() -> devConsole.info("Test info message"))
				.build())
			.add(UIComponents.button()
				.text("Test Warning")
				.onClick(() -> devConsole.warn("Test warning message"))
				.build())
			.add(UIComponents.button()
				.text("Show HTML Demo")
				.onClick(this::showHTMLDemo)
				.build())
			.build();
		controlPanel.setBorder(null);

		// Assemble final layout
		add(mainPanel, BorderLayout.NORTH);
		add(consolePanel, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);
	}

	public void logInfo(String message)
	{
		devConsole.info(message);
	}

	public void logDebug(String message)
	{
		devConsole.debug(message);
	}

	public void logWarn(String message)
	{
		devConsole.warn(message);
	}

	public void logError(String message)
	{
		devConsole.error(message);
	}

	/**
	 * Show HTML component demo
	 */
	private void showHTMLDemo()
	{
		// Create rich HTML content with CSS
		String html = "<html><head><style>" +
			"body { margin: 10px; font-family: 'RuneScape', monospace; font-size: 11px; }" +
			"h1 { color: #00FFFF; font-size: 14px; margin: 0 0 10px 0; }" +
			"h2 { color: #FF9900; font-size: 12px; margin: 10px 0 5px 0; }" +
			".info { background-color: rgba(0, 255, 0, 0.1); padding: 5px; margin: 5px 0; }" +
			".warning { background-color: rgba(255, 255, 0, 0.1); padding: 5px; margin: 5px 0; }" +
			".stat { display: block; padding: 3px 5px; background-color: rgba(50, 50, 50, 0.5); margin: 2px 0; }" +
			"</style></head><body>" +
			"<h1>HTML/CSS Component</h1>" +
			"<p>This panel uses <b>HTML and CSS</b> for rendering!</p>" +
			"<div class='info'>" +
			"  <b>✓ Benefits:</b><br/>" +
			"  • Rich text formatting<br/>" +
			"  • CSS styling<br/>" +
			"  • Familiar web syntax<br/>" +
			"  • Less boilerplate" +
			"</div>" +
			"<h2>Styling Examples:</h2>" +
			"<div class='stat'>Attack: <span style='color: #00FF00;'>99</span></div>" +
			"<div class='stat'>Strength: <span style='color: #00FF00;'>99</span></div>" +
			"<div class='warning'>This is a warning!</div>" +
			"<p style='color: #AAAAAA; font-size: 10px;'>Created with UIComponents.html()</p>" +
			"</body></html>";

		HTMLComponent htmlComponent = UIComponents.html()
			.html(html)
			.opaque(false)
			.selectable(true)
			.preferredSize(240, 400)
			.build();

		devConsole.info("HTML demo component shown above console");

		// Add HTML component above console
		JPanel consolePanel = (JPanel) getComponent(1); // Console is at index 1
		remove(consolePanel);
		add(htmlComponent.getComponent(), BorderLayout.CENTER);
		add(consolePanel, BorderLayout.SOUTH);
		consolePanel.setPreferredSize(new Dimension(0, 150));

		revalidate();
		repaint();
	}

	/**
	 * Add overlay control panel to the sidebar
	 */
	public void addOverlayControl(DockableOverlay overlay)
	{
		OverlayControlPanel controlPanel = new OverlayControlPanel(overlay);

		// Use vertical layout for multiple overlay controls
		if (overlayControlsContainer.getComponentCount() == 0)
		{
			overlayControlsContainer.setLayout(new BorderLayout());
			JPanel verticalContainer = VerticalBoxLayout.builder()
				.defaultGap(0)
				.autoGap(false)
				.build()
				.add(controlPanel)
				.getPanel();
			verticalContainer.setBorder(null);
			overlayControlsContainer.add(verticalContainer, BorderLayout.NORTH);
		}
		else
		{
			// Add to existing vertical container
			JPanel verticalContainer = (JPanel) overlayControlsContainer.getComponent(0);
			if (verticalContainer.getLayout() instanceof javax.swing.BoxLayout)
			{
				verticalContainer.add(controlPanel);
			}
		}

		overlayControlsContainer.revalidate();
		overlayControlsContainer.repaint();
	}

	/**
	 * Set callback for HTML panel toggle
	 */
	public void setHTMLPanelToggleCallback(Consumer<Boolean> callback)
	{
		this.htmlPanelToggleCallback = callback;
	}

	/**
	 * Get the current state of HTML panel toggle
	 */
	public boolean isHTMLPanelEnabled()
	{
		return htmlPanelToggle.isSelected();
	}

	/**
	 * Programmatically set the HTML panel toggle state
	 */
	public void setHTMLPanelEnabled(boolean enabled)
	{
		htmlPanelToggle.setSelected(enabled);
	}
}

