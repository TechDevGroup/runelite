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
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.components.LineComponent;

/**
 * Calculator-style dockable overlay with grid layout.
 * Demonstrates intricate button-grid layout similar to calculator plugin.
 */
public class CalculatorOverlay extends DockableOverlay
{
	private double currentValue = 0;
	private double storedValue = 0;
	private String operation = "";
	private String display = "0";

	public CalculatorOverlay(Plugin plugin, DockableOverlayConfig config)
	{
		super(plugin, config);
	}

	@Override
	protected void renderMinimized(Graphics2D graphics)
	{
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left(display)
			.leftColor(Color.GREEN)
			.build());
	}

	@Override
	protected void renderExpanded(Graphics2D graphics)
	{
		// Display
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Display:")
			.right(display)
			.rightColor(Color.GREEN)
			.build());

		// Calculator grid (4x4 button layout displayed as rows)
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("[ 7 ][ 8 ][ 9 ][ / ]")
			.leftColor(Color.WHITE)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("[ 4 ][ 5 ][ 6 ][ * ]")
			.leftColor(Color.WHITE)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("[ 1 ][ 2 ][ 3 ][ - ]")
			.leftColor(Color.WHITE)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("[ 0 ][ C ][ = ][ + ]")
			.leftColor(Color.WHITE)
			.build());

		// Current operation
		if (!operation.isEmpty())
		{
			getPanelComponent().getChildren().add(LineComponent.builder()
				.left("Op: " + operation)
				.leftColor(Color.YELLOW)
				.build());
		}

		// Instructions
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Right-click to control")
			.leftColor(Color.GRAY)
			.build());
	}

	public void pressNumber(int num)
	{
		if (display.equals("0"))
		{
			display = String.valueOf(num);
		}
		else
		{
			display += num;
		}
		currentValue = Double.parseDouble(display);
	}

	public void setOperation(String op)
	{
		storedValue = currentValue;
		operation = op;
		display = "0";
	}

	public void calculate()
	{
		switch (operation)
		{
			case "+":
				currentValue = storedValue + currentValue;
				break;
			case "-":
				currentValue = storedValue - currentValue;
				break;
			case "*":
				currentValue = storedValue * currentValue;
				break;
			case "/":
				if (currentValue != 0)
				{
					currentValue = storedValue / currentValue;
				}
				break;
		}
		display = String.valueOf(currentValue);
		operation = "";
	}

	public void clear()
	{
		currentValue = 0;
		storedValue = 0;
		operation = "";
		display = "0";
	}
}
