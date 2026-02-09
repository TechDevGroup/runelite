package net.runelite.client.plugins.runeutils;

import java.awt.*;

/**
 * Custom layout manager that maintains square (1:1) aspect ratio for all components
 * and fits them uniformly within the parent container bounds
 */
public class SquareGridLayout implements LayoutManager
{
	private final int rows;
	private final int cols;
	private final int hgap;
	private final int vgap;

	public SquareGridLayout(int rows, int cols, int hgap, int vgap)
	{
		this.rows = rows;
		this.cols = cols;
		this.hgap = hgap;
		this.vgap = vgap;
	}

	@Override
	public void addLayoutComponent(String name, Component comp)
	{
	}

	@Override
	public void removeLayoutComponent(Component comp)
	{
	}

	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		return calculateLayoutSize(parent, 36);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		return calculateLayoutSize(parent, 32);
	}

	private Dimension calculateLayoutSize(Container parent, int baseSize)
	{
		Insets insets = parent.getInsets();
		int width = cols * baseSize + (cols - 1) * hgap + insets.left + insets.right;
		int height = rows * baseSize + (rows - 1) * vgap + insets.top + insets.bottom;
		return new Dimension(width, height);
	}

	@Override
	public void layoutContainer(Container parent)
	{
		Insets insets = parent.getInsets();
		int availableWidth = parent.getWidth() - insets.left - insets.right;

		int totalHGap = (cols - 1) * hgap;
		int totalVGap = (rows - 1) * vgap;

		// Calculate cell size based on available width to fill container
		int cellSize = (availableWidth - totalHGap) / cols;

		// Start from insets, no centering - fill from edge
		int startX = insets.left;
		int startY = insets.top;

		Component[] components = parent.getComponents();
		int componentCount = components.length;

		for (int i = 0; i < componentCount; i++)
		{
			int row = i / cols;
			int col = i % cols;

			int x = startX + col * (cellSize + hgap);
			int y = startY + row * (cellSize + vgap);

			components[i].setBounds(x, y, cellSize, cellSize);
		}
	}
}
