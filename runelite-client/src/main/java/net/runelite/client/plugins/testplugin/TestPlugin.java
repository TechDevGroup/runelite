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

import com.google.inject.Provides;
import java.awt.Dimension;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.plugins.uicomponents.components.DraggableHTMLPanel;
import net.runelite.client.plugins.uicomponents.overlay.CalculatorOverlay;
import net.runelite.client.plugins.uicomponents.overlay.CollapseMode;
import net.runelite.client.plugins.uicomponents.overlay.DockableOverlayConfig;
import net.runelite.client.plugins.uicomponents.overlay.HTMLContentProvider;
import net.runelite.client.plugins.uicomponents.overlay.InteractiveOverlayMouseListener;
import net.runelite.client.plugins.uicomponents.overlay.PlayerStatsOverlay;
import net.runelite.client.plugins.uicomponents.overlay.ResourceHTMLContentProvider;
import net.runelite.client.plugins.uicomponents.overlay.StopwatchOverlay;
import net.runelite.client.plugins.uicomponents.overlay.ToggleButtonOverlay;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Test Plugin",
	description = "A simple test plugin to verify setup is working",
	tags = {"test", "demo"}
)
@Singleton
@Slf4j
public class TestPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TestPluginConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private TestPluginPanel panel;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private RuneLiteConfig runeLiteConfig;

	private NavigationButton navButton;
	private HotkeyListener overlayManagementHotkey;
	private StopwatchOverlay stopwatchOverlay;
	private PlayerStatsOverlay playerStatsOverlay;
	private CalculatorOverlay calculatorOverlay;
	private DraggableHTMLPanel htmlGraphPanel;
	private ToggleButtonOverlay htmlToggleOverlay;
	private InteractiveOverlayMouseListener toggleMouseListener;
	private int tickCount = 0;

	@Provides
	TestPluginConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TestPluginConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Test Plugin started!");
		tickCount = 0;

		// Add sidebar panel
		navButton = NavigationButton.builder()
			.tooltip("Test Plugin")
			.icon(ImageUtil.loadImageResource(getClass(), "/net/runelite/client/plugins/config/config_icon.png"))
			.priority(100)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		// Log to in-app console
		panel.logInfo("Plugin started successfully");
		panel.logInfo("Config message: " + config.customMessage());

		// Create and register dockable stopwatch overlay
		DockableOverlayConfig stopwatchConfig = DockableOverlayConfig.builder()
			.title("Stopwatch")
			.position(OverlayPosition.TOP_RIGHT)
			.preferredSize(new Dimension(180, 120))
			.initialCollapseMode(CollapseMode.EXPANDED)
			.collapsible(true)
			.pinnable(true)
			.movable(true)
			.resizable(true)
			.build();

		stopwatchOverlay = new StopwatchOverlay(this, stopwatchConfig);
		stopwatchOverlay.start();
		overlayManager.add(stopwatchOverlay);

		// Create and register player stats overlay (intricate grid layout)
		DockableOverlayConfig statsConfig = DockableOverlayConfig.builder()
			.title("Player Stats")
			.position(OverlayPosition.TOP_LEFT)
			.preferredSize(new Dimension(250, 400))
			.initialCollapseMode(CollapseMode.EXPANDED)
			.collapsible(true)
			.pinnable(true)
			.movable(true)
			.resizable(true)
			.build();

		playerStatsOverlay = new PlayerStatsOverlay(this, client, statsConfig);
		overlayManager.add(playerStatsOverlay);

		// Create and register calculator overlay (grid button layout)
		DockableOverlayConfig calcConfig = DockableOverlayConfig.builder()
			.title("Calculator")
			.position(OverlayPosition.BOTTOM_LEFT)
			.preferredSize(new Dimension(200, 250))
			.initialCollapseMode(CollapseMode.MINIMIZED)
			.collapsible(true)
			.pinnable(false)
			.movable(true)
			.resizable(true)
			.build();

		calculatorOverlay = new CalculatorOverlay(this, calcConfig);
		overlayManager.add(calculatorOverlay);

		// Create draggable HTML panel (not an overlay, standalone Swing component)
		HTMLContentProvider graphContentProvider = new ResourceHTMLContentProvider(
			getClass(),
			"/net/runelite/client/plugins/testplugin/html/graph.html"
		);

		String htmlContent = graphContentProvider.getHTML();
		String cssContent = graphContentProvider.getCSS();

		htmlGraphPanel = new DraggableHTMLPanel(
			"HTML Graph",
			htmlContent,
			cssContent,
			new Dimension(400, 500)
		);

		// Inject HTML panel into canvas parent
		java.awt.Canvas canvas = client.getCanvas();
		java.awt.Container canvasParent = canvas.getParent();
		if (canvasParent != null)
		{
			canvasParent.add(htmlGraphPanel, 0);
			canvasParent.setComponentZOrder(htmlGraphPanel, 0);
			htmlGraphPanel.setLocation(100, 100); // Initial position
			htmlGraphPanel.setVisible(false); // Start hidden
		}

		// Create toggle button overlay
		DockableOverlayConfig toggleConfig = DockableOverlayConfig.builder()
			.title("HTML Toggle")
			.position(OverlayPosition.TOP_CENTER)
			.preferredSize(new Dimension(80, 24))
			.initialCollapseMode(CollapseMode.EXPANDED)
			.collapsible(false)
			.pinnable(true)
			.movable(true)
			.resizable(false)
			.build();

		htmlToggleOverlay = new ToggleButtonOverlay(this, toggleConfig, "HTML");
		htmlToggleOverlay.setToggleCallback(enabled -> {
			// Overlay button controls HTML panel visibility
			if (htmlGraphPanel != null)
			{
				htmlGraphPanel.setVisible(enabled);
				// Keep button state synchronized
				htmlToggleOverlay.updateState(enabled);
			}
		});
		overlayManager.add(htmlToggleOverlay);

		// Register mouse listener for toggle overlay
		toggleMouseListener = new InteractiveOverlayMouseListener(htmlToggleOverlay);
		mouseManager.registerMouseListener(toggleMouseListener);

		// Sidebar checkbox controls overlay button visibility (not HTML panel directly)
		panel.setHTMLPanelToggleCallback(enabled -> {
			if (htmlToggleOverlay != null)
			{
				// Show/hide the overlay button based on sidebar checkbox
				if (enabled)
				{
					overlayManager.add(htmlToggleOverlay);
				}
				else
				{
					overlayManager.remove(htmlToggleOverlay);
					// Also hide HTML panel when overlay button is hidden
					if (htmlGraphPanel != null)
					{
						htmlGraphPanel.setVisible(false);
						htmlToggleOverlay.setEnabled(false);
					}
				}
			}
		});

		// Start with overlay button hidden
		overlayManager.remove(htmlToggleOverlay);

		// Register hotkey listener for overlay management mode (Alt key)
		overlayManagementHotkey = new HotkeyListener(runeLiteConfig::dragHotkey)
		{
			@Override
			public void hotkeyPressed()
			{
				// Enable management mode for HTML panel
				if (htmlGraphPanel != null && htmlGraphPanel.isVisible())
				{
					htmlGraphPanel.setManagementMode(true);
				}
			}

			@Override
			public void hotkeyReleased()
			{
				// Disable management mode for HTML panel
				if (htmlGraphPanel != null)
				{
					htmlGraphPanel.setManagementMode(false);
				}
			}
		};
		keyManager.registerKeyListener(overlayManagementHotkey);

		// Register overlay controls in sidebar
		panel.addOverlayControl(stopwatchOverlay);
		panel.addOverlayControl(playerStatsOverlay);
		panel.addOverlayControl(calculatorOverlay);

		panel.logInfo("Dockable overlays registered:");
		panel.logInfo("- Stopwatch (TOP_RIGHT)");
		panel.logInfo("- Player Stats (TOP_LEFT)");
		panel.logInfo("- Calculator (BOTTOM_LEFT)");
		panel.logInfo("Draggable HTML panel created:");
		panel.logInfo("- Toggle with sidebar checkbox");
		panel.logInfo("- Toggle with overlay button (TOP_CENTER)");
		panel.logInfo("- Drag by title bar to move");
		panel.logInfo("HTML loaded from: /html/graph.html");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Test Plugin stopped!");

		// Remove sidebar panel
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}

		// Remove overlays
		if (stopwatchOverlay != null)
		{
			overlayManager.remove(stopwatchOverlay);
			stopwatchOverlay = null;
		}

		if (playerStatsOverlay != null)
		{
			overlayManager.remove(playerStatsOverlay);
			playerStatsOverlay = null;
		}

		if (calculatorOverlay != null)
		{
			overlayManager.remove(calculatorOverlay);
			calculatorOverlay = null;
		}

		if (htmlGraphPanel != null)
		{
			java.awt.Container parent = htmlGraphPanel.getParent();
			if (parent != null)
			{
				parent.remove(htmlGraphPanel);
				parent.revalidate();
				parent.repaint();
			}
			htmlGraphPanel = null;
		}

		if (htmlToggleOverlay != null)
		{
			overlayManager.remove(htmlToggleOverlay);
			htmlToggleOverlay = null;
		}

		if (toggleMouseListener != null)
		{
			mouseManager.unregisterMouseListener(toggleMouseListener);
			toggleMouseListener = null;
		}

		if (overlayManagementHotkey != null)
		{
			keyManager.unregisterKeyListener(overlayManagementHotkey);
			overlayManagementHotkey = null;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (config.enableTickLogging())
		{
			tickCount++;
			if (tickCount % 10 == 0)
			{
				String message = "Test Plugin: " + tickCount + " ticks counted";
				log.info(message);
				panel.logInfo(message);
			}
		}
	}
}
