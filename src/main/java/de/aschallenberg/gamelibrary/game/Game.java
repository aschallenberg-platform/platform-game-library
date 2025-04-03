package de.aschallenberg.gamelibrary.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.aschallenberg.gamelibrary.websocket.MessageSender;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract class representing a game. You must extend this class to create your own game.
 * <p>
 * This class serves as a base for all game implementations. It defines the structure and methods that must be
 * implemented by any specific game.
 */
public abstract class Game {
    protected final ObjectMapper jsonObjectMapper = new ObjectMapper();

    @Getter
    private List<Bot> bots;

    @Getter
    private String module;

    @Getter
    private String version;

    @Getter
    private Map<String, ?> settings;

    /**
     * Called when the platform signals that the game should start.
     * @param bots A list of bots that will participate in the game.
     */
    public final void onStartGame(List<Bot> bots, String module, String version, Map<String, ?> settings) {
        this.bots = bots;
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
     * Called when the platform sends data to the game. This method is used to handle incoming data.
     * @param data A map containing the data sent by the platform. The keys and values depend on the message type.
     */
    public final void onDataReceived(Map<String, Object> data, UUID botToken) {
        bots.stream()
                .filter(bot ->  bot.getToken().equals(botToken))
                .findFirst()
                .ifPresent(bot -> onDataReceived(data, bot));
    }

    /**
     * Called when the platform sends data to the game. This method is used to handle incoming data.
     * @param data A map containing the data sent by the platform. The keys and values depend on the message type.
     */
    public abstract void onDataReceived(Map<String, Object> data, Bot bot);

    /**
     * Called when the game is interrupted. This method is used to handle any cleanup. After this the platform will
     * handle the game as ready to start again.
     */
    public abstract void onInterruptGame();

    /**
     * Sends a message to the platform indicating that the game has finished.
     */
    protected void sendGameFinished() {
        MessageSender.sendFinished();
    }

    /**
     * Sends a message to the platform.
     *
     * @param data The data to be sent. This should be a map containing the data to be sent.
     */
    protected final void sendData(@NonNull Map<String, Object> data, boolean logMessage, @NonNull List<Bot> bots) {
        MessageSender.sendGameData(data, logMessage, bots.stream().map(Bot::getToken).toList());
    }

    protected final void sendData(@NonNull Map<String, Object> data, boolean logMessage, @NonNull Bot bot) {
        MessageSender.sendGameData(data, logMessage, List.of(bot.getToken()));
    }
}
