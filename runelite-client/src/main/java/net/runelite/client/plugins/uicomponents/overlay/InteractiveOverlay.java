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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import net.runelite.client.plugins.Plugin;

/**
 * Interactive dockable overlay that can handle mouse clicks.
 * Clicks are captured within the overlay bounds instead of falling through to the game.
 *
 * Note: Mouse handling in RuneLite overlays is managed by OverlayRenderer.
 * This class provides helper methods to detect clicks within overlay bounds.
 */
public abstract class InteractiveOverlay extends DockableOverlay
{
	protected InteractiveOverlay(Plugin plugin, DockableOverlayConfig config)
	{
		super(plugin, config);
	}

	/**
	 * Check if a point is within this overlay's bounds
	 */
	protected boolean containsPoint(Point point)
	{
		Rectangle bounds = getBounds();
		return bounds != null && bounds.contains(point);
	}

	/**
	 * Check if a mouse event occurred within this overlay
	 */
	protected boolean containsClick(MouseEvent event)
	{
		return containsPoint(event.getPoint());
	}

	/**
	 * Called when the overlay is clicked.
	 * Override this to handle click events.
	 *
	 * @param point The click point relative to the overlay
	 * @return true if the click was handled (prevents click-through)
	 */
	protected boolean onClicked(Point point)
	{
		return false; // Default: don't consume clicks
	}

	/**
	 * Called when the overlay is right-clicked.
	 * Override this to handle right-click events.
	 *
	 * @param point The click point relative to the overlay
	 * @return true if the click was handled
	 */
	protected boolean onRightClicked(Point point)
	{
		return false; // Default: don't consume clicks
	}

	/**
	 * Called when mouse hovers over the overlay.
	 * Override this to handle hover events.
	 *
	 * @param point The hover point relative to the overlay
	 */
	protected void onHover(Point point)
	{
		// Default: no action
	}
}
