package de.aschallenberg.gamelibrary.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.aschallenberg.gamelibrary.config.ConfigLoader;
import de.aschallenberg.gamelibrary.game.Game;
import de.aschallenberg.gamelibrary.game.GameRegistry;
import de.aschallenberg.middleware.dto.BotData;
import de.aschallenberg.middleware.messages.Message;
import de.aschallenberg.middleware.messages.Meta;
import de.aschallenberg.middleware.messages.Payload;
import de.aschallenberg.middleware.messages.payloads.*;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.UUID;

@Log4j2
public final class WebSocketHandler extends WebSocketClient {
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Marker PLATFORM_MARKER = MarkerManager.getMarker("Platform");

	private final Game game = GameRegistry.instantiateGame();

	public WebSocketHandler(URI serverUri) {
		super(serverUri);
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		log.info("Connected to {}", getURI());

		String tokenAsString = ConfigLoader.get("platform.game.token");
		UUID token = UUID.fromString(tokenAsString);

		MessageSender.sendMessage(new RegisterRequestPayload(token));
	}

	@Override
	public synchronized void onMessage(String messageString) {
		final Message message;
		try {
			message = mapper.readValue(messageString, Message.class);
		} catch (final JsonProcessingException e) {
			log.warn(PLATFORM_MARKER, "Could not parse message: {}", e.getMessage());
			MessageSender.sendMessage(new ErrorPayload("Invalid JSON format: " + e.getMessage()));
			return;
		}

		switch (message.getPayload()) {
			case final ErrorPayload payload -> handleError(message, payload);
			case final BotClientDisconnectPayload payload -> handleBotClientDisconnected(message, payload);
			case final RegisterRequestPayload payload -> ignore();
			case final RegisterResponsePayload payload -> handleRegisterResponse(message, payload);
			case final LogPayload payload -> ignore();
			case final LobbyJoinPayload payload -> ignore();
			case final LobbyStartPayload payload -> ignore();
			case final LobbyInterruptPayload payload -> handleInterrupt(message, payload);
			case final LobbyFinishedPayload payload -> ignore();
			case final GameStartForBotsPayload payload -> ignore();
			case final GameStartPayload payload -> handleGameStart(message, payload);
			case final GameInterruptPayload payload -> handleInterrupt(message, payload);
			case final GameFinishedPayload payload -> ignore();
			case final StageStartPayload payload -> ignore();
			case final StageFinishedPayload payload -> ignore();
			case final GameUpdatePayload<?> payload -> handleGameUpdate(message, payload);
			case final MovePayload<?> payload -> handleMove(message, payload);
			case final DisqualifyPayload payload -> ignore();
			default -> handleUnknownMessage(message);
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

	private void handleError(
			@NonNull final Message message,
			@NonNull final ErrorPayload payload
	) {
		log.error(PLATFORM_MARKER, payload.getErrorMessage());
		System.exit(2);
	}

	private void handleRegisterResponse(
			@NonNull final Message message,
			@NonNull final RegisterResponsePayload payload
	) {
		log.info(PLATFORM_MARKER, "Successfully registered");
	}

	private void handleBotClientDisconnected(
			@NonNull final Message message,
			@NonNull final BotClientDisconnectPayload payload
	) {
		game.onBotDisconnected(payload.getDisconnectedBot());
	}

	private void handleGameStart(
			@NonNull final Message message,
			@NonNull final GameStartPayload payload
	) {
		game.onStartGame(payload.getGameData());
	}

	private void handleInterrupt(
			@NonNull final Message message,
			@NonNull final Payload payload
	) {
		game.onInterruptGame();
	}

	private void handleGameUpdate(
			@NonNull final Message message,
			@NonNull final GameUpdatePayload<?> payload
	) {
		BotData sender = getSender(message);
		if (sender != null) {
			game.onGameUpdateReceived(sender, payload);
		}
	}

	private void handleMove(
			@NonNull final Message message,
			@NonNull final MovePayload<?> payload
	) {
		BotData sender = getSender(message);
		if (sender != null) {
			game.onMoveReceived(sender, payload);
		}
	}

	private void handleUnknownMessage(@NonNull final Message message) {
		BotData sender = getSender(message);
		if (sender != null) {
			game.onOtherMessageReceived(sender, message.getPayload());
		}
	}

	private BotData getSender(@NonNull final Message message) {
		Meta meta = message.getMeta();
		if (meta != null) {
			BotData sender = meta.getSender();

			if (sender != null) {
				return sender;
			}
		}

		MessageSender.sendMessage(new ErrorPayload("Needed a sender but no sender was provided."));
		return null;
	}

	private void ignore() {}
}
