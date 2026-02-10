package net.runelite.client.plugins.runeutils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Overlay for highlighting container items based on configured profiles
 * Optimized with O(1) lookups using pre-staged data structures
 */
public class RuneUtilsOverlay extends Overlay
{
	private final Client client;
	private final RuneUtilsPlugin plugin;
	private final RuneUtilsPanel panel;
	private final RuneUtilsConfig config;
	private final SlotSelectionState slotSelectionState;
	private final ItemManager itemManager;

	private boolean lastWasSlotSelection = false;
	private ProfileState hoveredProfile = null;

	public RuneUtilsOverlay(Client client, RuneUtilsPlugin plugin, RuneUtilsPanel panel, RuneUtilsConfig config, SlotSelectionState slotSelectionState, ItemManager itemManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.panel = panel;
		this.config = config;
		this.slotSelectionState = slotSelectionState;
		this.itemManager = itemManager;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enableInventoryHighlighting())
		{
			return null;
		}

		if (itemManager == null)
		{
			return null;
		}

		if (slotSelectionState.isActive())
		{
			renderSlotSelectionMode(graphics);
			return null;
		}

		if (lastWasSlotSelection)
		{
			System.out.println("[Overlay] Slot selection mode deactivated");
			lastWasSlotSelection = false;
		}

		renderAllContainerOverlays(graphics);

		return null;
	}

	private void renderSlotSelectionMode(Graphics2D graphics)
	{
		Widget inventoryWidget = client.getWidget(149, 0);
		if (inventoryWidget == null)
		{
			return;
		}

		Widget[] slotWidgets = inventoryWidget.getChildren();
		if (slotWidgets == null || slotWidgets.length < 28)
		{
			return;
		}

		if (!lastWasSlotSelection)
		{
			System.out.println("[Overlay] Slot selection mode activated");
			lastWasSlotSelection = true;
		}

		renderSlotSelection(graphics, slotWidgets);
	}

	private void renderAllContainerOverlays(Graphics2D graphics)
	{
		java.util.List<ProfileState> profilesToRender = getProfilesToRender();
		java.util.Set<ContainerType> activeContainers = new java.util.HashSet<>();

		for (ProfileState profile : profilesToRender)
		{
			if (!profile.getContainerType().isComposite())
			{
				activeContainers.add(profile.getContainerType());
			}
		}

		for (ContainerType containerType : activeContainers)
		{
			renderContainerOverlay(graphics, containerType, profilesToRender);
		}
	}

	private void renderContainerOverlay(Graphics2D graphics, ContainerType containerType, java.util.List<ProfileState> profiles)
	{
		ContainerWidgetInfo widgetInfo = ContainerWidgetInfo.forContainer(containerType);
		if (widgetInfo == null)
		{
			return;
		}

		ItemContainer container = client.getItemContainer(containerType.getInventoryId());
		if (container == null)
		{
			return;
		}

		Item[] items = container.getItems();
		if (items == null)
		{
			return;
		}

		Widget containerWidget = client.getWidget(widgetInfo.getGroupId(), widgetInfo.getChildId());
		if (containerWidget == null || containerWidget.isHidden())
		{
			return;
		}

		Widget[] slotWidgets = containerWidget.getChildren();
		if (slotWidgets == null || slotWidgets.length == 0)
		{
			return;
		}

		renderSlotStatus(graphics, slotWidgets, items, containerType, profiles);
	}

	private java.util.List<ProfileState> getProfilesToRender()
	{
		if (hoveredProfile != null)
		{
			return java.util.Collections.singletonList(hoveredProfile);
		}

		// Try to use dev server profiles from ArtifactManager first
		ArtifactManager artifactManager = plugin.getArtifactManager();
		if (artifactManager != null && artifactManager.isReady())
		{
			return artifactManager.getAllProfiles().stream()
				.filter(profile -> profile.shouldRender(client))
				.collect(java.util.stream.Collectors.toList());
		}

		// Fallback to panel profiles if dev server not available
		return panel.getProfileStates().stream()
			.filter(profile -> profile.shouldRender(client))
			.collect(java.util.stream.Collectors.toList());
	}

	public void onInventoryChanged()
	{
		// Can be used for additional processing when inventory changes
	}

	private void renderSlotSelection(Graphics2D graphics, Widget[] slotWidgets)
	{
		Color selectionColor = config.slotSelectionColor();

		for (int i = 0; i < Math.min(slotWidgets.length, 28); i++)
		{
			Widget slotWidget = slotWidgets[i];
			if (slotWidget == null)
			{
				continue;
			}

			Rectangle slotBounds = slotWidget.getBounds();

			graphics.setColor(selectionColor);
			graphics.fillRect(slotBounds.x, slotBounds.y, slotBounds.width, slotBounds.height);

			graphics.setColor(Color.WHITE);
			graphics.setStroke(new BasicStroke(2));
			graphics.drawRect(slotBounds.x, slotBounds.y, slotBounds.width, slotBounds.height);

			graphics.setColor(Color.BLACK);
			String slotText = String.valueOf(i + 1);
			graphics.drawString(slotText, slotBounds.x + slotBounds.width / 2 - 5, slotBounds.y + slotBounds.height / 2 + 5);
		}
	}

	private void renderSlotStatus(Graphics2D graphics, Widget[] slotWidgets, Item[] items, ContainerType containerType, java.util.List<ProfileState> profiles)
	{
		Color matchColor = config.slotMatchColor();
		Color mismatchColor = config.slotMismatchColor();

		// Pre-stage: Build O(1) lookup maps
		java.util.Map<Integer, TrackedItemState> positionRequirements = new java.util.HashMap<>();
		java.util.Map<Integer, java.util.List<TrackedItemState>> agnosticRequirements = new java.util.HashMap<>();

		// Calculate max valid slot index for this container
		int maxSlots = Math.min(items.length, slotWidgets.length);

		for (ProfileState profile : profiles)
		{
			if (profile.getContainerType() != containerType)
			{
				continue;
			}

			ContainerSnapshot snapshot = profile.getSnapshot();
			if (snapshot == null || snapshot.getAllItemStates().isEmpty())
			{
				continue;
			}

			for (java.util.Map.Entry<Integer, TrackedItemState> entry : snapshot.getPositionSpecificStates().entrySet())
			{
				int slot = entry.getKey();
				// Only add requirements that are within the valid slot range
				if (slot >= 0 && slot < maxSlots)
				{
					positionRequirements.put(slot, entry.getValue());
				}
			}

			for (TrackedItemState agnosticState : snapshot.getPositionAgnosticStates().values())
			{
				agnosticRequirements
					.computeIfAbsent(agnosticState.getItemId(), k -> new java.util.ArrayList<>())
					.add(agnosticState);
			}
		}

		if (positionRequirements.isEmpty() && agnosticRequirements.isEmpty())
		{
			return;
		}

		for (int i = 0; i < maxSlots; i++)
		{
			Item item = items[i];
			Widget slotWidget = slotWidgets[i];

			if (slotWidget == null)
			{
				continue;
			}

			TrackedItemState positionReq = positionRequirements.get(i);
			java.util.List<TrackedItemState> agnosticReqs = item != null && item.getId() != -1
				? agnosticRequirements.get(item.getId())
				: null;

			if (positionReq == null && agnosticReqs == null)
			{
				continue;
			}

			Rectangle slotBounds = slotWidget.getBounds();
			boolean shouldHighlight = false;
			boolean isCorrect = false;

			if (item != null && item.getId() != -1)
			{
				var itemComposition = itemManager.getItemComposition(item.getId());
				if (itemComposition != null)
				{
					String itemName = itemComposition.getName();

					if (positionReq != null)
					{
						shouldHighlight = true;
						isCorrect = positionReq.matches(item.getId(), itemName, item.getQuantity(), i);
					}
					else if (agnosticReqs != null)
					{
						for (TrackedItemState agnosticReq : agnosticReqs)
						{
							if (agnosticReq.matches(item.getId(), itemName, item.getQuantity(), null))
							{
								shouldHighlight = true;
								isCorrect = true;
								break;
							}
						}
					}
				}
			}
			else if (positionReq != null)
			{
				shouldHighlight = true;
				isCorrect = false;
			}

			if (shouldHighlight)
			{
				graphics.setColor(isCorrect ? matchColor : mismatchColor);
				graphics.fillRect(slotBounds.x, slotBounds.y, slotBounds.width, slotBounds.height);
			}
		}
	}

	public void setHoveredProfile(ProfileState profile)
	{
		this.hoveredProfile = profile;
	}

	public void handleClick(int mouseX, int mouseY)
	{
		if (!slotSelectionState.isActive())
		{
			return;
		}

		Widget inventoryWidget = client.getWidget(149, 0);
		if (inventoryWidget == null)
		{
			return;
		}

		Widget[] slotWidgets = inventoryWidget.getChildren();
		if (slotWidgets == null || slotWidgets.length < 28)
		{
			return;
		}

		for (int i = 0; i < Math.min(slotWidgets.length, 28); i++)
		{
			Widget slotWidget = slotWidgets[i];
			if (slotWidget == null)
			{
				continue;
			}

			Rectangle slotBounds = slotWidget.getBounds();
			if (slotBounds.contains(mouseX, mouseY))
			{
				slotSelectionState.selectSlot(i);
				return;
			}
		}
	}
}
