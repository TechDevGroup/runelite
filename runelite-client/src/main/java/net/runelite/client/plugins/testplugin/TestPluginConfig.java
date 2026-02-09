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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("testplugin")
public interface TestPluginConfig extends Config
{
	@ConfigItem(
		keyName = "enableTickLogging",
		name = "Enable Tick Logging",
		description = "Log every 10th game tick to console",
		position = 1
	)
	default boolean enableTickLogging()
	{
		return true;
	}

	@ConfigItem(
		keyName = "customMessage",
		name = "Custom Message",
		description = "A custom message for testing",
		position = 2
	)
	default String customMessage()
	{
		return "Hello from Test Plugin!";
	}

	@ConfigItem(
		keyName = "consoleMaxLines",
		name = "Console Max Lines",
		description = "Maximum number of lines to keep in the dev console",
		position = 3
	)
	default int consoleMaxLines()
	{
		return 1000;
	}

	@ConfigItem(
		keyName = "consoleFontSize",
		name = "Console Font Size",
		description = "Font size for the dev console",
		position = 4
	)
	default int consoleFontSize()
	{
		return 12;
	}

	@ConfigItem(
		keyName = "consoleShowTimestamp",
		name = "Show Timestamps",
		description = "Show timestamps in console log messages",
		position = 5
	)
	default boolean consoleShowTimestamp()
	{
		return true;
	}
}
