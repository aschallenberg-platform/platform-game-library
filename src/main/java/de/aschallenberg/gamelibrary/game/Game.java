package de.aschallenberg.gamelibrary.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.aschallenberg.gamelibrary.data.BotData;
import de.aschallenberg.gamelibrary.websocket.MessageSender;
import de.aschallenberg.gamelibrary.websocket.MessageType;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class representing a game. You must extend this class to create your own game.
 * <p>
 * This class serves as a base for all game implementations. It defines the structure and methods that must be
 * implemented by any specific game.
 */
public abstract class Game {
	/**
	 * JSON object mapper used for converting objects to and from JSON for sending them to the platform and the game.
	 */
	protected final ObjectMapper jsonObjectMapper = new ObjectMapper();

	/**
	 * List of
	 */
	@Getter
	private List<BotData> bots;

	/**
	 * Module name of the game.
	 */
	@Getter
	private String module;

	/**
	 * Version of the game.
	 */
	@Getter
	private String version;

	/**
	 * Settings for the game.
	 */
	@Getter
	private Map<String, ?> settings;

	/**
	 * Called when the platform signals that the game should start.
	 *
	 * @param botData A list of bots that will participate in the game.
	 */
	public final void onStartGame(List<BotData> botData, String module, String version, Map<String, ?> settings) {
		this.bots = botData;
		this.module = module;
		this.version = version;
		this.settings = settings;
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
	 * @param object The object representing the move.
	 */
	public abstract void onMove(BotData sender, Object object);

	/**
	 * Called when the platform sends other data to the game. This method is used to handle incoming data that is not
	 * assigned to a specific action, such as move or start. The simplest way to handle the object is to implement a
	 * Data Transfer Object (DTO) class and parse it using the jsonObjectMapper as follows:
	 * <p>
	 * {@code jsonObjectMapper.convertValue(object, YourDTO.class)}
	 * </p>
	 *
	 * @param sender The Bot that sent the message
	 * @param object The Object with the data.
	 */
	public abstract void onMessageReceived(BotData sender, Object object);

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
	 * This methode can be overridden to implement a better bot disconnect handling.
	 *
	 * @param botData The Bot that disconnected
	 */
	public void onBotDisconnected(BotData botData) {
		resetGame();
		MessageSender.sendMessage(MessageType.INTERRUPT);
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
		Map<String, Integer> scoresMap = new HashMap<>();
		scores.entrySet().forEach(entry -> {
			try {
				scoresMap.put(jsonObjectMapper.writeValueAsString(entry.getKey()), entry.getValue());
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});

		MessageSender.sendMessage(MessageType.FINISHED, scoresMap);
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
		if (bots.remove(botData)) {
			MessageSender.sendMessage(MessageType.DISQUALIFY, botData);
		}
	}

	/**
	 * Sends a move message to a single bot.
	 * <p>
	 * This method sends a message of type MOVE to the platform, including the specified object and bot.
	 * </p>
	 *
	 * @param object   The object representing the move.
	 * @param receiver The bot to which the move message will be sent.
	 */
	protected void sendMove(Object object, BotData receiver) {
		MessageSender.sendMessage(MessageType.MOVE, object, List.of(receiver));
	}

	/**
	 * Sends a message to the specified list of bots.
	 * <p>
	 * This method sends a message of type GAME_INTERNAL to the platform, including the specified object and list of bots.
	 * It also logs the message if the logMessage parameter is true.
	 * </p>
	 *
	 * @param object     The object to be sent in the message.
	 * @param recipients The list of bots to which the message will be sent.
	 */
	protected final void sendMessage(@NonNull Object object, @NonNull List<BotData> recipients) {
		MessageSender.sendMessage(MessageType.GAME_INTERNAL, object, recipients);
	}

	/**
	 * Sends a message to a single bot.
	 * <p>
	 * This method sends a message of type GAME_INTERNAL, that specifies game internal messages (for bots) to the
	 * platform, including the specified object (data) and bot.
	 * It also logs the message on the platform if the logMessage parameter is true.
	 * </p>
	 *
	 * @param object     The object to be sent in the message.
	 * @param recipients The bot to which the message will be sent.
	 */
	protected final void sendMessage(@NonNull Object object, @NonNull BotData recipients) {
		sendMessage(object, List.of(recipients));
	}

	/**
	 * Sends an error message to a single bot.
	 * <p>
	 * This method sends a message of type ERROR to the platform, including the specified error message and bot.
	 * </p>
	 *
	 * @param errorMessage The error message to be sent.
	 * @param recipients   The bot to which the error message will be sent.
	 */
	protected final void sendError(@NonNull String errorMessage, @NonNull BotData recipients) {
		MessageSender.sendMessage(MessageType.ERROR, errorMessage, List.of(recipients));
	}
}
