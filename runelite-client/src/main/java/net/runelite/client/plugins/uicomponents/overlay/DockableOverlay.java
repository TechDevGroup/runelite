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

import java.awt.Dimension;
import java.awt.Graphics2D;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.MenuAction;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Base class for dockable overlays with collapsing, pinning, and advanced layout features.
 * Extends OverlayPanel to leverage existing overlay infrastructure.
 *
 * Features:
 * - Multiple collapse modes (HIDDEN, COLLAPSED, MINIMIZED, EXPANDED)
 * - Pinning to keep overlay visible
 * - Config-driven appearance and behavior
 * - Movable, resizable, snappable
 */
public abstract class DockableOverlay extends OverlayPanel
{
	@Getter
	protected DockableOverlayConfig config;

	@Getter
	@Setter
	protected CollapseMode collapseMode;

	@Getter
	@Setter
	protected boolean isPinned;

	protected DockableOverlay(Plugin plugin, DockableOverlayConfig config)
	{
		super(plugin);
		this.config = config;
		this.collapseMode = config.getInitialCollapseMode();
		this.isPinned = config.isPinned();

		// Apply config settings
		setPosition(config.getPosition());
		setPreferredSize(config.getPreferredSize());
		setResizable(config.isResizable());
		setMovable(config.isMovable());
		setPriority(config.getPriority());

		if (config.getBackgroundColor() != null)
		{
			setPreferredColor(config.getBackgroundColor());
		}

		// Add menu entries for collapse/pin controls
		setupMenuEntries();
	}

	/**
	 * Update the overlay configuration
	 */
	public void updateConfig(DockableOverlayConfig newConfig)
	{
		this.config = newConfig;
		setPosition(config.getPosition());
		setPreferredSize(config.getPreferredSize());
		setResizable(config.isResizable());
		setMovable(config.isMovable());
		setPriority(config.getPriority());

		if (config.getBackgroundColor() != null)
		{
			setPreferredColor(config.getBackgroundColor());
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		// Don't render if hidden
		if (collapseMode == CollapseMode.HIDDEN)
		{
			return null;
		}

		// Clear previous children
		getPanelComponent().getChildren().clear();

		// Render header with title
		if (config.getTitle() != null && !config.getTitle().isEmpty())
		{
			renderHeader(graphics);
		}

		// Render content based on collapse mode
		if (collapseMode == CollapseMode.MINIMIZED)
		{
			renderMinimized(graphics);
		}
		else if (collapseMode == CollapseMode.EXPANDED)
		{
			renderExpanded(graphics);
		}

		return super.render(graphics);
	}

	/**
	 * Render the header with title and controls
	 */
	protected void renderHeader(Graphics2D graphics)
	{
		TitleComponent titleComponent = TitleComponent.builder()
			.text(config.getTitle())
			.color(config.getTitleColor())
			.build();

		getPanelComponent().getChildren().add(titleComponent);
	}

	/**
	 * Render minimized content (compact view)
	 * Override this to provide minimized content
	 */
	protected void renderMinimized(Graphics2D graphics)
	{
		// Default: same as expanded
		renderExpanded(graphics);
	}

	/**
	 * Render expanded content (full view)
	 * Subclasses must implement this
	 */
	protected abstract void renderExpanded(Graphics2D graphics);

	/**
	 * Toggle collapse mode between COLLAPSED and EXPANDED
	 */
	public void toggleCollapse()
	{
		if (collapseMode == CollapseMode.EXPANDED)
		{
			collapseMode = CollapseMode.COLLAPSED;
		}
		else
		{
			collapseMode = CollapseMode.EXPANDED;
		}
	}

	/**
	 * Toggle pin state
	 */
	public void togglePin()
	{
		isPinned = !isPinned;
	}

	/**
	 * Hide the overlay
	 */
	public void hide()
	{
		collapseMode = CollapseMode.HIDDEN;
	}

	/**
	 * Show the overlay
	 */
	public void show()
	{
		if (collapseMode == CollapseMode.HIDDEN)
		{
			collapseMode = config.getInitialCollapseMode();
		}
	}

	/**
	 * Setup right-click menu entries for controls
	 */
	private void setupMenuEntries()
	{
		if (config.isCollapsible())
		{
			getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, "Toggle Collapse", getName()));
		}

		if (config.isPinnable())
		{
			getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, "Toggle Pin", getName()));
		}

		if (config.isShowCloseButton())
		{
			getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, "Hide", getName()));
		}
	}
}
