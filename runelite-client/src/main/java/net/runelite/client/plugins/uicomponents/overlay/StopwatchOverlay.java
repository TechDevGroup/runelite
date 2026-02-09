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
import java.time.Duration;
import java.time.Instant;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.components.LineComponent;

/**
 * Example dockable overlay showing a stopwatch.
 * Demonstrates collapse modes and config-driven behavior.
 */
public class StopwatchOverlay extends DockableOverlay
{
	private Instant startTime;
	private Duration elapsed = Duration.ZERO;
	private boolean running = false;

	public StopwatchOverlay(Plugin plugin, DockableOverlayConfig config)
	{
		super(plugin, config);
	}

	public void start()
	{
		if (!running)
		{
			startTime = Instant.now();
			running = true;
		}
	}

	public void stop()
	{
		if (running)
		{
			elapsed = elapsed.plus(Duration.between(startTime, Instant.now()));
			running = false;
		}
	}

	public void reset()
	{
		elapsed = Duration.ZERO;
		running = false;
	}

	private Duration getCurrentElapsed()
	{
		if (running)
		{
			return elapsed.plus(Duration.between(startTime, Instant.now()));
		}
		return elapsed;
	}

	private String formatDuration(Duration duration)
	{
		long hours = duration.toHours();
		long minutes = duration.toMinutes() % 60;
		long seconds = duration.getSeconds() % 60;

		if (hours > 0)
		{
			return String.format("%d:%02d:%02d", hours, minutes, seconds);
		}
		return String.format("%d:%02d", minutes, seconds);
	}

	@Override
	protected void renderMinimized(Graphics2D graphics)
	{
		// Minimized view: just show time
		Duration current = getCurrentElapsed();
		String timeText = formatDuration(current);

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left(timeText)
			.leftColor(running ? Color.GREEN : Color.WHITE)
			.build());
	}

	@Override
	protected void renderExpanded(Graphics2D graphics)
	{
		Duration current = getCurrentElapsed();
		String timeText = formatDuration(current);

		// Time display
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Elapsed:")
			.right(timeText)
			.rightColor(running ? Color.GREEN : Color.WHITE)
			.build());

		// Status
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Status:")
			.right(running ? "Running" : "Stopped")
			.rightColor(running ? Color.GREEN : Color.GRAY)
			.build());

		// Controls hint
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Right-click for controls")
			.leftColor(Color.GRAY)
			.build());
	}
}
