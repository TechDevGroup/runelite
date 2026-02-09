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
import java.awt.Dimension;
import lombok.Builder;
import lombok.Value;

/**
 * Configuration for HTML rendering component.
 * Allows using HTML/CSS for rich text display instead of Swing components.
 */
@Value
@Builder
public class HTMLComponentConfig
{
	/**
	 * HTML content to display
	 */
	@Builder.Default
	String html = "";

	/**
	 * Custom CSS rules to apply to the HTML content
	 * Example: "body { font-size: 12px; } a { color: #00FF00; }"
	 */
	@Builder.Default
	String css = "";

	/**
	 * Whether to enable automatic hyperlink handling (open links in browser)
	 */
	@Builder.Default
	boolean autoLinkHandler = true;

	/**
	 * Whether the component is editable
	 */
	@Builder.Default
	boolean editable = false;

	/**
	 * Whether the component is opaque (has background)
	 */
	@Builder.Default
	boolean opaque = false;

	/**
	 * Background color (only visible if opaque = true)
	 */
	Color backgroundColor;

	/**
	 * Preferred size of the HTML component
	 */
	Dimension preferredSize;

	/**
	 * Minimum size of the HTML component
	 */
	Dimension minimumSize;

	/**
	 * Maximum size of the HTML component
	 */
	Dimension maximumSize;

	/**
	 * Whether to enable text selection
	 */
	@Builder.Default
	boolean selectable = true;

	/**
	 * Custom hyperlink click handler (overrides autoLinkHandler if provided)
	 */
	HyperlinkClickHandler hyperlinkHandler;

	/**
	 * Functional interface for custom hyperlink handling
	 */
	@FunctionalInterface
	public interface HyperlinkClickHandler
	{
		void onHyperlinkClicked(String url);
	}
}
