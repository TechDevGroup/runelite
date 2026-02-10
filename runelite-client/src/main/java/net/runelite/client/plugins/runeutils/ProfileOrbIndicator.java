package net.runelite.client.plugins.runeutils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

/**
 * Visual indicator showing profile enabled state as a filled circle orb
 */
public class ProfileOrbIndicator extends JPanel
{
	private static final int ORB_SIZE = 12;
	private static final Color ENABLED_COLOR = new Color(0, 200, 0);
	private static final Color DISABLED_COLOR = new Color(100, 100, 100);

	private boolean enabled;
	private Runnable onToggle;

	public ProfileOrbIndicator(boolean enabled, Runnable onToggle)
	{
		this.enabled = enabled;
		this.onToggle = onToggle;

		setPreferredSize(new Dimension(ORB_SIZE, ORB_SIZE));
		setOpaque(false);
		setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				handleClick();
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color orbColor = enabled ? ENABLED_COLOR : DISABLED_COLOR;
		g2d.setColor(orbColor);
		g2d.fillOval(0, 0, ORB_SIZE, ORB_SIZE);
	}

	public void setOrbEnabled(boolean enabled)
	{
		if (this.enabled != enabled)
		{
			this.enabled = enabled;
			repaint();
		}
	}

	public boolean isOrbEnabled()
	{
		return enabled;
	}

	private void handleClick()
	{
		if (onToggle != null)
		{
			onToggle.run();
		}
	}
}
