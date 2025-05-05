package de.aschallenberg.gamelibrary;


import de.aschallenberg.gamelibrary.game.Game;
import de.aschallenberg.gamelibrary.modules.TicTacToe3x3;
import de.aschallenberg.gamelibrary.modules.TicTacToe5x5;
import de.aschallenberg.gamelibrary.modules.TicTacToeModule;
import de.aschallenberg.middleware.dto.BotData;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
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
		logInit();
		sendMove();
	}

	@Override
	public void onMoveReceived(final de.aschallenberg.middleware.dto.BotData sender, final Object moveObject) {
		int move = jsonObjectMapper.convertValue(moveObject, Integer.class);

		BotData currentBotData = getCurrentBot();
		if (!currentBotData.equals(sender)) {
			log.warn("current bot and sender do not match. Current bot: {}, Sender: {}", currentBotData, sender);
			return;
		}

		// Check if move valid.
		int maxMove = board.length - 1;
		if (move < 0 || move > maxMove || board[move] != 0) {
			disqualifyBot(currentBotData);
			return;
		}

		board[move] = currentBotIndex + 1;

		logBoard();
		sendGameUpdate(board, getGameData().getBots()); // send current board to all bots

		Map<BotData, Integer> scores = checkForGameFinished();

		if (scores == null) { // Game is not finished
			currentBotIndex = (currentBotIndex + 1) % 2;
			sendMove();
		} else { // Game is finished
			logFinish(scores);
			sendFinished(scores);
			resetGame();
		}
	}

	private void logInit() {
		List<BotData> bots = getGameData().getBots();
		BotData bot1 = bots.get(0);
		BotData bot2 = bots.get(1);

		String logMessage = """
				Bot 1: %s (%s)
				Bot 2: %s (%s)
				""".formatted(bot1.getName(), bot1.getOwnerName(), bot2.getName(), bot2.getOwnerName());

		sendLog(logMessage);
	}

	private void logBoard() {
		BotData bot = getCurrentBot();

		String logMessage = "Bot am Zug: %s (%s) [%d]%n Spielfeld: %n"
				.formatted(bot.getName(), bot.getOwnerName(), currentBotIndex);
		if (board.length == 9) {
			logMessage += """
					%d %d %d
					%d %d %d
					%d %d %d
					""";
		} else if (board.length == 25) {
			logMessage += """
					%d %d %d %d %d
					%d %d %d %d %d
					%d %d %d %d %d
					%d %d %d %d %d
					%d %d %d %d %d
					""";
		}

		logMessage = logMessage.formatted(Arrays.stream(board).boxed().toArray());
		sendLog(logMessage);
	}

	private void logFinish(Map<BotData, Integer> scores) {
		List<BotData> bots = getGameData().getBots();
		BotData bot1 = bots.get(0);
		BotData bot2 = bots.get(1);

		int points1 = scores.get(bot1);
		int points2 = scores.get(bot2);

		String winnerString;
		if (points1 > points2) {
			winnerString = "%s (%s) [1]".formatted(bot1.getName(), bot1.getOwnerName());
		} else if (points1 < points2) {
			winnerString = "%s (%s) [2]".formatted(bot2.getName(), bot2.getOwnerName());
		} else {
			winnerString = "Unentschieden";
		}

		String logMessage = """
				Spiel abgeschlossen!
				Sieger: %s
				Punkte:
				%s (%s) [1] hat %d Punkte
				%s (%s) [2] hat %d Punkte
				"""
				.formatted(winnerString,
						bot1.getName(), bot1.getOwnerName(), points1,
						bot2.getName(), bot2.getOwnerName(), points2
				);

		sendLog(logMessage);
	}

	@Override
	public void onGameUpdateReceived(final de.aschallenberg.middleware.dto.BotData sender, final Object gameUpdateData) {
		// Nothing to do here
	}

	@Override
	public void onBotDisqualified(final BotData botData) {
		disqualify(botData);
	}

	@Override
	protected void disqualifyBot(final BotData botData) {
		super.disqualifyBot(botData);
		disqualify(botData);
	}

	private void disqualify(BotData botData) {
		List<BotData> bots = getGameData().getBots();

		BotData firstBot = bots.get(0);
		BotData other = firstBot.equals(botData) ? bots.get(1) : firstBot;
		Map<BotData, Integer> scores = Map.of(botData, 0, other, 2);

		sendFinished(scores);
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
