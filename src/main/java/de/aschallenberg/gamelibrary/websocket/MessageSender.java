package de.aschallenberg.gamelibrary.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.aschallenberg.gamelibrary.config.ConfigLoader;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Log4j2
@UtilityClass
public class MessageSender {
    static final String GI_TOKEN_KEY = "gi_token";
    static final String TYPE_KEY = "type";
    static final String LOG_KEY = "log";
    static final String RECIPIENTS_KEY = "recipients";
    static final String GAME_DATA_KEY = "game_data";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static WebSocketHandler webSocketHandler;

    public static void sendGameData(@NonNull Map<String, Object> data, boolean logMessage, @NonNull List<UUID> recipients) {
        sendData(MessageType.GAME_INTERNAL, data, logMessage, recipients);
    }

    static void sendPlatformData(@NonNull MessageType type, Map<String, Object> data) {
        sendData(type, data, false, null);
    }

    static void sendData(MessageType type, Map<String, Object> gameData, boolean logMessage, List<UUID> recipients) {
        Map<String, Object> data = new HashMap<>();

        data.put(TYPE_KEY, type.name());
        data.put(GI_TOKEN_KEY, ConfigLoader.get("plattform.game.token"));

        if (type == MessageType.GAME_INTERNAL) {
            data.put(LOG_KEY, logMessage);
            data.put(RECIPIENTS_KEY, recipients);
            data.put(GAME_DATA_KEY, gameData);
        }

        String dataString;
        try {
            dataString = OBJECT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("'data' could not be converted to JSON: " + e.getMessage());
        }

        log.info("Sending data: {}", dataString);
        webSocketHandler.send(dataString);
    }

    public static void sendFinished() {
        sendPlatformData(MessageType.FINISHED, null);
    }

    static void setWebSocketHandler(WebSocketHandler webSocketHandler) {
        MessageSender.webSocketHandler = webSocketHandler;
    }
}
