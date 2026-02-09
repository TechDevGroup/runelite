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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.components.LineComponent;

/**
 * Interactive graph overlay for displaying time-series data, metrics, and analytics.
 * User can click within the overlay to interact with graph elements.
 *
 * TODO: Implement full graph rendering system
 * - Line graphs, bar charts, area charts
 * - Multiple data series support
 * - Axes with labels and gridlines
 * - Interactive tooltips on hover
 * - Zoom and pan controls
 * - Data point highlighting
 * - Legend/key display
 * - Click handling for data points
 *
 * Minimum dimensions: 300x200
 * Recommended dimensions: 400x250 to 600x400
 * Supports resizing like XP tracker overlays
 */
public class GraphOverlay extends InteractiveOverlay
{
	private int clickCount = 0;
	private String lastClickLocation = "None";

	public GraphOverlay(Plugin plugin, DockableOverlayConfig config)
	{
		super(plugin, config);
	}

	@Override
	protected boolean onClicked(Point point)
	{
		// Handle clicks within the overlay bounds
		clickCount++;
		lastClickLocation = String.format("(%d, %d)", point.x, point.y);
		return true; // Consume click - don't pass through to game
	}

	@Override
	protected boolean onRightClicked(Point point)
	{
		// Handle right-clicks
		lastClickLocation = String.format("Right-click at (%d, %d)", point.x, point.y);
		return true; // Consume click
	}

	@Override
	protected void onHover(Point point)
	{
		// TODO: Show tooltip with data point value
		// For now, just track that we're hovering
	}

	@Override
	protected void renderMinimized(Graphics2D graphics)
	{
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Interactive Graph")
			.right("Clicks: " + clickCount)
			.rightColor(Color.CYAN)
			.build());
	}

	@Override
	protected void renderExpanded(Graphics2D graphics)
	{
		// Header
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Interactive Graph Overlay")
			.leftColor(Color.CYAN)
			.build());

		// Click interaction demo
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Click anywhere in this overlay!")
			.leftColor(Color.YELLOW)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Total clicks:")
			.right(String.valueOf(clickCount))
			.rightColor(Color.GREEN)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Last click:")
			.right(lastClickLocation)
			.rightColor(Color.WHITE)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("")
			.build());

		// Dimensions info
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Dimensions:")
			.leftColor(Color.ORANGE)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Min: 300x200")
			.leftColor(Color.GRAY)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Current: " + getConfig().getPreferredSize().width + "x" + getConfig().getPreferredSize().height)
			.leftColor(Color.WHITE)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Resizable: " + getConfig().isResizable())
			.leftColor(Color.GREEN)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("")
			.build());

		// TODO list
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("TODO - Graph Features:")
			.leftColor(Color.ORANGE)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("• Line/bar/area charts")
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("• Multi-series support")
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("• Axes & gridlines")
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("• Interactive tooltips")
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("• Zoom & pan controls")
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("• Data point selection")
			.build());
	}
}
