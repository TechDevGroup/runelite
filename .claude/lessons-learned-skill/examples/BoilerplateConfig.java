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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * Boilerplate Config Example
 *
 * Configuration interface for the Boilerplate plugin.
 * All config methods should return default values.
 *
 * The @ConfigGroup annotation must match your plugin name (lowercase).
 * Each @ConfigItem defines a configurable option that appears in the
 * RuneLite settings panel.
 */
@ConfigGroup("boilerplate")
public interface BoilerplateConfig extends Config
{
	/**
	 * Example boolean configuration
	 */
	@ConfigItem(
		keyName = "enableLogging",
		name = "Enable Logging",
		description = "Enables debug logging for this plugin",
		position = 1
	)
	default boolean enableLogging()
	{
		return false;
	}

	/**
	 * Example string configuration
	 */
	@ConfigItem(
		keyName = "customMessage",
		name = "Custom Message",
		description = "A custom message to display",
		position = 2
	)
	default String customMessage()
	{
		return "Hello, RuneLite!";
	}

	/**
	 * Example integer configuration
	 */
	@ConfigItem(
		keyName = "tickInterval",
		name = "Tick Interval",
		description = "Number of ticks between actions",
		position = 3
	)
	default int tickInterval()
	{
		return 10;
	}

	/**
	 * Example enum configuration
	 */
	@ConfigItem(
		keyName = "displayMode",
		name = "Display Mode",
		description = "How to display the information",
		position = 4
	)
	default DisplayMode displayMode()
	{
		return DisplayMode.OVERLAY;
	}

	/**
	 * Example enum for configuration options
	 */
	enum DisplayMode
	{
		OVERLAY,
		INFOBOX,
		CHAT,
		NONE
	}

	// Configuration Tips:
	// - Use clear, descriptive names
	// - Provide helpful descriptions
	// - Order items logically using position parameter
	// - Common types: boolean, String, int, Color, Dimension, enum
	// - Methods return defaults, actual values are stored in ConfigManager
}
