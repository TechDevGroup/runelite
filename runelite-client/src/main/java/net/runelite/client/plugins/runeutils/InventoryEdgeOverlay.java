package net.runelite.client.plugins.runeutils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Overlay that renders profile tabs on inventory edges for quick access
 */
public class InventoryEdgeOverlay extends Overlay
{
	private static final int TAB_WIDTH = 30;
	private static final int ORB_SIZE = 8;
	private static final Color ENABLED_COLOR = new Color(0, 200, 0);
	private static final Color DISABLED_COLOR = new Color(100, 100, 100);
	private static final Color BACKGROUND_ENABLED = new Color(60, 60, 60, 200);
	private static final Color BACKGROUND_DISABLED = new Color(40, 40, 40, 150);
	private static final Color BORDER_COLOR = Color.WHITE;

	private final Client client;
	private final List<ProfileState> profiles;
	private final EdgeSide edgeSide;
	private final Map<Integer, Rectangle> tabBounds;

	private Integer hoveredProfileIndex = null;

	public enum EdgeSide
	{
		LEFT,
		RIGHT
	}

	public InventoryEdgeOverlay(Client client, List<ProfileState> profiles, EdgeSide edgeSide)
	{
		this.client = client;
		this.profiles = profiles;
		this.edgeSide = edgeSide;
		this.tabBounds = new HashMap<>();

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Widget inventoryWidget = client.getWidget(149, 0);
		if (inventoryWidget == null)
		{
			return null;
		}

		Rectangle inventoryBounds = inventoryWidget.getBounds();
		if (inventoryBounds == null)
		{
			return null;
		}

		List<ProfileState> visibleProfiles = getVisibleProfiles();
		if (visibleProfiles.isEmpty())
		{
			return null;
		}

		renderTabs(graphics, inventoryBounds, visibleProfiles);

		return null;
	}

	/**
	 * Get list of profiles that should be visible based on prioritized filtering
	 */
	private List<ProfileState> getVisibleProfiles()
	{
		boolean hasPrioritized = false;
		for (ProfileState profile : profiles)
		{
			if (profile.isPrioritized())
			{
				hasPrioritized = true;
				break;
			}
		}

		List<ProfileState> visibleProfiles = new ArrayList<>();
		for (ProfileState profile : profiles)
		{
			if (hasPrioritized)
			{
				if (profile.isPrioritized())
				{
					visibleProfiles.add(profile);
				}
			}
			else
			{
				visibleProfiles.add(profile);
			}
		}

		return visibleProfiles;
	}

	/**
	 * Render profile tabs on the inventory edge
	 */
	private void renderTabs(Graphics2D graphics, Rectangle inventoryBounds, List<ProfileState> visibleProfiles)
	{
		tabBounds.clear();

		int tabCount = visibleProfiles.size();
		if (tabCount == 0)
		{
			return;
		}

		int tabHeight = inventoryBounds.height / tabCount;
		int xPosition = edgeSide == EdgeSide.LEFT ? inventoryBounds.x - TAB_WIDTH : inventoryBounds.x + inventoryBounds.width;

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for (int i = 0; i < visibleProfiles.size(); i++)
		{
			ProfileState profile = visibleProfiles.get(i);
			int yPosition = inventoryBounds.y + (i * tabHeight);

			Rectangle tabRect = new Rectangle(xPosition, yPosition, TAB_WIDTH, tabHeight);
			tabBounds.put(i, tabRect);

			renderTab(graphics, tabRect, profile);
		}
	}

	/**
	 * Render a single profile tab
	 */
	private void renderTab(Graphics2D graphics, Rectangle tabRect, ProfileState profile)
	{
		Color backgroundColor = profile.isEnabled() ? BACKGROUND_ENABLED : BACKGROUND_DISABLED;
		graphics.setColor(backgroundColor);
		graphics.fillRect(tabRect.x, tabRect.y, tabRect.width, tabRect.height);

		graphics.setColor(BORDER_COLOR);
		graphics.setStroke(new BasicStroke(1));
		graphics.drawRect(tabRect.x, tabRect.y, tabRect.width, tabRect.height);

		int orbX = tabRect.x + (tabRect.width / 2) - (ORB_SIZE / 2);
		int orbY = tabRect.y + 5;
		Color orbColor = profile.isEnabled() ? ENABLED_COLOR : DISABLED_COLOR;
		graphics.setColor(orbColor);
		graphics.fillOval(orbX, orbY, ORB_SIZE, ORB_SIZE);

		String profileName = profile.getName();
		if (profileName.length() > 3)
		{
			profileName = profileName.substring(0, 3);
		}

		FontMetrics fm = graphics.getFontMetrics();
		int textWidth = fm.stringWidth(profileName);
		int textX = tabRect.x + (tabRect.width / 2) - (textWidth / 2);
		int textY = tabRect.y + tabRect.height / 2 + fm.getAscent() / 2;

		graphics.setColor(Color.WHITE);
		graphics.drawString(profileName, textX, textY);
	}

	/**
	 * Check if a point is inside any tab and return the profile index
	 */
	public Integer getProfileIndexAt(Point point)
	{
		for (Map.Entry<Integer, Rectangle> entry : tabBounds.entrySet())
		{
			if (entry.getValue().contains(point))
			{
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Get the profile at the given index from visible profiles
	 */
	public ProfileState getProfileAt(int index)
	{
		List<ProfileState> visibleProfiles = getVisibleProfiles();
		if (index >= 0 && index < visibleProfiles.size())
		{
			return visibleProfiles.get(index);
		}
		return null;
	}

	/**
	 * Set the hovered profile index for preview
	 */
	public void setHoveredProfileIndex(Integer index)
	{
		this.hoveredProfileIndex = index;
	}

	/**
	 * Get the currently hovered profile for preview
	 */
	public ProfileState getHoveredProfile()
	{
		if (hoveredProfileIndex == null)
		{
			return null;
		}
		return getProfileAt(hoveredProfileIndex);
	}
}
