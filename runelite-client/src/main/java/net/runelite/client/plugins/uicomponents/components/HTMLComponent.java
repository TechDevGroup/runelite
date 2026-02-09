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
package net.runelite.client.plugins.uicomponents.components;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.uicomponents.config.HTMLComponentConfig;

/**
 * HTML rendering component that allows using HTML/CSS instead of Swing components.
 * Based on RuneLite's JRichTextPane pattern but with config-first approach.
 */
@Slf4j
public class HTMLComponent
{
	@Getter
	private final HTMLComponentConfig config;

	private final JEditorPane editorPane;
	private HyperlinkListener linkHandler;

	public HTMLComponent(HTMLComponentConfig config)
	{
		this.config = config;
		this.editorPane = new JEditorPane();

		// Configure editor pane
		editorPane.setContentType("text/html");
		editorPane.setEditable(config.isEditable());
		editorPane.setOpaque(config.isOpaque());
		editorPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		// Enable/disable text selection
		if (!config.isSelectable())
		{
			editorPane.setHighlighter(null);
		}

		// Set background color if provided
		if (config.getBackgroundColor() != null)
		{
			editorPane.setBackground(config.getBackgroundColor());
		}

		// Set sizes if provided
		if (config.getPreferredSize() != null)
		{
			editorPane.setPreferredSize(config.getPreferredSize());
		}
		if (config.getMinimumSize() != null)
		{
			editorPane.setMinimumSize(config.getMinimumSize());
		}
		if (config.getMaximumSize() != null)
		{
			editorPane.setMaximumSize(config.getMaximumSize());
		}

		// Apply custom CSS
		HTMLEditorKit kit = (HTMLEditorKit) editorPane.getEditorKitForContentType("text/html");
		StyleSheet styleSheet = kit.getStyleSheet();

		// Add custom CSS rules if provided
		if (config.getCss() != null && !config.getCss().isEmpty())
		{
			styleSheet.addRule(config.getCss());
		}

		// Set up hyperlink handling
		if (config.getHyperlinkHandler() != null)
		{
			// Custom handler provided
			linkHandler = e ->
			{
				if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType()) && e.getURL() != null)
				{
					config.getHyperlinkHandler().onHyperlinkClicked(e.getURL().toString());
				}
			};
			editorPane.addHyperlinkListener(linkHandler);
		}
		else if (config.isAutoLinkHandler())
		{
			// Default link handler (open in browser)
			linkHandler = e ->
			{
				if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType()) && e.getURL() != null)
				{
					if (Desktop.isDesktopSupported())
					{
						try
						{
							Desktop.getDesktop().browse(e.getURL().toURI());
						}
						catch (URISyntaxException | IOException ex)
						{
							log.warn("Error opening link: " + e.getURL(), ex);
						}
					}
				}
			};
			editorPane.addHyperlinkListener(linkHandler);
		}

		// Set initial HTML content
		editorPane.setText(config.getHtml());
	}

	public Component getComponent()
	{
		return editorPane;
	}

	/**
	 * Update the HTML content dynamically
	 */
	public void setHTML(String html)
	{
		editorPane.setText(html);
		editorPane.setCaretPosition(0); // Scroll to top
	}

	/**
	 * Get the current HTML content
	 */
	public String getHTML()
	{
		return editorPane.getText();
	}

	/**
	 * Add additional CSS rules dynamically
	 */
	public void addCSS(String css)
	{
		HTMLEditorKit kit = (HTMLEditorKit) editorPane.getEditorKitForContentType("text/html");
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule(css);
		// Refresh the display
		editorPane.setText(editorPane.getText());
	}

	/**
	 * Enable or disable hyperlink handling
	 */
	public void setAutoLinkHandlerEnabled(boolean enabled)
	{
		if (linkHandler != null && !enabled)
		{
			editorPane.removeHyperlinkListener(linkHandler);
			linkHandler = null;
		}
		else if (linkHandler == null && enabled)
		{
			linkHandler = e ->
			{
				if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType()) && e.getURL() != null)
				{
					if (Desktop.isDesktopSupported())
					{
						try
						{
							Desktop.getDesktop().browse(e.getURL().toURI());
						}
						catch (URISyntaxException | IOException ex)
						{
							log.warn("Error opening link: " + e.getURL(), ex);
						}
					}
				}
			};
			editorPane.addHyperlinkListener(linkHandler);
		}
	}

	/**
	 * Get the underlying JEditorPane for advanced customization
	 */
	public JEditorPane getEditorPane()
	{
		return editorPane;
	}
}
