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
import net.runelite.client.input.MouseAdapter;

/**
 * Mouse listener for interactive overlays.
 * Captures mouse events and routes them to the overlay's click handlers.
 */
public class InteractiveOverlayMouseListener extends MouseAdapter
{
	private final InteractiveOverlay overlay;

	public InteractiveOverlayMouseListener(InteractiveOverlay overlay)
	{
		this.overlay = overlay;
	}

	@Override
	public MouseEvent mouseClicked(MouseEvent event)
	{
		if (!isOverlayVisible())
		{
			return event;
		}

		Rectangle bounds = overlay.getBounds();
		if (bounds == null || !bounds.contains(event.getPoint()))
		{
			return event;
		}

		// Convert to overlay-relative coordinates
		Point relativePoint = new Point(
			event.getX() - bounds.x,
			event.getY() - bounds.y
		);

		boolean consumed = false;

		// Handle left click
		if (event.getButton() == MouseEvent.BUTTON1)
		{
			consumed = overlay.onClicked(relativePoint);
		}
		// Handle right click
		else if (event.getButton() == MouseEvent.BUTTON3)
		{
			consumed = overlay.onRightClicked(relativePoint);
		}

		// Consume the event if the overlay handled it
		if (consumed)
		{
			event.consume();
		}

		return event;
	}

	@Override
	public MouseEvent mousePressed(MouseEvent event)
	{
		if (!isOverlayVisible())
		{
			return event;
		}

		Rectangle bounds = overlay.getBounds();
		if (bounds != null && bounds.contains(event.getPoint()))
		{
			// Consume mouse press to prevent game interaction
			event.consume();
		}

		return event;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent event)
	{
		if (!isOverlayVisible())
		{
			return event;
		}

		Rectangle bounds = overlay.getBounds();
		if (bounds != null && bounds.contains(event.getPoint()))
		{
			// Consume mouse release to prevent game interaction
			event.consume();
		}

		return event;
	}

	@Override
	public MouseEvent mouseMoved(MouseEvent event)
	{
		if (!isOverlayVisible())
		{
			return event;
		}

		Rectangle bounds = overlay.getBounds();
		if (bounds != null && bounds.contains(event.getPoint()))
		{
			// Convert to overlay-relative coordinates
			Point relativePoint = new Point(
				event.getX() - bounds.x,
				event.getY() - bounds.y
			);

			overlay.onHover(relativePoint);
		}

		return event;
	}

	private boolean isOverlayVisible()
	{
		return overlay.getCollapseMode() != CollapseMode.HIDDEN;
	}
}
