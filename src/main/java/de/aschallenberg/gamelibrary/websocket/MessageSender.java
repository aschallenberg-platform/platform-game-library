package de.aschallenberg.gamelibrary.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.aschallenberg.middleware.dto.BotData;
import de.aschallenberg.middleware.messages.Message;
import de.aschallenberg.middleware.messages.MessageFactory;
import de.aschallenberg.middleware.messages.Payload;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@UtilityClass
public class MessageSender {
	private static final ObjectMapper mapper = new ObjectMapper();

	private static WebSocketHandler webSocketHandler;

	public static void sendMessage(@NonNull Payload payload) {
		sendMessage(MessageFactory.createMessage(payload));
	}

	public static void sendMessage(@NonNull Payload payload, BotData sender) {
		sendMessage(MessageFactory.createMessage(payload, sender));
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
		log.info("Sent [{}]: {}", message.getPayload().getClass().getSimpleName(), json);
	}

	static void setWebSocketHandler(WebSocketHandler webSocketHandler) {
		MessageSender.webSocketHandler = webSocketHandler;
	}
}
