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
package net.runelite.client.plugins.uicomponents.layouts;

import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import lombok.Builder;
import net.runelite.client.ui.ColorScheme;

/**
 * Grid layout builder.
 * Useful for creating uniform grid-based UIs like skill displays.
 *
 * Fluent API for building grid layouts with configurable rows, columns, and gaps.
 */
public class GridPanelLayout implements PanelLayout
{
	private final JPanel panel;
	private final int rows;
	private final int columns;

	@Builder
	public GridPanelLayout(
		Integer rows,
		Integer columns,
		Integer hgap,
		Integer vgap,
		Border border)
	{
		this.rows = rows != null ? rows : 0;
		this.columns = columns != null ? columns : 3;

		int horizontalGap = hgap != null ? hgap : 5;
		int verticalGap = vgap != null ? vgap : 5;

		this.panel = new JPanel();
		this.panel.setLayout(new GridLayout(this.rows, this.columns, horizontalGap, verticalGap));
		this.panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		if (border != null)
		{
			panel.setBorder(border);
		}
		else
		{
			panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		}
	}

	public static GridPanelLayout create(int columns)
	{
		return builder().columns(columns).build();
	}

	public static GridPanelLayout create(int rows, int columns)
	{
		return builder().rows(rows).columns(columns).build();
	}

	public static GridPanelLayoutBuilder builder()
	{
		return new GridPanelLayoutBuilder();
	}

	@Override
	public PanelLayout add(JComponent component)
	{
		panel.add(component);
		return this;
	}

	@Override
	public PanelLayout add(JComponent component, Object constraints)
	{
		panel.add(component);
		return this;
	}

	@Override
	public PanelLayout addGap(int size)
	{
		// Grid layout doesn't support dynamic gaps
		// Add empty panel instead
		JPanel spacer = new JPanel();
		spacer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.add(spacer);
		return this;
	}

	@Override
	public PanelLayout addSeparator()
	{
		// Add separator spanning full width
		JSeparator separator = new JSeparator();
		panel.add(separator);
		return this;
	}

	@Override
	public JPanel build()
	{
		return panel;
	}

	@Override
	public JPanel getPanel()
	{
		return panel;
	}
}
