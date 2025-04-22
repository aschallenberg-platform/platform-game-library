package de.aschallenberg.gamelibrary.game;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GameRegistry {
	private static Class<? extends Game> gameClass;

	public static void setGameClass(Class<? extends Game> gameClass) {
		GameRegistry.gameClass = gameClass;
	}

	public static Game instantiateGame() {
		try {
			return gameClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to instantiate game class", e);
		}
	}
}
