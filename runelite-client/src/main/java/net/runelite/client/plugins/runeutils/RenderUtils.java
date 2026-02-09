package net.runelite.client.plugins.runeutils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.OverlayUtil;

/**
 * Utility class for 3D world coordinate rendering
 * Consolidates common patterns from tile marker, ground items, and other plugins
 */
public class RenderUtils
{
	/**
	 * Renders a tile outline in the game world
	 */
	public static void renderTileOutline(Graphics2D graphics, Client client, LocalPoint location, int height, Color color, int strokeWidth)
	{
		Polygon poly = Perspective.getCanvasTilePoly(client, location, height);
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color, new BasicStroke(strokeWidth));
		}
	}

	/**
	 * Renders a filled tile with outline in the game world
	 */
	public static void renderTileFilled(Graphics2D graphics, Client client, LocalPoint location, int height, Color borderColor, Color fillColor, int strokeWidth)
	{
		Polygon poly = Perspective.getCanvasTilePoly(client, location, height);
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, borderColor, fillColor, new BasicStroke(strokeWidth));
		}
	}

	/**
	 * Renders text at a 3D world location
	 */
	public static void renderTextAtLocation(Graphics2D graphics, Client client, LocalPoint location, String text, int zOffset, Color color, boolean outline)
	{
		Point textPoint = Perspective.getCanvasTextLocation(client, graphics, location, text, zOffset);
		if (textPoint != null)
		{
			if (outline)
			{
				graphics.setColor(Color.BLACK);
				graphics.drawString(text, textPoint.getX() + 1, textPoint.getY() + 1);
			}
			graphics.setColor(color);
			graphics.drawString(text, textPoint.getX(), textPoint.getY());
		}
	}

	/**
	 * Renders a downward-pointing arrow above a tile location
	 */
	public static void renderArrowAtTile(Graphics2D graphics, Client client, LocalPoint location, int height, Color color, int arrowSize)
	{
		Point screenPoint = Perspective.localToCanvas(client, location, client.getPlane(), height + 150);
		if (screenPoint != null)
		{
			GeneralPath arrow = new GeneralPath();

			// Arrow pointing down
			arrow.moveTo(screenPoint.getX(), screenPoint.getY() + arrowSize);
			arrow.lineTo(screenPoint.getX() - arrowSize / 2, screenPoint.getY() - arrowSize / 2);
			arrow.lineTo(screenPoint.getX() + arrowSize / 2, screenPoint.getY() - arrowSize / 2);
			arrow.closePath();

			graphics.setColor(new Color(0, 0, 0, 100));
			graphics.fill(arrow);
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(2));
			graphics.draw(arrow);
		}
	}

	/**
	 * Renders an outline for a TileObject (GameObject, GroundObject, etc.)
	 */
	public static void renderTileObjectOutline(Graphics2D graphics, TileObject tileObject, Color color, int strokeWidth)
	{
		Polygon poly = tileObject.getCanvasTilePoly();
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color, new BasicStroke(strokeWidth));
		}
	}

	/**
	 * Calculates distance from player to a LocalPoint
	 */
	public static int getDistanceFromPlayer(Client client, LocalPoint location)
	{
		LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();
		if (playerLocation == null || location == null)
		{
			return Integer.MAX_VALUE;
		}
		return playerLocation.distanceTo(location);
	}

	/**
	 * Calculates distance between two LocalPoints
	 */
	public static int getDistanceBetween(LocalPoint from, LocalPoint to)
	{
		if (from == null || to == null)
		{
			return Integer.MAX_VALUE;
		}
		return from.distanceTo(to);
	}

	/**
	 * Renders a highlight box around a shape (for hovering/selection)
	 */
	public static void renderHoverableShape(Graphics2D graphics, Shape shape, Point mousePosition, Color borderColor, Color hoverColor, Color fillColor)
	{
		if (shape == null)
		{
			return;
		}

		boolean isHovered = shape.contains(mousePosition.getX(), mousePosition.getY());

		graphics.setColor(isHovered ? hoverColor : borderColor);
		graphics.setStroke(new BasicStroke(2));
		graphics.draw(shape);

		if (fillColor != null)
		{
			graphics.setColor(fillColor);
			graphics.fill(shape);
		}
	}
}
