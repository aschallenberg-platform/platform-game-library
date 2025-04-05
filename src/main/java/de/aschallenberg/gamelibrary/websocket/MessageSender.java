package de.aschallenberg.gamelibrary.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.aschallenberg.gamelibrary.data.BotData;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;

@Log4j2
@UtilityClass
public class MessageSender {
	static final String OBJECT_KEY = "object";
	static final String TYPE_KEY = "type";
	static final String RECIPIENTS_KEY = "recipients";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static WebSocketHandler webSocketHandler;

	public static void sendMessage(@NonNull MessageType type) {
		sendMessage(Map.of(
				TYPE_KEY, type
		));
	}

	public static void sendMessage(@NonNull MessageType type, @NonNull Object object) {
		sendMessage(Map.of(
				TYPE_KEY, type,
				OBJECT_KEY, object
		));
	}

	public static void sendMessage(@NonNull MessageType type, @NonNull Object object, @NonNull List<BotData> recipients) {
		if (recipients.isEmpty()) {
			throw new IllegalArgumentException("Bots cannot be empty");
		}

		sendMessage(Map.of(
				TYPE_KEY, type,
				OBJECT_KEY, object,
				RECIPIENTS_KEY, recipients
		));
	}

	private static void sendMessage(Map<String, Object> data) {
		try {
			webSocketHandler.send(OBJECT_MAPPER.writeValueAsString(data));
			log.info("Sent [{}]: {}", data.get(TYPE_KEY), data);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("'object' could not be converted to JSON: " + e.getMessage(), e);
		}
	}

	static void setWebSocketHandler(WebSocketHandler webSocketHandler) {
		MessageSender.webSocketHandler = webSocketHandler;
	}
}
