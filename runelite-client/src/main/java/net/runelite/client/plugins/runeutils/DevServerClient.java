package net.runelite.client.plugins.runeutils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * WebSocket client for dev server communication
 * Enables hot-reload without plugin rebuilds
 * Auto-reconnects with exponential backoff on disconnection
 */
@Slf4j
public class DevServerClient extends WebSocketClient
{
	private final Gson gson = new Gson();
	private final Map<String, CopyOnWriteArrayList<Consumer<JsonObject>>> handlers = new ConcurrentHashMap<>();
	private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
	private String sessionId;
	private boolean connected = false;
	private boolean shutdownRequested = false;
	private int reconnectAttempt = 0;
	private static final int MAX_RECONNECT_DELAY_SECONDS = 30;

	public DevServerClient(String serverUrl)
	{
		super(URI.create(serverUrl));
		setConnectionLostTimeout(10);
	}

	@Override
	public void onOpen(ServerHandshake handshake)
	{
		connected = true;
		reconnectAttempt = 0;
		log.info("[DevServer] Connected to dev server");
	}

	@Override
	public void onMessage(String rawMessage)
	{
		JsonObject message = parseMessage(rawMessage);
		if (message == null)
		{
			return;
		}

		String messageType = extractMessageType(message);
		if (messageType == null)
		{
			return;
		}

		if ("connected".equals(messageType))
		{
			handleConnected(message);
		}

		emitEvent(messageType, message);
	}

	@Override
	public void onClose(int code, String reason, boolean remote)
	{
		connected = false;
		log.info("[DevServer] Disconnected: {} (code: {})", reason, code);
		emitEvent("disconnected", new JsonObject());
		scheduleReconnect();
	}

	@Override
	public void onError(Exception error)
	{
		log.error("[DevServer] WebSocket error", error);
	}

	/**
	 * Send message to dev server
	 */
	public void sendMessage(String type, JsonObject data)
	{
		if (!connected)
		{
			log.warn("[DevServer] Cannot send message, not connected");
			return;
		}

		JsonObject message = buildMessage(type, data);
		String json = gson.toJson(message);
		send(json);
	}

	/**
	 * Subscribe to message type
	 */
	public void on(String messageType, Consumer<JsonObject> handler)
	{
		handlers.computeIfAbsent(messageType, k -> new CopyOnWriteArrayList<>()).add(handler);
	}

	/**
	 * Unsubscribe from message type
	 */
	public void off(String messageType, Consumer<JsonObject> handler)
	{
		CopyOnWriteArrayList<Consumer<JsonObject>> messageHandlers = handlers.get(messageType);
		if (messageHandlers != null)
		{
			messageHandlers.remove(handler);
		}
	}

	/**
	 * Subscribe to channel on dev server
	 */
	public void subscribe(String channel)
	{
		JsonObject data = new JsonObject();
		data.addProperty("channel", channel);
		sendMessage("subscribe", data);
		log.debug("[DevServer] Subscribed to channel: {}", channel);
	}

	public boolean isConnected()
	{
		return connected;
	}

	@Override
	public void close()
	{
		shutdownRequested = true;
		reconnectExecutor.shutdownNow();
		super.close();
	}

	private void scheduleReconnect()
	{
		if (shutdownRequested)
		{
			return;
		}

		reconnectAttempt++;
		int delay = Math.min((int) Math.pow(2, reconnectAttempt), MAX_RECONNECT_DELAY_SECONDS);
		log.info("[DevServer] Reconnecting in {}s (attempt {})", delay, reconnectAttempt);

		reconnectExecutor.schedule(() ->
		{
			if (shutdownRequested)
			{
				return;
			}

			try
			{
				reconnect();
			}
			catch (Exception e)
			{
				log.warn("[DevServer] Reconnect failed: {}", e.getMessage());
				scheduleReconnect();
			}
		}, delay, TimeUnit.SECONDS);
	}

	public String getSessionId()
	{
		return sessionId;
	}

	private JsonObject parseMessage(String rawMessage)
	{
		try
		{
			return gson.fromJson(rawMessage, JsonObject.class);
		}
		catch (Exception e)
		{
			log.error("[DevServer] Failed to parse message", e);
			return null;
		}
	}

	private String extractMessageType(JsonObject message)
	{
		if (!message.has("type"))
		{
			log.warn("[DevServer] Message missing type field");
			return null;
		}

		return message.get("type").getAsString();
	}

	private void handleConnected(JsonObject message)
	{
		if (message.has("sessionId"))
		{
			sessionId = message.get("sessionId").getAsString();
			log.info("[DevServer] Session ID: {}", sessionId);
		}
	}

	private JsonObject buildMessage(String type, JsonObject data)
	{
		JsonObject message = new JsonObject();
		message.addProperty("type", type);
		message.addProperty("source", "plugin");
		message.addProperty("timestamp", System.currentTimeMillis());

		if (sessionId != null)
		{
			message.addProperty("sessionId", sessionId);
		}

		if (data != null)
		{
			message.add("data", data);
		}

		return message;
	}

	private void emitEvent(String messageType, JsonObject message)
	{
		CopyOnWriteArrayList<Consumer<JsonObject>> messageHandlers = handlers.get(messageType);
		if (messageHandlers == null)
		{
			return;
		}

		for (Consumer<JsonObject> handler : messageHandlers)
		{
			try
			{
				handler.accept(message);
			}
			catch (Exception e)
			{
				log.error("[DevServer] Handler error for {}", messageType, e);
			}
		}
	}
}
