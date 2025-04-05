package de.aschallenberg.gamelibrary.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.aschallenberg.gamelibrary.config.ConfigLoader;
import de.aschallenberg.gamelibrary.data.BotData;
import de.aschallenberg.gamelibrary.data.GameData;
import de.aschallenberg.gamelibrary.game.Game;
import de.aschallenberg.gamelibrary.game.GameRegisty;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

@Log4j2
public final class WebSocketHandler extends WebSocketClient {
	private static final Marker PLATFORM_MARKER = MarkerManager.getMarker("Platform");
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


	private final Game game = GameRegisty.instantiateGame();

	public WebSocketHandler(URI serverUri) {
		super(serverUri);
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		log.info("Connected to {}", getURI());

		MessageSender.sendMessage(MessageType.REGISTER, ConfigLoader.get("platform.game.token"));
	}

	@Override
	public synchronized void onMessage(String message) {
		Map<String, Object> data;
		try {
			data = OBJECT_MAPPER.readValue(message, Map.class);
		} catch (JsonProcessingException e) {
			log.error("Can't parse message: {}", e.getMessage());
			return;
		}
		MessageType type = MessageType.valueOf((String) data.get(MessageSender.TYPE_KEY));
		Object object = data.get(MessageSender.OBJECT_KEY); // null if the map doesn't have an object

		log.info("Received [{}]: {}", type, message);

		switch (type) {
			case ERROR -> handleError(OBJECT_MAPPER.convertValue(object, String.class));
			case BOT_CLIENT_DISCONNECTED -> game.onBotDisconnected(OBJECT_MAPPER.convertValue(object, BotData.class));
			case REGISTER -> log.info(PLATFORM_MARKER, "Successfully registered");
			case START -> handleGameStart(OBJECT_MAPPER.convertValue(object, GameData.class));
			case INTERRUPT -> game.onInterruptGame();
			case FINISHED -> handleFinished();
			case GAME_INTERNAL -> game.onMessageReceived(getSender(data), object);
			case MOVE -> game.onMove(getSender(data), object);
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

	private void handleError(String errorMessage) {
		log.error(PLATFORM_MARKER, errorMessage);
		System.exit(2);
	}

	private void handleGameStart(GameData dto) {
		game.onStartGame(dto.getBots(), dto.getModule(), dto.getVersion(), dto.getSettings());
	}

	private void handleFinished() {
		// If the game receives a FINISHED, it's irrelevant
	}

	private BotData getSender(Map<String, Object> data) {
		return OBJECT_MAPPER.convertValue(data.get("sender"), BotData.class);
	}
}
