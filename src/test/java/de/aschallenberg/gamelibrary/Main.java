package de.aschallenberg.gamelibrary;


import de.aschallenberg.gamelibrary.config.ConfigLoader;
import de.aschallenberg.gamelibrary.game.GameRegistry;
import de.aschallenberg.gamelibrary.websocket.WebSocketInitiator;

public class Main {

	/**
	 * Setzte hier deine Klasse, die von {@link de.aschallenberg.gamelibrary.game.Game} erbt, als GameClass in der GameRegistry.
	 * Ohne das kann das Framework deine Klasse nicht instanziieren.
	 * Mit WebSocketInitiator.initConnection() baust du die WebSocket Verbindung zur Plattform auf. Stelle sicher,
	 * dass deine config.properties korrekt konfiguriert ist, bevor die Verbindung aufgebaut wird.
	 *
	 * @param args Command line Arguments, die ggf. die config.properties überschreiben.
	 */
	public static void main(String[] args) {
		GameRegistry.setGameClass(TicTacToe.class);
		ConfigLoader.load(args);
		WebSocketInitiator.initConnection();
	}
}

