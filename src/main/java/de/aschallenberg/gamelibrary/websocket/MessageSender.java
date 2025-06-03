package de.aschallenberg.gamelibrary.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.aschallenberg.communication.dto.BotData;
import de.aschallenberg.communication.messages.Message;
import de.aschallenberg.communication.messages.MessageFactory;
import de.aschallenberg.communication.messages.Payload;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@UtilityClass
public class MessageSender {
	public static final boolean DEBUG = false;

	private static final ObjectMapper mapper = new ObjectMapper();

	private static WebSocketHandler webSocketHandler;

	public static void sendMessage(@NonNull Payload payload) {
		sendMessage(MessageFactory.createMessage(payload));
	}

	public static void sendMessage(@NonNull Payload payload, List<BotData> recipients) {
		sendMessage(MessageFactory.createMessage(payload, recipients));
	}

	private static void sendMessage(Message message) {
		String json;
		try {
			json = mapper.writeValueAsString(message);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("P" +
					"Message could not be converted to JSON: " + e.getMessage(), e);
		}

		webSocketHandler.send(json);

		if (DEBUG) {
			log.info("Sent: {}", message);
		}
	}

	static void setWebSocketHandler(WebSocketHandler webSocketHandler) {
		MessageSender.webSocketHandler = webSocketHandler;
	}
}
