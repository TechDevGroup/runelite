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
package net.runelite.client.plugins.uicomponents.overlay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.uicomponents.builders.UIComponents;
import net.runelite.client.plugins.uicomponents.components.HTMLComponent;
import net.runelite.client.plugins.uicomponents.components.ScrollableHTMLComponent;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;

/**
 * Interactive overlay that renders HTML content on the game canvas.
 * Supports pluggable content providers for filesystem, resources, or dynamic generation.
 *
 * This is a clickable HTML overlay that can display graphs, charts, or any HTML/CSS content
 * directly on top of the game canvas with full interaction support.
 */
public class HTMLOverlay extends InteractiveOverlay
{
	@Getter
	@Setter
	private HTMLContentProvider contentProvider;

	private Client client;
	private ScrollableHTMLComponent scrollableHTML;
	private HTMLComponent htmlComponent;
	private JPanel contentContainer;
	private JPanel titleBar;
	private JPanel htmlContentPanel;
	private boolean componentInjected = false;

	public HTMLOverlay(Plugin plugin, Client client, DockableOverlayConfig config, HTMLContentProvider contentProvider)
	{
		super(plugin, config);
		this.client = client;
		this.contentProvider = contentProvider;
		initializeHTMLPanel();
	}

	private void initializeHTMLPanel()
	{
		// Build HTML content with base CSS
		String htmlContent = contentProvider != null ? contentProvider.getHTML() : "<html><body>No content</body></html>";
		String cssContent = buildCSS();

		// Create HTML component using UI builder
		htmlComponent = UIComponents.html()
			.html(htmlContent)
			.css(cssContent)
			.opaque(false)
			.selectable(true)
			.build();

		// Wrap in scrollable component
		scrollableHTML = new ScrollableHTMLComponent(
			htmlComponent,
			true,  // vertical scroll
			false  // no horizontal scroll - content should wrap
		);

		// Create title bar with label
		titleBar = new JPanel(new BorderLayout());
		titleBar.setOpaque(true);
		titleBar.setBackground(new Color(40, 40, 40, 240));
		titleBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

		JLabel titleLabel = new JLabel(getConfig().getTitle());
		titleLabel.setForeground(getConfig().getTitleColor());
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
		titleBar.add(titleLabel, BorderLayout.CENTER);

		// Create HTML content panel
		htmlContentPanel = new JPanel(new BorderLayout());
		htmlContentPanel.setOpaque(true);
		htmlContentPanel.setBackground(new Color(30, 30, 30, 220));
		htmlContentPanel.add((Component) scrollableHTML.getComponent(), BorderLayout.CENTER);

		// Create main container with title and content
		contentContainer = new JPanel(new BorderLayout());
		contentContainer.setOpaque(false);
		contentContainer.add(titleBar, BorderLayout.NORTH);
		contentContainer.add(htmlContentPanel, BorderLayout.CENTER);

		// Set initial size
		Dimension initialSize = getConfig().getPreferredSize();
		contentContainer.setPreferredSize(initialSize);
		contentContainer.setSize(initialSize);
	}

	private String buildCSS()
	{
		// Default base styles for overlay rendering
		String baseCSS =
			"body { " +
			"  margin: 8px; " +
			"  padding: 0; " +
			"  font-family: 'RuneScape', monospace; " +
			"  font-size: 11px; " +
			"  color: #FFFFFF; " +
			"  background-color: transparent; " +
			"  overflow-x: hidden; " +
			"} " +
			"* { " +
			"  box-sizing: border-box; " +
			"  max-width: 100%; " +
			"}";

		// Add provider CSS if available
		if (contentProvider != null && !contentProvider.getCSS().isEmpty())
		{
			return baseCSS + "\n" + contentProvider.getCSS();
		}

		return baseCSS;
	}

	/**
	 * Update HTML content from provider
	 */
	public void updateContent()
	{
		if (contentProvider != null)
		{
			scrollableHTML.setHTML(contentProvider.getHTML());
		}
	}

	/**
	 * Reload content from provider (useful for file-based providers)
	 */
	public void reloadContent()
	{
		if (contentProvider != null)
		{
			contentProvider.reload();
			updateContent();
		}
	}

	/**
	 * Update overlay size and resize all components elegantly
	 */
	public void setSize(Dimension size)
	{
		scrollableHTML.setPreferredSize(size);
		contentContainer.setPreferredSize(size);
		contentContainer.setSize(size);
		contentContainer.doLayout();
	}

	@Override
	public void setPreferredSize(Dimension size)
	{
		super.setPreferredSize(size);
		// Immediately sync when preferred size changes (e.g., from resize drag)
		// Check for null since this can be called before initializeHTMLPanel()
		if (size != null && contentContainer != null && scrollableHTML != null)
		{
			scrollableHTML.setPreferredSize(size);
			contentContainer.setPreferredSize(size);
			contentContainer.setSize(size);
			contentContainer.doLayout();
		}
	}


	@Override
	public void setBounds(java.awt.Rectangle bounds)
	{
		super.setBounds(bounds);
		// Immediately sync the HTML component when overlay bounds change
		// This ensures resize via Alt+drag works smoothly
		// Check for null since this can be called before initializeHTMLPanel()
		if (bounds != null && contentContainer != null && scrollableHTML != null)
		{
			Dimension size = bounds.getSize();
			scrollableHTML.setPreferredSize(size);
			contentContainer.setPreferredSize(size);
			contentContainer.setSize(size);
			contentContainer.doLayout();
		}
	}

	@Override
	protected boolean onClicked(Point point)
	{
		// Forward click to Swing component for HTML interactivity
		if (contentContainer != null && point != null)
		{
			java.awt.event.MouseEvent mouseEvent = new java.awt.event.MouseEvent(
				contentContainer,
				java.awt.event.MouseEvent.MOUSE_CLICKED,
				System.currentTimeMillis(),
				0,
				point.x,
				point.y,
				1,
				false
			);
			contentContainer.dispatchEvent(mouseEvent);
			return true; // Consume the event
		}
		return false;
	}

	@Override
	protected boolean onRightClicked(Point point)
	{
		// Right-click could trigger a reload
		reloadContent();
		return false; // Allow event to propagate
	}

	@Override
	protected void onHover(Point point)
	{
		// Could show tooltips or highlight elements
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		// Only show overlay when player is logged in and in interactive game state
		GameState gameState = client.getGameState();
		if (gameState != GameState.LOGGED_IN && gameState != GameState.LOADING)
		{
			if (componentInjected)
			{
				contentContainer.setVisible(false);
			}
			return null;
		}

		// Don't render if hidden
		if (getCollapseMode() == CollapseMode.HIDDEN)
		{
			if (componentInjected)
			{
				contentContainer.setVisible(false);
			}
			return null;
		}

		// Handle rendering based on collapse mode
		if (getCollapseMode() == CollapseMode.MINIMIZED)
		{
			renderMinimized(graphics);
		}
		else if (getCollapseMode() == CollapseMode.EXPANDED)
		{
			renderExpanded(graphics);
		}

		// Return size for overlay system
		if (componentInjected && getCollapseMode() == CollapseMode.EXPANDED)
		{
			return contentContainer.getSize();
		}

		return null;
	}

	@Override
	protected void renderMinimized(Graphics2D graphics)
	{
		// Minimized view - hide the component
		if (componentInjected)
		{
			contentContainer.setVisible(false);
		}
	}

	@Override
	protected void renderExpanded(Graphics2D graphics)
	{
		// Check if content has changed
		if (contentProvider != null && contentProvider.hasChanged())
		{
			updateContent();
		}

		// Get overlay bounds as authoritative source
		Rectangle overlayBounds = getBounds();
		if (overlayBounds != null && componentInjected)
		{
			// Inset bounds slightly to leave room for resize handles (3px border)
			Rectangle insetBounds = new Rectangle(
				overlayBounds.x + 3,
				overlayBounds.y + 3,
				overlayBounds.width - 6,
				overlayBounds.height - 6
			);

			// Only update bounds if they've actually changed to avoid scroll reset
			Rectangle currentBounds = contentContainer.getBounds();
			if (!currentBounds.equals(insetBounds))
			{
				// Calculate title bar height
				int titleHeight = titleBar.getPreferredSize().height;

				// Bind Swing component to inset bounds
				contentContainer.setBounds(insetBounds);

				// Update HTML content size (subtract title bar height)
				Dimension htmlSize = new Dimension(
					insetBounds.width,
					insetBounds.height - titleHeight
				);
				scrollableHTML.setPreferredSize(htmlSize);

				contentContainer.revalidate();
				contentContainer.repaint();
			}

			contentContainer.setVisible(getCollapseMode() == CollapseMode.EXPANDED);
		}
		else if (componentInjected)
		{
			contentContainer.setVisible(false);
		}
	}

	/**
	 * Inject the Swing component into the provided container
	 * Call this from the plugin's startUp() method
	 */
	public void injectComponent(java.awt.Container canvasParent)
	{
		if (!componentInjected && canvasParent != null)
		{
			// Add at position 0 to be on top
			canvasParent.add(contentContainer, 0);

			// Explicitly set z-order to ensure it's above the canvas
			canvasParent.setComponentZOrder(contentContainer, 0);

			// Ensure the container doesn't block mouse events to canvas when not visible
			contentContainer.setVisible(false);

			componentInjected = true;
		}
	}

	/**
	 * Remove the injected component
	 * Call this from the plugin's shutDown() method
	 */
	public void removeInjectedComponent()
	{
		if (componentInjected)
		{
			java.awt.Container parent = contentContainer.getParent();
			if (parent != null)
			{
				parent.remove(contentContainer);
				parent.revalidate();
				parent.repaint();
			}
			componentInjected = false;
		}
	}
}
