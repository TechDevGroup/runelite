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
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.function.Consumer;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.components.LineComponent;

/**
 * Small clickable overlay that acts as a toggle button.
 * Can be used to show/hide other components or trigger actions.
 */
public class ToggleButtonOverlay extends InteractiveOverlay
{
	private static final Color BUTTON_BG_OFF = new Color(60, 60, 60);
	private static final Color BUTTON_BG_ON = new Color(80, 120, 80);
	private static final Color TEXT_COLOR = Color.WHITE;
	private static final int BUTTON_WIDTH = 80;
	private static final int BUTTON_HEIGHT = 50;

	private String buttonText;
	private boolean isEnabled;
	private Consumer<Boolean> toggleCallback;

	public ToggleButtonOverlay(Plugin plugin, DockableOverlayConfig config, String buttonText)
	{
		super(plugin, config);
		this.buttonText = buttonText;
		this.isEnabled = false;

		// Set size for overlay system
		setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));

		// Set panel background color
		getPanelComponent().setBackgroundColor(BUTTON_BG_OFF);
	}

	/**
	 * Set callback to be notified when toggle state changes
	 */
	public void setToggleCallback(Consumer<Boolean> callback)
	{
		this.toggleCallback = callback;
	}

	/**
	 * Get current toggle state
	 */
	public boolean isEnabled()
	{
		return isEnabled;
	}

	/**
	 * Set toggle state (programmatically, without triggering callback)
	 */
	public void setEnabled(boolean enabled)
	{
		this.isEnabled = enabled;
	}

	/**
	 * Update button state to reflect external changes (e.g., HTML panel visibility)
	 */
	public void updateState(boolean state)
	{
		this.isEnabled = state;
	}

	@Override
	protected boolean onClicked(Point point)
	{
		// Toggle state
		isEnabled = !isEnabled;

		// Notify callback
		if (toggleCallback != null)
		{
			toggleCallback.accept(isEnabled);
		}

		return true; // Consume event
	}

	@Override
	protected void renderMinimized(Graphics2D graphics)
	{
		// Show button text even when minimized
		renderExpanded(graphics);
	}

	@Override
	protected void renderExpanded(Graphics2D graphics)
	{
		// Update panel background color based on state
		getPanelComponent().setBackgroundColor(isEnabled ? BUTTON_BG_ON : BUTTON_BG_OFF);

		// Add button text as a line component
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left(buttonText)
			.leftColor(TEXT_COLOR)
			.build());
	}
}
