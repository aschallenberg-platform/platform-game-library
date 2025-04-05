package de.aschallenberg.gamelibrary.websocket;

import de.aschallenberg.gamelibrary.config.ConfigLoader;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.net.URI;

@Log4j2
@UtilityClass
public class WebSocketInitiator {

	/**
	 * The URI format for the WebSocket connection.
	 * <p>
	 * This format is used to create the WebSocket URI using the host and port
	 * specified in the configuration.
	 * </p>
	 */
	private static final String WS_URI_FORMAT = "ws://%s:%s/ws/game";

	/**
	 * Initializes the WebSocket connection.
	 * <p>
	 * This method retrieves the WebSocket host and port from the configuration,
	 * creates a WebSocketHandler client, and attempts to establish a connection
	 * synchronously. If the connection attempt is interrupted, it logs a warning
	 * message and re-interrupts the current thread.
	 * </p>
	 */
	public static void initConnection() {
		initConnection(true);
	}

	public static void initConnection(boolean interruptOnFailure) {
		ConfigLoader.load();

		String host = ConfigLoader.get("platform.host");
		String port = ConfigLoader.get("platform.port");

		WebSocketHandler client = new WebSocketHandler(URI.create(String.format(WS_URI_FORMAT, host, port)));
		MessageSender.setWebSocketHandler(client);

		try {
			// Establish connection synchronously
			client.connectBlocking();
		} catch (InterruptedException e) {
			if (interruptOnFailure) {
				log.warn(e.getMessage());
				System.exit(1);
			} else {
				log.warn("Cannot connect to the platform");
			}
		}
	}
}
