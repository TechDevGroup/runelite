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
import java.awt.Dimension;
import lombok.Builder;
import lombok.Value;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Configuration for dockable overlays.
 * Config-first approach for flexible, reusable overlay modals.
 */
@Value
@Builder
public class DockableOverlayConfig
{
	/**
	 * Title displayed in the overlay header
	 */
	@Builder.Default
	String title = "Overlay";

	/**
	 * Initial collapse mode
	 */
	@Builder.Default
	CollapseMode initialCollapseMode = CollapseMode.EXPANDED;

	/**
	 * Whether the overlay is pinned (stays visible)
	 */
	@Builder.Default
	boolean pinned = false;

	/**
	 * Whether the overlay can be collapsed
	 */
	@Builder.Default
	boolean collapsible = true;

	/**
	 * Whether the overlay can be pinned
	 */
	@Builder.Default
	boolean pinnable = true;

	/**
	 * Whether the overlay can be moved
	 */
	@Builder.Default
	boolean movable = true;

	/**
	 * Whether the overlay can be resized
	 */
	@Builder.Default
	boolean resizable = true;

	/**
	 * Preferred position on screen
	 */
	@Builder.Default
	OverlayPosition position = OverlayPosition.TOP_RIGHT;

	/**
	 * Preferred size when expanded
	 */
	@Builder.Default
	Dimension preferredSize = new Dimension(250, 300);

	/**
	 * Minimum size when resizing
	 */
	@Builder.Default
	Dimension minimumSize = new Dimension(150, 100);

	/**
	 * Background color
	 */
	Color backgroundColor;

	/**
	 * Header background color
	 */
	Color headerColor;

	/**
	 * Title text color
	 */
	@Builder.Default
	Color titleColor = Color.WHITE;

	/**
	 * Whether to show close button
	 */
	@Builder.Default
	boolean showCloseButton = true;

	/**
	 * Priority for rendering order
	 */
	@Builder.Default
	float priority = 0.5f;
}
