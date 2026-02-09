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

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import lombok.Builder;
import net.runelite.client.ui.ColorScheme;

/**
 * Vertical box layout builder.
 * Common pattern used in RuneLite sidebar panels.
 *
 * Fluent API for building vertical layouts with gaps, separators, and components.
 */
public class VerticalBoxLayout implements PanelLayout
{
	private final JPanel panel;
	private final int defaultGap;
	private final boolean autoGap;

	@Builder
	public VerticalBoxLayout(
		Border border,
		Integer defaultGap,
		Boolean autoGap,
		Integer width)
	{
		this.panel = new JPanel();
		this.panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		this.panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		this.defaultGap = defaultGap != null ? defaultGap : 5;
		this.autoGap = autoGap != null ? autoGap : true;

		if (border != null)
		{
			panel.setBorder(border);
		}
		else
		{
			panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		}

		if (width != null)
		{
			panel.setPreferredSize(new Dimension(width, 0));
		}
	}

	public static VerticalBoxLayout create()
	{
		return builder().build();
	}

	public static VerticalBoxLayoutBuilder builder()
	{
		return new VerticalBoxLayoutBuilder();
	}

	@Override
	public PanelLayout add(JComponent component)
	{
		if (autoGap && panel.getComponentCount() > 0)
		{
			addGap(defaultGap);
		}
		panel.add(component);
		return this;
	}

	@Override
	public PanelLayout add(JComponent component, Object constraints)
	{
		add(component);
		return this;
	}

	@Override
	public PanelLayout addGap(int size)
	{
		panel.add(Box.createVerticalStrut(size));
		return this;
	}

	@Override
	public PanelLayout addSeparator()
	{
		if (panel.getComponentCount() > 0)
		{
			addGap(defaultGap);
		}

		JSeparator separator = new JSeparator();
		separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		panel.add(separator);

		addGap(defaultGap);
		return this;
	}

	/**
	 * Adds flexible glue to push components apart
	 * @return this layout for chaining
	 */
	public VerticalBoxLayout addGlue()
	{
		panel.add(Box.createVerticalGlue());
		return this;
	}

	/**
	 * Adds a fixed height rigid area
	 * @param height height in pixels
	 * @return this layout for chaining
	 */
	public VerticalBoxLayout addRigidArea(int height)
	{
		panel.add(Box.createRigidArea(new Dimension(0, height)));
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
