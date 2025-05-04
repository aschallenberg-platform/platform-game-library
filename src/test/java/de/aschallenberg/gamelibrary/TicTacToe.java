package de.aschallenberg.gamelibrary;


import de.aschallenberg.gamelibrary.game.Game;
import de.aschallenberg.gamelibrary.modules.TicTacToe3x3;
import de.aschallenberg.gamelibrary.modules.TicTacToe5x5;
import de.aschallenberg.gamelibrary.modules.TicTacToeModule;
import de.aschallenberg.middleware.dto.BotData;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;

@Log4j2
public class TicTacToe extends Game {
	TicTacToeModule module;
	private int[] board;
	private int currentBotIndex;


	@Override
	public void onStartGame() {
		String moduleString = getGameData().getModule();

		if (moduleString.equals("Klassisches 3x3 Feld für 2 Bots")) {
			module = new TicTacToe3x3();
		} else if (moduleString.equals("Größeres 5x5 Feld für 2 Bots")) {
			module = new TicTacToe5x5();
		} else {
			throw new RuntimeException("Unknown module: " + moduleString);
		}

		resetGame(); // Initialize board and currentBotIndex
		sendMove();
	}

	@Override
	public void onMoveReceived(final de.aschallenberg.middleware.dto.BotData sender, final Object moveObject) {
		System.out.println(moveObject);
		int move = jsonObjectMapper.convertValue(moveObject, Integer.class);

		BotData currentBotData = getCurrentBot();
		if (!currentBotData.equals(sender)) {
			log.warn("current bot and sender do not match. Current bot: {}, Sender: {}", currentBotData, sender);
			return;
		}

		// Check if move valid.
		int maxMove = board.length - 1;
		if (move < 0 || move > maxMove || board[move] != 0) {
			disqualify(currentBotData);
			return;
		}

		board[move] = currentBotIndex + 1;

		sendGameUpdate(board, getGameData().getBots()); // send current board to all bots

		Map<BotData, Integer> scores = checkForGameFinished();

		if (scores == null) { // Game is not finished
			currentBotIndex = (currentBotIndex + 1) % 2;
			sendMove();
		} else { // Game is finished
			sendFinished(scores);
			resetGame();
		}
	}

	@Override
	public void onGameUpdateReceived(final de.aschallenberg.middleware.dto.BotData sender, final Object gameUpdateData) {
		// Nothing to do here
	}

	@Override
	protected void disqualify(final BotData botData) {
		List<BotData> bots = getGameData().getBots();

		BotData firstBot = bots.get(0);
		BotData other = firstBot.equals(botData) ? bots.get(1) : firstBot;

		super.disqualify(botData);

		sendFinished(Map.of(
				botData, 0,
				other, 2
		));
		resetGame();
	}

	@Override
	public void resetGame() {
		board = new int[module.getBoardSize()];
		currentBotIndex = 0;
	}

	private Map<BotData, Integer> checkForGameFinished() {
		for (int cell : board) {
			if (cell == 0) {
				return null; // game still running
			}
		}

		List<BotData> bots = getGameData().getBots();

		for (int[] pattern : module.getWinPatterns()) {
			if (board[pattern[0]] != 0 && board[pattern[0]] == board[pattern[1]] && board[pattern[1]] == board[pattern[2]]) {
				int winnerIndex = board[pattern[0]];

				return Map.of(
						bots.get(winnerIndex - 1), 2, // Winner gets 2 points
						bots.get((winnerIndex) % 2), 0 // Loser gets 0 points
				);
			}
		}

		return Map.of(bots.get(0), 1, bots.get(1), 1); // Draw: both get 1 point
	}

	private BotData getCurrentBot() {
		return getGameData().getBots().get(currentBotIndex);
	}

	private void sendMove() {
		super.sendMove(new Move(board, currentBotIndex), getCurrentBot());
	}
}
