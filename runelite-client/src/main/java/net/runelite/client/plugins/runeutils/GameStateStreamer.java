package net.runelite.client.plugins.runeutils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Streams game state to dev server for digital twin rendering
 * Auto-syncs icons for new item IDs via IconExtractor
 */
@Slf4j
public class GameStateStreamer
{
	private final Client client;
	private final ItemManager itemManager;
	private final DevServerClient devClient;
	private final IconExtractor iconExtractor;
	private final Set<Integer> syncedItemIds = ConcurrentHashMap.newKeySet();
	private long lastStreamTime = 0;
	private static final long STREAM_INTERVAL_MS = 500;

	public GameStateStreamer(Client client, ItemManager itemManager, DevServerClient devClient, IconExtractor iconExtractor)
	{
		this.client = client;
		this.itemManager = itemManager;
		this.devClient = devClient;
		this.iconExtractor = iconExtractor;
	}

	public void streamState()
	{
		if (!shouldStream())
		{
			return;
		}

		JsonObject gameState = captureGameState();
		if (gameState == null)
		{
			return;
		}

		devClient.sendMessage("game_state", gameState);
		lastStreamTime = System.currentTimeMillis();
	}

	private boolean shouldStream()
	{
		if (!devClient.isConnected())
		{
			return false;
		}

		long timeSinceLastStream = System.currentTimeMillis() - lastStreamTime;
		return timeSinceLastStream >= STREAM_INTERVAL_MS;
	}

	private JsonObject captureGameState()
	{
		JsonObject state = new JsonObject();

		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null)
		{
			state.add("inventory", serializeContainer(inventory));
		}

		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank != null)
		{
			state.add("bank", serializeContainer(bank));
		}

		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment != null)
		{
			state.add("equipment", serializeContainer(equipment));
		}

		return state;
	}

	private JsonObject serializeContainer(ItemContainer container)
	{
		JsonObject obj = new JsonObject();
		JsonArray itemsArray = new JsonArray();

		Item[] items = container.getItems();
		if (items == null)
		{
			return obj;
		}

		for (Item item : items)
		{
			itemsArray.add(serializeItem(item));
		}

		obj.add("items", itemsArray);
		return obj;
	}

	private JsonObject serializeItem(Item item)
	{
		JsonObject obj = new JsonObject();

		if (item == null)
		{
			obj.addProperty("id", -1);
			return obj;
		}

		obj.addProperty("id", item.getId());
		obj.addProperty("quantity", item.getQuantity());

		if (item.getId() != -1)
		{
			String name = getItemName(item.getId());
			obj.addProperty("name", name);

			// Auto-sync icon if this item ID hasn't been synced yet
			if (iconExtractor != null && syncedItemIds.add(item.getId()))
			{
				iconExtractor.syncIcon(item.getId(), name);
			}
		}

		return obj;
	}

	private String getItemName(int itemId)
	{
		try
		{
			return itemManager.getItemComposition(itemId).getName();
		}
		catch (Exception e)
		{
			return "Unknown";
		}
	}
}
