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
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;

/**
 * Scrollable HTML component with styled scrollbars.
 * Wraps HTMLComponent in a JScrollPane with custom styling.
 */
public class ScrollableHTMLComponent
{
	@Getter
	private final HTMLComponent htmlComponent;

	private final JScrollPane scrollPane;
	private final JPanel container;

	public ScrollableHTMLComponent(HTMLComponent htmlComponent)
	{
		this(htmlComponent, true, true);
	}

	public ScrollableHTMLComponent(HTMLComponent htmlComponent, boolean verticalScroll, boolean horizontalScroll)
	{
		this.htmlComponent = htmlComponent;

		// Create scroll pane with HTML component
		scrollPane = new JScrollPane(htmlComponent.getComponent());
		scrollPane.setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setOpaque(false);

		// Configure scrollbar visibility
		scrollPane.setVerticalScrollBarPolicy(
			verticalScroll ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED : ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
		);
		scrollPane.setHorizontalScrollBarPolicy(
			horizontalScroll ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
		);

		// Style the scrollbars
		styleScrollBar(scrollPane);

		// Create container panel
		container = new JPanel(new BorderLayout());
		container.setOpaque(false);
		container.setBorder(BorderFactory.createEmptyBorder());
		container.add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Style the scrollbars to match RuneLite theme
	 */
	private void styleScrollBar(JScrollPane scrollPane)
	{
		// Vertical scrollbar styling
		scrollPane.getVerticalScrollBar().setOpaque(false);
		scrollPane.getVerticalScrollBar().setBackground(new Color(40, 40, 40));
		scrollPane.getVerticalScrollBar().setForeground(ColorScheme.BRAND_ORANGE);
		scrollPane.getVerticalScrollBar().setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		// Horizontal scrollbar styling
		scrollPane.getHorizontalScrollBar().setOpaque(false);
		scrollPane.getHorizontalScrollBar().setBackground(new Color(40, 40, 40));
		scrollPane.getHorizontalScrollBar().setForeground(ColorScheme.BRAND_ORANGE);
		scrollPane.getHorizontalScrollBar().setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
	}

	public Component getComponent()
	{
		return container;
	}

	public JScrollPane getScrollPane()
	{
		return scrollPane;
	}

	public void setPreferredSize(Dimension size)
	{
		container.setPreferredSize(size);
		container.setSize(size);
		scrollPane.setPreferredSize(size);
		scrollPane.setSize(size);

		// Also size the underlying JEditorPane so it knows how to lay out HTML
		htmlComponent.getEditorPane().setPreferredSize(size);
		htmlComponent.getEditorPane().setSize(size);
	}

	public void setMinimumSize(Dimension size)
	{
		container.setMinimumSize(size);
		scrollPane.setMinimumSize(size);
	}

	public void setMaximumSize(Dimension size)
	{
		container.setMaximumSize(size);
		scrollPane.setMaximumSize(size);
	}

	/**
	 * Update HTML content
	 */
	public void setHTML(String html)
	{
		htmlComponent.setHTML(html);
	}

	/**
	 * Get current HTML content
	 */
	public String getHTML()
	{
		return htmlComponent.getHTML();
	}

	/**
	 * Add CSS rules
	 */
	public void addCSS(String css)
	{
		htmlComponent.addCSS(css);
	}

	/**
	 * Scroll to top
	 */
	public void scrollToTop()
	{
		scrollPane.getVerticalScrollBar().setValue(0);
	}

	/**
	 * Scroll to bottom
	 */
	public void scrollToBottom()
	{
		scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
	}
}
