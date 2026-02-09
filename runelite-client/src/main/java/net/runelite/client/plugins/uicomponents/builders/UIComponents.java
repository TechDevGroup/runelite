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
package net.runelite.client.plugins.uicomponents.builders;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import net.runelite.client.plugins.uicomponents.components.HTMLComponent;
import net.runelite.client.plugins.uicomponents.config.HTMLComponentConfig;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * Fluent API for building common UI components.
 * Config-first approach with sensible defaults.
 *
 * Example:
 * <pre>
 * JLabel title = UIComponents.label()
 *     .text(config.titleText())
 *     .font(FontManager.getRunescapeBoldFont())
 *     .color(Color.WHITE)
 *     .build();
 * </pre>
 */
public final class UIComponents
{
	private UIComponents()
	{
	}

	public static LabelBuilder label()
	{
		return new LabelBuilder();
	}

	public static ButtonBuilder button()
	{
		return new ButtonBuilder();
	}

	public static TextFieldBuilder textField()
	{
		return new TextFieldBuilder();
	}

	public static PanelBuilder panel()
	{
		return new PanelBuilder();
	}

	public static HTMLBuilder html()
	{
		return new HTMLBuilder();
	}

	/**
	 * Fluent label builder
	 */
	public static class LabelBuilder
	{
		private String text = "";
		private Font font = FontManager.getRunescapeFont();
		private Color color = Color.WHITE;
		private int alignment = SwingConstants.LEFT;
		private Dimension size;

		public LabelBuilder text(String text)
		{
			this.text = text;
			return this;
		}

		public LabelBuilder font(Font font)
		{
			this.font = font;
			return this;
		}

		public LabelBuilder color(Color color)
		{
			this.color = color;
			return this;
		}

		public LabelBuilder bold()
		{
			this.font = FontManager.getRunescapeBoldFont();
			return this;
		}

		public LabelBuilder small()
		{
			this.font = FontManager.getRunescapeSmallFont();
			return this;
		}

		public LabelBuilder alignLeft()
		{
			this.alignment = SwingConstants.LEFT;
			return this;
		}

		public LabelBuilder alignCenter()
		{
			this.alignment = SwingConstants.CENTER;
			return this;
		}

		public LabelBuilder alignRight()
		{
			this.alignment = SwingConstants.RIGHT;
			return this;
		}

		public LabelBuilder size(int width, int height)
		{
			this.size = new Dimension(width, height);
			return this;
		}

		public JLabel build()
		{
			JLabel label = new JLabel(text);
			label.setFont(font);
			label.setForeground(color);
			label.setHorizontalAlignment(alignment);

			if (size != null)
			{
				label.setPreferredSize(size);
				label.setMaximumSize(size);
			}

			return label;
		}
	}

	/**
	 * Fluent button builder
	 */
	public static class ButtonBuilder
	{
		private String text = "";
		private Runnable action;
		private Dimension size;
		private boolean enabled = true;

		public ButtonBuilder text(String text)
		{
			this.text = text;
			return this;
		}

		public ButtonBuilder onClick(Runnable action)
		{
			this.action = action;
			return this;
		}

		public ButtonBuilder size(int width, int height)
		{
			this.size = new Dimension(width, height);
			return this;
		}

		public ButtonBuilder enabled(boolean enabled)
		{
			this.enabled = enabled;
			return this;
		}

		public JButton build()
		{
			JButton button = new JButton(text);
			button.setEnabled(enabled);

			if (action != null)
			{
				button.addActionListener(e -> action.run());
			}

			if (size != null)
			{
				button.setPreferredSize(size);
				button.setMaximumSize(size);
			}

			return button;
		}
	}

	/**
	 * Fluent text field builder
	 */
	public static class TextFieldBuilder
	{
		private String text = "";
		private String placeholder = "";
		private int columns = 20;
		private boolean editable = true;

		public TextFieldBuilder text(String text)
		{
			this.text = text;
			return this;
		}

		public TextFieldBuilder placeholder(String placeholder)
		{
			this.placeholder = placeholder;
			return this;
		}

		public TextFieldBuilder columns(int columns)
		{
			this.columns = columns;
			return this;
		}

		public TextFieldBuilder editable(boolean editable)
		{
			this.editable = editable;
			return this;
		}

		public JTextField build()
		{
			JTextField textField = new JTextField(text, columns);
			textField.setEditable(editable);
			textField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			textField.setForeground(Color.WHITE);

			if (!placeholder.isEmpty())
			{
				textField.setToolTipText(placeholder);
			}

			return textField;
		}
	}

	/**
	 * Fluent panel builder
	 */
	public static class PanelBuilder
	{
		private Color background = ColorScheme.DARK_GRAY_COLOR;
		private int borderPadding = 0;

		public PanelBuilder background(Color color)
		{
			this.background = color;
			return this;
		}

		public PanelBuilder padding(int padding)
		{
			this.borderPadding = padding;
			return this;
		}

		public JPanel build()
		{
			JPanel panel = new JPanel();
			panel.setBackground(background);

			if (borderPadding > 0)
			{
				panel.setBorder(BorderFactory.createEmptyBorder(
					borderPadding, borderPadding, borderPadding, borderPadding));
			}

			return panel;
		}
	}

	/**
	 * Fluent HTML component builder
	 * Allows rendering HTML/CSS instead of using Swing components
	 */
	public static class HTMLBuilder
	{
		private String html = "";
		private String css = "";
		private boolean autoLinkHandler = true;
		private boolean editable = false;
		private boolean opaque = false;
		private Color backgroundColor;
		private Dimension preferredSize;
		private Dimension minimumSize;
		private Dimension maximumSize;
		private boolean selectable = true;
		private HTMLComponentConfig.HyperlinkClickHandler hyperlinkHandler;

		public HTMLBuilder html(String html)
		{
			this.html = html;
			return this;
		}

		public HTMLBuilder css(String css)
		{
			this.css = css;
			return this;
		}

		public HTMLBuilder autoLinkHandler(boolean enabled)
		{
			this.autoLinkHandler = enabled;
			return this;
		}

		public HTMLBuilder editable(boolean editable)
		{
			this.editable = editable;
			return this;
		}

		public HTMLBuilder opaque(boolean opaque)
		{
			this.opaque = opaque;
			return this;
		}

		public HTMLBuilder backgroundColor(Color color)
		{
			this.backgroundColor = color;
			return this;
		}

		public HTMLBuilder preferredSize(int width, int height)
		{
			this.preferredSize = new Dimension(width, height);
			return this;
		}

		public HTMLBuilder minimumSize(int width, int height)
		{
			this.minimumSize = new Dimension(width, height);
			return this;
		}

		public HTMLBuilder maximumSize(int width, int height)
		{
			this.maximumSize = new Dimension(width, height);
			return this;
		}

		public HTMLBuilder selectable(boolean selectable)
		{
			this.selectable = selectable;
			return this;
		}

		public HTMLBuilder onHyperlinkClick(HTMLComponentConfig.HyperlinkClickHandler handler)
		{
			this.hyperlinkHandler = handler;
			return this;
		}

		public HTMLComponent build()
		{
			HTMLComponentConfig config = HTMLComponentConfig.builder()
				.html(html)
				.css(css)
				.autoLinkHandler(autoLinkHandler)
				.editable(editable)
				.opaque(opaque)
				.backgroundColor(backgroundColor)
				.preferredSize(preferredSize)
				.minimumSize(minimumSize)
				.maximumSize(maximumSize)
				.selectable(selectable)
				.hyperlinkHandler(hyperlinkHandler)
				.build();

			return new HTMLComponent(config);
		}
	}
}
