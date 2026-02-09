/*
 * Copyright (c) 2024, YourName <your@email.com>
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
package net.runelite.client.plugins.boilerplate;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * Boilerplate Plugin Example
 *
 * This is a minimal working example of a RuneLite plugin.
 * Use this as a starting template for creating new plugins.
 *
 * Key Components:
 * - @PluginDescriptor: Defines plugin metadata
 * - @Singleton: Ensures single instance
 * - @Slf4j: Provides logging capability
 * - extends Plugin: Required base class
 * - @Inject: Dependency injection
 * - startUp()/shutDown(): Lifecycle methods
 * - @Subscribe: Event handlers
 */
@PluginDescriptor(
	name = "Boilerplate",
	description = "A minimal boilerplate plugin example",
	tags = {"example", "template", "boilerplate"}
)
@Singleton
@Slf4j
public class BoilerplatePlugin extends Plugin
{
	// Injected dependencies
	@Inject
	private Client client;

	@Inject
	private BoilerplateConfig config;

	@Inject
	private ConfigManager configManager;

	/**
	 * Provides the configuration instance
	 * This method is required if your plugin uses a Config interface
	 */
	@Provides
	BoilerplateConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BoilerplateConfig.class);
	}

	/**
	 * Called when the plugin is started/enabled
	 * Initialize resources, register event handlers, etc.
	 */
	@Override
	protected void startUp() throws Exception
	{
		log.info("Boilerplate plugin started!");

		// Initialize your plugin here
		// Examples:
		// - Register overlays: overlayManager.add(myOverlay);
		// - Set up initial state
		// - Load saved data
	}

	/**
	 * Called when the plugin is stopped/disabled
	 * Clean up resources, unregister handlers, etc.
	 */
	@Override
	protected void shutDown() throws Exception
	{
		log.info("Boilerplate plugin stopped!");

		// Clean up your plugin here
		// Examples:
		// - Remove overlays: overlayManager.remove(myOverlay);
		// - Clear cached data
		// - Save state
	}

	/**
	 * Example event handler
	 * Triggered every game tick (approximately 600ms)
	 */
	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Handle game tick events here
		if (config.enableLogging())
		{
			log.debug("Game tick occurred");
		}

		// Example: Get player information
		// Player player = client.getLocalPlayer();
		// if (player != null)
		// {
		//     log.info("Player location: {}", player.getWorldLocation());
		// }
	}

	// Add more event handlers as needed
	// Common events:
	// - onGameStateChanged(GameStateChanged event)
	// - onStatChanged(StatChanged event)
	// - onChatMessage(ChatMessage event)
	// - onMenuOptionClicked(MenuOptionClicked event)
}
