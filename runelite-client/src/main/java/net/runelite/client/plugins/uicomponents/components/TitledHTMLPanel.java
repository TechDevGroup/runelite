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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * Titled HTML panel with header and scrollable content.
 * This is a reusable UI component that elegantly resizes both title and content together.
 */
public class TitledHTMLPanel
{
	@Getter
	private final ScrollableHTMLComponent scrollableHTML;

	private final JPanel container;
	private final JLabel titleLabel;
	private final JPanel headerPanel;
	private final JPanel contentPanel;

	public TitledHTMLPanel(String title, HTMLComponent htmlComponent)
	{
		this(title, htmlComponent, ColorScheme.BRAND_ORANGE, true, true);
	}

	public TitledHTMLPanel(String title, HTMLComponent htmlComponent, Color titleColor, boolean verticalScroll, boolean horizontalScroll)
	{
		// Create scrollable HTML component
		scrollableHTML = new ScrollableHTMLComponent(htmlComponent, verticalScroll, horizontalScroll);

		// Create title label
		titleLabel = new JLabel(title);
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(titleColor);
		titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

		// Create header panel with border
		headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(new Color(40, 40, 40, 230));
		headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, titleColor));
		headerPanel.add(titleLabel, BorderLayout.WEST);

		// Create content panel
		contentPanel = new JPanel(new BorderLayout());
		contentPanel.setOpaque(false);
		contentPanel.setBorder(BorderFactory.createEmptyBorder());
		contentPanel.add(scrollableHTML.getComponent(), BorderLayout.CENTER);

		// Create main container with BorderLayout
		container = new JPanel(new BorderLayout());
		container.setOpaque(false);
		container.setBorder(BorderFactory.createEmptyBorder());
		container.add(headerPanel, BorderLayout.NORTH);
		container.add(contentPanel, BorderLayout.CENTER);
	}

	public Component getComponent()
	{
		return container;
	}

	/**
	 * Set the preferred size of the entire panel (title + content)
	 * The content area will automatically adjust to fill remaining space
	 */
	public void setPreferredSize(Dimension size)
	{
		// Set size on container
		container.setPreferredSize(size);
		container.setSize(size);
		container.setBounds(0, 0, size.width, size.height);

		// Force header to layout to get actual height
		int headerPrefHeight = headerPanel.getPreferredSize().height;
		headerPanel.setPreferredSize(new Dimension(size.width, headerPrefHeight));
		headerPanel.setSize(size.width, headerPrefHeight);
		headerPanel.setBounds(0, 0, size.width, headerPrefHeight);
		headerPanel.doLayout();

		// Get actual header height after layout
		int headerHeight = headerPanel.getHeight();
		if (headerHeight == 0)
		{
			headerHeight = headerPrefHeight; // Fallback to preferred height
		}

		// Calculate content area size (subtract header height)
		int contentHeight = Math.max(50, size.height - headerHeight);
		Dimension contentSize = new Dimension(size.width, contentHeight);

		// Set size on content panel
		contentPanel.setPreferredSize(contentSize);
		contentPanel.setSize(contentSize);
		contentPanel.setBounds(0, headerHeight, size.width, contentHeight);

		// Set size on scrollable HTML component (this propagates down to JEditorPane)
		scrollableHTML.setPreferredSize(contentSize);

		// Force complete layout update on entire hierarchy
		contentPanel.doLayout();
		container.doLayout();
		container.validate();
		container.revalidate();
	}

	public void setMinimumSize(Dimension size)
	{
		container.setMinimumSize(size);
	}

	public void setMaximumSize(Dimension size)
	{
		container.setMaximumSize(size);
	}

	/**
	 * Update the title text
	 */
	public void setTitle(String title)
	{
		titleLabel.setText(title);
	}

	/**
	 * Update the title color
	 */
	public void setTitleColor(Color color)
	{
		titleLabel.setForeground(color);
		headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, color));
	}

	/**
	 * Update the title font
	 */
	public void setTitleFont(Font font)
	{
		titleLabel.setFont(font);
	}

	/**
	 * Update HTML content
	 */
	public void setHTML(String html)
	{
		scrollableHTML.setHTML(html);
	}

	/**
	 * Get current HTML content
	 */
	public String getHTML()
	{
		return scrollableHTML.getHTML();
	}

	/**
	 * Add CSS rules
	 */
	public void addCSS(String css)
	{
		scrollableHTML.addCSS(css);
	}

	/**
	 * Get the underlying HTMLComponent
	 */
	public HTMLComponent getHTMLComponent()
	{
		return scrollableHTML.getHtmlComponent();
	}

	/**
	 * Scroll to top of content
	 */
	public void scrollToTop()
	{
		scrollableHTML.scrollToTop();
	}

	/**
	 * Scroll to bottom of content
	 */
	public void scrollToBottom()
	{
		scrollableHTML.scrollToBottom();
	}
}
