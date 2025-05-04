package de.aschallenberg.gamelibrary.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.aschallenberg.gamelibrary.websocket.MessageSender;
import de.aschallenberg.middleware.dto.BotData;
import de.aschallenberg.middleware.dto.GameData;
import de.aschallenberg.middleware.messages.Payload;
import de.aschallenberg.middleware.messages.payloads.*;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Abstract class representing a game. You must extend this class to create your own game.
 * <p>
 * This class serves as a base for all game implementations. It defines the structure and methods that must be
 * implemented by any specific game.
 */
public abstract class Game {
	private static final Logger log = LogManager.getLogger(Game.class);
	/**
	 * JSON object mapper used for converting objects to and from JSON for sending them to the platform and the game.
	 */
	protected final ObjectMapper jsonObjectMapper = new ObjectMapper();

	/**
	 * List of
	 */
	@Getter
	private GameData gameData;

	/**
	 * Called when the platform signals that the game should start.
	 *
	 * @param gameData The game data with the necessary information for this game run
	 */
	public final void onStartGame(GameData gameData) {
		this.gameData = gameData;
		onStartGame();
	}

	/**
	 * Called when the platform signals that the game should start.
	 */
	public abstract void onStartGame();

	/**
	 * Called when the platform forwards a move of a bot.
	 * <p>
	 * This method is abstract and must be implemented by subclasses to handle the move of a bot.
	 * </p>
	 *
	 * @param sender The bot that made the move.
	 * @param move   The move.
	 */
	public abstract void onMoveReceived(BotData sender, Object move);

	/**
	 * Called when the platform forwards an update message from a bot to this game. This method is used to handle any
	 * incoming data for actions that are not a move or start.
	 * The simplest way to handle the object is to implement a
	 * Data Transfer Object (DTO) class and parse it using the jsonObjectMapper as follows:
	 * <p>
	 * {@code jsonObjectMapper.convertValue(object, YourDTO.class)}
	 * </p>
	 *
	 * @param sender         The Bot that sent the message
	 * @param gameUpdateData The game update data.
	 */
	public abstract void onGameUpdateReceived(BotData sender, Object gameUpdateData);

	/**
	 * Handles the reception of self-created messages from a bot.
	 * <p>
	 * Self-created messages must be described in the game description on the platform, otherwise bots are not able to
	 * implement these. The platform cannot check them. They are only for communication between bots and this game.
	 * </p>
	 *
	 * @param sender  The bot that sent the unknown message.
	 * @param payload The payload of the unknown message.
	 */
	public void onOtherMessageReceived(BotData sender, Payload payload) {
		log.warn("Received unknown message from {}: {}", sender, payload.getClass().getSimpleName());
	}

	/**
	 * Resets the game to its initial state.
	 * <p>
	 * This method is abstract and must be implemented by subclasses to define how the game should be reset.
	 * </p>
	 */
	public abstract void resetGame();

	/**
	 * Called when the game is interrupted. This method will bring the game into its initial state.
	 * After this the game has to be ready to play again.
	 */
	public void onInterruptGame() {
		resetGame();
	}

	/**
	 * Called when a Bot of this game disconnected. By default, the game will be reset.
	 * This method can be overridden to implement a better bot disconnect handling.
	 *
	 * @param botData The Bot that disconnected
	 */
	public void onBotDisconnected(BotData botData) {
		resetGame();
		MessageSender.sendMessage(new GameInterruptPayload());
	}

	/**
	 * Sends a message to the platform indicating that the game has finished.
	 * <p>
	 * This method sends a message of type FINISHED to the platform, including the scores of the bots.
	 * After that the method will clean up the game as in {@code onInterruptGame()} defined.
	 * </p>
	 *
	 * @param scores A map containing the bots and their corresponding scores.
	 */
	protected void sendFinished(Map<BotData, Integer> scores) {
		MessageSender.sendMessage(new GameFinishedPayload(scores));
		resetGame();
	}

	/**
	 * Disqualifies a bot from the game.
	 * <p>
	 * This method removes the specified bot from the list of participating bots. If the bot is successfully removed,
	 * a message of type DISQUALIFY is sent to the platform to notify it of the disqualification.
	 * </p>
	 *
	 * @param botData The bot to be disqualified.
	 */
	protected void disqualify(BotData botData) {
		if (gameData.getBots().remove(botData)) {
			MessageSender.sendMessage(new DisqualifyPayload(botData));
		}
	}

	/**
	 * Sends a move message to a single bot.
	 * <p>
	 * This method sends a message of type MOVE to the platform, including the specified object and bot.
	 * </p>
	 *
	 * @param move      The object representing the move.
	 * @param recipient The bot to which the move message will be sent.
	 */
	protected void sendMove(Object move, BotData recipient) {
		MessageSender.sendMessage(new MovePayload<>(move), List.of(recipient));
	}

	/**
	 * <p>
	 * This method sends a game update message to a list of bots (via the platform).
	 * </p>
	 *
	 * @param gameUpdate The game update to be sent in the message.
	 * @param recipients The list of bots to which the message will be sent.
	 */
	protected final void sendGameUpdate(@NonNull Object gameUpdate, @NonNull List<BotData> recipients) {
		MessageSender.sendMessage(new GameUpdatePayload<>(gameUpdate), recipients);
	}

	/**
	 * <p>
	 * This method sends a game update message to a single bot (via the platform).
	 * </p>
	 *
	 * @param gameUpdate The game update to be sent in the message.
	 * @param recipient  The bot to which the message will be sent.
	 */
	protected final void sendGameUpdate(@NonNull Object gameUpdate, @NonNull BotData recipient) {
		sendGameUpdate(gameUpdate, List.of(recipient));
	}

	/**
	 * Sends an error message to a single bot.
	 * <p>
	 * This method sends an error message to a specific bot (via the platform).
	 * </p>
	 *
	 * @param errorMessage The error message to be sent.
	 * @param recipient    The bot to which the error message will be sent.
	 */
	protected final void sendError(@NonNull String errorMessage, @NonNull BotData recipient) {
		MessageSender.sendMessage(new ErrorPayload(errorMessage), List.of(recipient));
	}
}
