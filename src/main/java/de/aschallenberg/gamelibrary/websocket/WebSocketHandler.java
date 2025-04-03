package de.aschallenberg.gamelibrary.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.aschallenberg.gamelibrary.game.Bot;
import de.aschallenberg.gamelibrary.game.Game;
import de.aschallenberg.gamelibrary.game.GameRegisty;
import lombok.extern.log4j.Log4j2;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.*;

@Log4j2
public final class WebSocketHandler extends WebSocketClient {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Game game = GameRegisty.instantiateGame();

    public WebSocketHandler(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        log.info("Connected to {}", getURI());

        MessageSender.sendPlatformData(MessageType.REGISTER, null);
    }

    @Override
    public synchronized void onMessage(String message) {
        Map<String, Object> data = objectMapper.convertValue(message, Map.class);
        MessageType type = MessageType.valueOf((String) data.get(MessageSender.TYPE_KEY));

        log.info("Message received: {}", message);

        switch (type) {
            case REGISTER -> log.info("Successfully registered");
            case START -> handleGameStart(data);
            case INTERRUPT -> game.onInterruptGame();
            default -> {
                Map<String, Object> gameData = objectMapper.convertValue(data.get(MessageSender.GAME_DATA_KEY), Map.class);
                game.onDataReceived(gameData, UUID.fromString((String) data.get("bot")));
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.warn("Connection closed ({}): {}", code, reason);
    }

    @Override
    public void onError(Exception ex) {
        log.error(ex.getMessage());
    }

    private void handleGameStart(Map<String, Object> data) {
        List<Bot> bots = getBots(data.get("bots"));
        String module = (String) data.get("module");
        String version = (String) data.get("version");
        Map<String, Object> settings = (Map<String, Object>) data.get("settings");

        game.onStartGame(bots, module, version, settings);
    }

    public List<Bot> getBots(Object botsObject) {
        List<Bot> bots = new ArrayList<>();

        if(botsObject instanceof Object[] botsObjectArray) {
            for (Object botObject : botsObjectArray) {
                if (botObject instanceof Map<?, ?> botMap) {
                    Bot bot = Bot.fromMap((Map<String, Object>) botMap);
                    bots.add(bot);
                } else {
                    log.error("Invalid bot object: {}", botObject);
                }
            }
        } else {
            log.error("Invalid bots object: {}", botsObject);
        }

        return bots;
    }
}
