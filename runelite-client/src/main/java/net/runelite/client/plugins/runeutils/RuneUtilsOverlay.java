package net.runelite.client.plugins.runeutils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Overlay for highlighting inventory items based on configured profiles
 */
public class RuneUtilsOverlay extends Overlay
{
	private final Client client;
	private final RuneUtilsPlugin plugin;
	private final RuneUtilsPanel panel;
	private final RuneUtilsConfig config;
	private final SlotSelectionState slotSelectionState;
	private final ItemManager itemManager;

	// Debug output deduplication - tracks state across render calls
	private boolean lastWasSlotSelection = false;
	private boolean lastHadInventory = false;

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

		// Check if itemManager is available
		if (itemManager == null)
		{
			return null;
		}

		// Get inventory
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null)
		{
			if (lastHadInventory)
			{
				System.out.println("[Overlay] Inventory closed");
				lastHadInventory = false;
			}
			return null;
		}

		Item[] items = inventory.getItems();
		if (items == null)
		{
			return null;
		}

		// Get inventory widget for positioning
		Widget inventoryWidget = client.getWidget(149, 0);
		if (inventoryWidget == null)
		{
			return null;
		}

		// Get individual slot widgets (each slot is a child widget with its own bounds)
		Widget[] slotWidgets = inventoryWidget.getChildren();
		if (slotWidgets == null || slotWidgets.length < 28)
		{
			return null;
		}

		lastHadInventory = true;

		// Check if we're in slot selection mode
		if (slotSelectionState.isActive())
		{
			if (!lastWasSlotSelection)
			{
				System.out.println("[Overlay] Slot selection mode activated");
				lastWasSlotSelection = true;
			}
			// Draw slot selection overlays (clickable, not click-through)
			renderSlotSelection(graphics, slotWidgets);
			return null;
		}
		else if (lastWasSlotSelection)
		{
			System.out.println("[Overlay] Slot selection mode deactivated");
			lastWasSlotSelection = false;
		}

		// Draw status overlays (click-through) - shows match/mismatch for profile items
		renderSlotStatus(graphics, slotWidgets, items);

		return null;
	}

	public void onInventoryChanged()
	{
		// Can be used for additional processing when inventory changes
	}

	/**
	 * Draw slot selection overlays when in selection mode
	 */
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

			// Draw selection overlay
			graphics.setColor(selectionColor);
			graphics.fillRect(slotBounds.x, slotBounds.y, slotBounds.width, slotBounds.height);

			// Draw border
			graphics.setColor(Color.WHITE);
			graphics.setStroke(new BasicStroke(2));
			graphics.drawRect(slotBounds.x, slotBounds.y, slotBounds.width, slotBounds.height);

			// Draw slot number
			graphics.setColor(Color.BLACK);
			String slotText = String.valueOf(i + 1);
			graphics.drawString(slotText, slotBounds.x + slotBounds.width / 2 - 5, slotBounds.y + slotBounds.height / 2 + 5);
		}
	}

	/**
	 * Draw status overlays showing match/mismatch for profile items
	 */
	private void renderSlotStatus(Graphics2D graphics, Widget[] slotWidgets, Item[] items)
	{
		Color matchColor = config.slotMatchColor();
		Color mismatchColor = config.slotMismatchColor();

		for (int i = 0; i < Math.min(items.length, 28); i++)
		{
			Item item = items[i];
			Widget slotWidget = slotWidgets[i];
			if (slotWidget == null)
			{
				continue;
			}

			Rectangle slotBounds = slotWidget.getBounds();

			// Check if this slot/item matches any enabled profile
			boolean shouldHighlight = false;
			boolean isCorrect = false;

			if (item != null && item.getId() != -1)
			{
				var itemComposition = itemManager.getItemComposition(item.getId());
				if (itemComposition != null)
				{
					String itemName = itemComposition.getName();

					for (ProfileState profile : panel.getProfileStates())
					{
						if (!profile.isEnabled() || profile.getContainerType() != ContainerType.INVENTORY)
						{
							continue;
						}

						ContainerSnapshot snapshot = profile.getSnapshot();
						if (snapshot == null)
						{
							continue;
						}

						// Check position-specific items (must be in exact slot)
						TrackedItemState positionState = snapshot.getPositionSpecificStates().get(i);
						if (positionState != null)
						{
							shouldHighlight = true;
							if (positionState.matches(item.getId(), itemName, item.getQuantity(), i))
							{
								isCorrect = true;
							}
							break; // Found a position-specific requirement for this slot
						}

						// Check position-agnostic items (can be in any slot)
						for (TrackedItemState agnosticState : snapshot.getPositionAgnosticStates().values())
						{
							if (agnosticState.matches(item.getId(), itemName, item.getQuantity(), null))
							{
								shouldHighlight = true;
								isCorrect = true;
								break;
							}
						}

						if (shouldHighlight)
						{
							break;
						}
					}
				}
			}
			else
			{
				// Empty slot - check if any profile requires something here
				for (ProfileState profile : panel.getProfileStates())
				{
					if (!profile.isEnabled() || profile.getContainerType() != ContainerType.INVENTORY)
					{
						continue;
					}

					ContainerSnapshot snapshot = profile.getSnapshot();
					if (snapshot == null)
					{
						continue;
					}

					// Check if this slot is required by profile (position-specific)
					TrackedItemState requiredState = snapshot.getPositionSpecificStates().get(i);
					if (requiredState != null)
					{
						shouldHighlight = true;
						isCorrect = false; // Item is missing
						break;
					}
				}
			}

			// Draw overlay based on status
			if (shouldHighlight)
			{
				if (isCorrect)
				{
					graphics.setColor(matchColor);
				}
				else
				{
					graphics.setColor(mismatchColor);
				}
				graphics.fillRect(slotBounds.x, slotBounds.y, slotBounds.width, slotBounds.height);
			}
		}
	}

	/**
	 * Handle mouse clicks for slot selection
	 */
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

		// Check which slot was clicked by checking bounds of each slot widget
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
