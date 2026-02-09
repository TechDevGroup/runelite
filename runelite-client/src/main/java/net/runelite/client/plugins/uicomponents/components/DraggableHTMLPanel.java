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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.uicomponents.builders.UIComponents;

/**
 * Draggable HTML panel that can be shown/hidden via toggle.
 * Themed according to RuneLite color scheme.
 *
 * This is a standalone Swing component that overlays the game canvas
 * and can be freely dragged around by the user.
 */
public class DraggableHTMLPanel extends JPanel
{
	private static final Color TITLE_BAR_COLOR = new Color(40, 40, 40, 240);
	private static final Color CONTENT_BG_COLOR = new Color(30, 30, 30, 220);
	private static final Color TITLE_TEXT_COLOR = new Color(255, 255, 255);
	private static final Color BORDER_COLOR = new Color(70, 70, 70);

	// Snapping constants (matching OverlayRenderer behavior)
	private static final int SNAP_TOLERANCE = 80;
	private static final int PADDING = 5;

	// Management mode visual feedback
	private static final Color TITLE_BAR_MANAGING_COLOR = new Color(80, 80, 40, 240);
	private static final Color BORDER_MANAGING_COLOR = new Color(255, 255, 0);
	private static final Dimension SNAP_CORNER_SIZE = new Dimension(80, 80);
	private static final Color SNAP_CORNER_COLOR = new Color(0, 255, 255, 50);
	private static final Color SNAP_CORNER_ACTIVE_COLOR = new Color(0, 255, 0, 100);

	@Getter
	private String title;

	// Overlay management mode (Alt key drag mode)
	private boolean inManagementMode = false;

	// Borders that maintain consistent dimensions
	private final Border normalBorder;
	private final Border managingBorder;

	private JPanel titleBar;
	private JLabel titleLabel;
	private JPanel contentPanel;
	private ScrollableHTMLComponent scrollableHTML;
	private HTMLComponent htmlComponent;

	// Drag support
	private Point dragOffset;
	private boolean isDragging = false;

	// Management mode callback
	private java.util.function.Consumer<Boolean> managementModeCallback;

	public DraggableHTMLPanel(String title, String htmlContent, String cssContent, Dimension initialSize)
	{
		this.title = title;

		// Create borders with consistent dimensions (2px total)
		normalBorder = new CompoundBorder(
			new LineBorder(BORDER_COLOR, 1),
			new EmptyBorder(1, 1, 1, 1)
		);
		managingBorder = new LineBorder(BORDER_MANAGING_COLOR, 2);

		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(CONTENT_BG_COLOR);
		setBorder(normalBorder);
		setSize(initialSize);
		setPreferredSize(initialSize);

		// Create title bar
		titleBar = new JPanel(new BorderLayout());
		titleBar.setOpaque(true);
		titleBar.setBackground(TITLE_BAR_COLOR);
		titleBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		titleBar.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

		titleLabel = new JLabel(title);
		titleLabel.setForeground(TITLE_TEXT_COLOR);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
		titleBar.add(titleLabel, BorderLayout.CENTER);

		// Build CSS with base styles
		String fullCSS = buildCSS(cssContent);

		// Create HTML content
		htmlComponent = UIComponents.html()
			.html(htmlContent)
			.css(fullCSS)
			.opaque(false)
			.selectable(true)
			.build();

		scrollableHTML = new ScrollableHTMLComponent(
			htmlComponent,
			true,  // vertical scroll
			false  // no horizontal scroll
		);

		contentPanel = new JPanel(new BorderLayout());
		contentPanel.setOpaque(true);
		contentPanel.setBackground(CONTENT_BG_COLOR);

		// Fix ghosting: Make scroll pane and viewport opaque
		scrollableHTML.getScrollPane().setOpaque(true);
		scrollableHTML.getScrollPane().setBackground(CONTENT_BG_COLOR);
		scrollableHTML.getScrollPane().getViewport().setOpaque(true);
		scrollableHTML.getScrollPane().getViewport().setBackground(CONTENT_BG_COLOR);

		// Also make the container opaque
		JComponent scrollComponent = (JComponent) scrollableHTML.getComponent();
		scrollComponent.setOpaque(true);
		scrollComponent.setBackground(CONTENT_BG_COLOR);
		contentPanel.add(scrollComponent, BorderLayout.CENTER);

		// Fix buffer stealing: Ensure editor pane has its own double buffer
		javax.swing.JEditorPane editorPane = htmlComponent.getEditorPane();
		editorPane.setDoubleBuffered(true);

		// Add key listener to HTML component to handle Alt key when focused
		setupKeyListener(editorPane);

		// Assemble panel
		add(titleBar, BorderLayout.NORTH);
		add(contentPanel, BorderLayout.CENTER);

		// Add drag support
		setupDragSupport();
	}

	private String buildCSS(String providedCSS)
	{
		// Base CSS for RuneLite theme with hover effects
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
			"} " +
			".hoverable, button, a, input[type='button'], input[type='submit'] { " +
			"  transition: filter 0.2s ease; " +
			"} " +
			".hoverable:hover, button:hover, a:hover, input[type='button']:hover, input[type='submit']:hover { " +
			"  filter: brightness(0.7); " +
			"  cursor: pointer; " +
			"}";

		// Combine with provided CSS
		if (providedCSS != null && !providedCSS.isEmpty())
		{
			return baseCSS + "\n" + providedCSS;
		}

		return baseCSS;
	}

	private void setupKeyListener(javax.swing.JEditorPane editorPane)
	{
		// Add key listener to handle Alt key when HTML component has focus
		KeyAdapter keyAdapter = new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ALT)
				{
					setManagementMode(true);
				}
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ALT)
				{
					setManagementMode(false);
				}
			}
		};

		editorPane.addKeyListener(keyAdapter);
		// Also add to scroll pane in case it gets focus
		scrollableHTML.getScrollPane().addKeyListener(keyAdapter);
		// Add to this panel as well
		addKeyListener(keyAdapter);
	}

	private void setupDragSupport()
	{
		MouseAdapter dragAdapter = new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				// Only allow dragging in management mode (like overlays)
				if (e.getButton() == MouseEvent.BUTTON1 && inManagementMode)
				{
					dragOffset = e.getPoint();
					isDragging = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (isDragging && dragOffset != null)
				{
					// Apply snapping on release
					Point finalPosition = getLocation();
					Point snappedPosition = getSnappedPosition(finalPosition);
					if (snappedPosition != null)
					{
						setLocation(snappedPosition);
					}
				}
				isDragging = false;
			}

			@Override
			public void mouseDragged(MouseEvent e)
			{
				// Only drag in management mode
				if (isDragging && dragOffset != null && inManagementMode)
				{
					Point currentLocation = getLocation();
					int newX = currentLocation.x + e.getX() - dragOffset.x;
					int newY = currentLocation.y + e.getY() - dragOffset.y;
					setLocation(newX, newY);
				}
			}
		};

		titleBar.addMouseListener(dragAdapter);
		titleBar.addMouseMotionListener(dragAdapter);
	}

	/**
	 * Calculate snap corners based on parent container dimensions
	 */
	private Rectangle[] getSnapCorners()
	{
		Container parent = getParent();
		if (parent == null)
		{
			return new Rectangle[0];
		}

		int parentWidth = parent.getWidth();
		int parentHeight = parent.getHeight();

		// Define snap corner positions matching RuneLite overlay system
		return new Rectangle[] {
			// TOP_LEFT
			new Rectangle(0, 0, SNAP_TOLERANCE, SNAP_TOLERANCE),
			// TOP_CENTER
			new Rectangle(parentWidth / 2 - SNAP_TOLERANCE / 2, 0, SNAP_TOLERANCE, SNAP_TOLERANCE),
			// TOP_RIGHT
			new Rectangle(parentWidth - SNAP_TOLERANCE, 0, SNAP_TOLERANCE, SNAP_TOLERANCE),
			// BOTTOM_LEFT
			new Rectangle(0, parentHeight - SNAP_TOLERANCE, SNAP_TOLERANCE, SNAP_TOLERANCE),
			// BOTTOM_RIGHT
			new Rectangle(parentWidth - SNAP_TOLERANCE, parentHeight - SNAP_TOLERANCE, SNAP_TOLERANCE, SNAP_TOLERANCE)
		};
	}

	/**
	 * Get snapped position if near a snap corner, otherwise return null
	 */
	private Point getSnappedPosition(Point currentPosition)
	{
		Rectangle[] snapCorners = getSnapCorners();
		Container parent = getParent();
		if (parent == null)
		{
			return null;
		}

		int panelWidth = getWidth();
		int panelHeight = getHeight();

		// Check each snap corner
		for (int i = 0; i < snapCorners.length; i++)
		{
			Rectangle corner = snapCorners[i];

			// Check if panel's top-left corner is within snap zone
			if (corner.contains(currentPosition))
			{
				// Snap to corner position with padding
				switch (i)
				{
					case 0: // TOP_LEFT
						return new Point(PADDING, PADDING);
					case 1: // TOP_CENTER
						return new Point((parent.getWidth() - panelWidth) / 2, PADDING);
					case 2: // TOP_RIGHT
						return new Point(parent.getWidth() - panelWidth - PADDING, PADDING);
					case 3: // BOTTOM_LEFT
						return new Point(PADDING, parent.getHeight() - panelHeight - PADDING);
					case 4: // BOTTOM_RIGHT
						return new Point(parent.getWidth() - panelWidth - PADDING, parent.getHeight() - panelHeight - PADDING);
				}
			}
		}

		return null;
	}

	/**
	 * Update HTML content
	 */
	public void setHTMLContent(String html)
	{
		if (scrollableHTML != null)
		{
			scrollableHTML.setHTML(html);
		}
	}

	/**
	 * Update title text
	 */
	public void setTitle(String title)
	{
		this.title = title;
		if (titleLabel != null)
		{
			titleLabel.setText(title);
		}
	}

	/**
	 * Get the scrollable HTML component for advanced control
	 */
	public ScrollableHTMLComponent getScrollableHTML()
	{
		return scrollableHTML;
	}

	/**
	 * Enable or disable overlay management mode (Alt key drag mode)
	 * When enabled, panel can be dragged and shows visual feedback
	 */
	public void setManagementMode(boolean enabled)
	{
		if (this.inManagementMode != enabled)
		{
			this.inManagementMode = enabled;
			updateManagementVisuals();

			// Notify callback if registered
			if (managementModeCallback != null)
			{
				managementModeCallback.accept(enabled);
			}
		}
	}

	/**
	 * Check if panel is in management mode
	 */
	public boolean isInManagementMode()
	{
		return inManagementMode;
	}

	/**
	 * Set callback to be notified when management mode changes
	 */
	public void setManagementModeCallback(java.util.function.Consumer<Boolean> callback)
	{
		this.managementModeCallback = callback;
	}

	/**
	 * Update visual feedback based on management mode
	 */
	private void updateManagementVisuals()
	{
		if (inManagementMode)
		{
			// Show yellow highlight like overlays do
			titleBar.setBackground(TITLE_BAR_MANAGING_COLOR);
			setBorder(managingBorder);
			titleBar.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}
		else
		{
			// Restore normal appearance
			titleBar.setBackground(TITLE_BAR_COLOR);
			setBorder(normalBorder);
			titleBar.setCursor(Cursor.getDefaultCursor());
		}

		// Trigger repaint to show/hide snap corners
		if (getParent() != null)
		{
			getParent().repaint();
		}
		repaint();
	}

	@Override
	public void paint(Graphics g)
	{
		// Draw snap corners first if in management mode
		if (inManagementMode)
		{
			Graphics2D g2d = (Graphics2D) g.create();
			try
			{
				drawSnapCorners(g2d);
			}
			finally
			{
				g2d.dispose();
			}
		}

		// Then draw the panel normally
		super.paint(g);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		// Clear background to prevent ghosting
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		super.paintComponent(g);
	}

	/**
	 * Draw snap corner indicators when in management mode
	 */
	private void drawSnapCorners(Graphics2D g2d)
	{
		Container parent = getParent();
		if (parent == null)
		{
			return;
		}

		Rectangle[] snapCorners = getSnapCorners();
		Point panelLocation = getLocation();

		// Get mouse position relative to parent
		java.awt.PointerInfo pointerInfo = java.awt.MouseInfo.getPointerInfo();
		if (pointerInfo == null)
		{
			return;
		}

		Point mouseScreenPos = pointerInfo.getLocation();
		javax.swing.SwingUtilities.convertPointFromScreen(mouseScreenPos, parent);

		// Translate graphics to parent coordinate system
		g2d.translate(-panelLocation.x, -panelLocation.y);

		// Draw each snap corner
		for (Rectangle corner : snapCorners)
		{
			// Adjust corner position to account for snap corner size offset
			Rectangle adjustedCorner = new Rectangle(
				corner.x + SNAP_CORNER_SIZE.width,
				corner.y + SNAP_CORNER_SIZE.height,
				corner.width,
				corner.height
			);

			// Check if mouse is over this corner
			boolean isActive = adjustedCorner.contains(mouseScreenPos);

			g2d.setColor(isActive ? SNAP_CORNER_ACTIVE_COLOR : SNAP_CORNER_COLOR);
			g2d.fill(adjustedCorner);
		}

		g2d.translate(panelLocation.x, panelLocation.y);
	}
}
