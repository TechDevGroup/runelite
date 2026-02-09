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
package net.runelite.client.plugins.uicomponents.components;

import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.uicomponents.base.AbstractConfigurableComponent;
import net.runelite.client.plugins.uicomponents.config.DevConsoleConfig;

/**
 * Dev Console component for displaying logs in-app.
 * Follows config-first principle and SOLID design.
 *
 * Single Responsibility: Display and manage log messages
 * Open/Closed: Extensible through configuration, closed for modification
 * Dependency Inversion: Depends on abstractions (ComponentConfig)
 */
@Slf4j
public class DevConsoleComponent extends AbstractConfigurableComponent<DevConsoleConfig>
{
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	private final ConcurrentLinkedDeque<String> logBuffer = new ConcurrentLinkedDeque<>();
	private JTextArea textArea;
	private JScrollPane scrollPane;

	public DevConsoleComponent(DevConsoleConfig config)
	{
		super(config);
	}

	@Override
	protected JComponent buildComponent()
	{
		JPanel panel = new JPanel(new BorderLayout());

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font(config.getFontFamily(), Font.PLAIN, config.getFontSize()));
		textArea.setBackground(config.getBackgroundColor());
		textArea.setForeground(config.getTextColor());
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	@Override
	protected void refreshInternal()
	{
		if (textArea != null)
		{
			textArea.setFont(new Font(config.getFontFamily(), Font.PLAIN, config.getFontSize()));
			textArea.setBackground(config.getBackgroundColor());
			textArea.setForeground(config.getTextColor());
			rebuildDisplay();
		}
	}

	/**
	 * Logs a message to the console
	 * @param level log level (ERROR, WARN, INFO, DEBUG)
	 * @param message the message to log
	 */
	public void log(LogLevel level, String message)
	{
		StringBuilder logLine = new StringBuilder();

		if (config.isShowTimestamp())
		{
			logLine.append('[').append(LocalDateTime.now().format(TIME_FORMATTER)).append("] ");
		}

		if (config.isShowLogLevel())
		{
			logLine.append('[').append(level.name()).append("] ");
		}

		logLine.append(message);

		addLogLine(logLine.toString(), level);
	}

	/**
	 * Logs an info message
	 * @param message the message
	 */
	public void info(String message)
	{
		log(LogLevel.INFO, message);
	}

	/**
	 * Logs a debug message
	 * @param message the message
	 */
	public void debug(String message)
	{
		log(LogLevel.DEBUG, message);
	}

	/**
	 * Logs a warning message
	 * @param message the message
	 */
	public void warn(String message)
	{
		log(LogLevel.WARN, message);
	}

	/**
	 * Logs an error message
	 * @param message the message
	 */
	public void error(String message)
	{
		log(LogLevel.ERROR, message);
	}

	/**
	 * Clears all logs
	 */
	public void clear()
	{
		logBuffer.clear();
		if (textArea != null)
		{
			SwingUtilities.invokeLater(() -> textArea.setText(""));
		}
	}

	private void addLogLine(String line, LogLevel level)
	{
		logBuffer.addLast(line);

		// Trim buffer if it exceeds max lines
		while (logBuffer.size() > config.getMaxLines())
		{
			logBuffer.removeFirst();
		}

		if (textArea != null)
		{
			SwingUtilities.invokeLater(() ->
			{
				textArea.append(line + "\n");

				if (config.isAutoscroll())
				{
					textArea.setCaretPosition(textArea.getDocument().getLength());
				}
			});
		}
	}

	private void rebuildDisplay()
	{
		if (textArea != null)
		{
			textArea.setText("");
			for (String line : logBuffer)
			{
				textArea.append(line + "\n");
			}

			if (config.isAutoscroll())
			{
				textArea.setCaretPosition(textArea.getDocument().getLength());
			}
		}
	}

	public enum LogLevel
	{
		ERROR,
		WARN,
		INFO,
		DEBUG
	}
}
