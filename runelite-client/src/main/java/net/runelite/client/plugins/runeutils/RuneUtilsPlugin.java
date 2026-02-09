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

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

/**
 * RuneUtils Plugin - Advanced menu manipulation and inventory highlighting
 *
 * Features:
 * - Right-click menu priority modification with modifier keys
 * - Inventory item highlighting with fuzzy matching
 * - Profile-based filter system
 * - Custom on-screen indicators
 */
@PluginDescriptor(
	name = "Rune Utils",
	description = "Advanced menu manipulation and inventory highlighting utilities",
	tags = {"menu", "inventory", "highlight", "utility"}
)
@Singleton
@Slf4j
public class RuneUtilsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private RuneUtilsConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private RuneUtilsPanel panel;

	@Inject
	private OverlayManager overlayManager;

	private NavigationButton navButton;
	private RuneUtilsOverlay inventoryOverlay;

	@Provides
	RuneUtilsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuneUtilsConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Rune Utils started!");

		// Add sidebar panel
		navButton = NavigationButton.builder()
			.tooltip("Rune Utils")
			.icon(ImageUtil.loadImageResource(getClass(), "/net/runelite/client/plugins/config/config_icon.png"))
			.priority(100)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		// Create and register inventory overlay
		inventoryOverlay = new RuneUtilsOverlay(client, this, panel);
		overlayManager.add(inventoryOverlay);

		panel.log("Plugin started successfully");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Rune Utils stopped!");

		// Remove sidebar panel
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}

		// Remove overlay
		if (inventoryOverlay != null)
		{
			overlayManager.remove(inventoryOverlay);
			inventoryOverlay = null;
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		// Menu manipulation logic
		if (!config.enableMenuManipulation())
		{
			return;
		}

		MenuEntry[] menuEntries = event.getMenuEntries();
		if (menuEntries.length == 0)
		{
			return;
		}

		// Check if shift is pressed
		boolean shiftPressed = client.isKeyPressed(KeyCode.KC_SHIFT);

		if (shiftPressed)
		{
			handleShiftClickPriority(menuEntries);
		}
	}

	private void handleShiftClickPriority(MenuEntry[] menuEntries)
	{
		// Look for deposit options and prioritize based on config
		String priorityOption = config.shiftDepositPriority();

		for (int i = 0; i < menuEntries.length; i++)
		{
			MenuEntry entry = menuEntries[i];
			String option = entry.getOption();

			if (option != null && option.toLowerCase().contains(priorityOption.toLowerCase()))
			{
				// Move this entry to the top (highest priority)
				if (i > 0)
				{
					System.arraycopy(menuEntries, 0, menuEntries, 1, i);
					menuEntries[0] = entry;
				}
				break;
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		// Inventory change detection for highlighting
		// The overlay will handle the actual highlighting
		if (inventoryOverlay != null)
		{
			inventoryOverlay.onInventoryChanged();
		}
	}
}
