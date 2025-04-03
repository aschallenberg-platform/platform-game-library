package de.aschallenberg.gamelibrary;


import de.aschallenberg.gamelibrary.game.GameRegisty;
import de.aschallenberg.gamelibrary.websocket.WebSocketInitiator;

public class Main {

    /**
     * Setzte hier deine Klasse, die von {@link de.aschallenberg.gamelibrary.game.Game} erbt, als GameClass in der GameRegistry.
     * Ohne das kann das Framework deine Klasse nicht instanziieren.
     * Mit WebSocketInitiator.initConnection() baust du die WebSocket Verbindung zur Plattform auf. Stelle sicher,
     * dass deine config.properties korrekt konfiguriert ist, bevor die Verbindung aufgebaut wird.
     * @param args Command line Arguments, aber die sind uns hier erstmal egal.
     */
    public static void main(String[] args) {
        GameRegisty.setGameClass(TicTacToe.class);
        WebSocketInitiator.initConnection();
    }
}

