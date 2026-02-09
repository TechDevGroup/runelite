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
package net.runelite.client.plugins.uicomponents.config;

import java.awt.Color;
import lombok.Builder;
import lombok.Value;
import net.runelite.client.plugins.uicomponents.base.ComponentConfig;

/**
 * Configuration for Dev Console component.
 * Immutable value object following config-first principle.
 */
@Value
@Builder
public class DevConsoleConfig implements ComponentConfig
{
	@Builder.Default
	int maxLines = 1000;

	@Builder.Default
	boolean showTimestamp = true;

	@Builder.Default
	boolean showLogLevel = true;

	@Builder.Default
	boolean autoscroll = true;

	@Builder.Default
	String fontFamily = "Monospaced";

	@Builder.Default
	int fontSize = 12;

	@Builder.Default
	Color backgroundColor = Color.BLACK;

	@Builder.Default
	Color textColor = Color.GREEN;

	@Builder.Default
	Color errorColor = Color.RED;

	@Builder.Default
	Color warnColor = Color.YELLOW;

	@Builder.Default
	Color infoColor = Color.CYAN;

	@Builder.Default
	Color debugColor = Color.LIGHT_GRAY;

	@Override
	public String getConfigKey()
	{
		return "devconsole";
	}

	@Override
	public boolean isValid()
	{
		return maxLines > 0 && fontSize > 0 && fontFamily != null;
	}
}
