package de.aschallenberg.gamelibrary;


import de.aschallenberg.gamelibrary.game.Bot;
import de.aschallenberg.gamelibrary.game.Game;

import java.util.Map;

public class TicTacToe extends Game {
    int[] board = new int[9];
    int currentBot = 0;

    int[][] winPatterns = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Reihen
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Spalten
            {0, 4, 8}, {2, 4, 6}             // Diagonalen
    };

    @Override
    public void onStartGame() {
        sendMoveRequest();
    }

    @Override
    public void onDataReceived(Map<String, Object> data, Bot bot) {
        if(isCurrentBot(bot)) {
            if (data.get("type").equals("move_response")) {
                int move = (int) data.get("move");

                if(board[move] != 0) {
                    sendMoveRequest("Invalid move");
                    return;
                }

                board[move] = currentBot + 1;

                if(!handleGameState()) {
                    currentBot = (currentBot + 1) % getBots().size();
                    sendMoveRequest();
                } else {
                    sendGameFinished();
                    reset();
                }
            }
        }
    }

    @Override
    public void onInterruptGame() {
        reset();
    }

    private void sendMoveRequest() {
        sendData(Map.of(
                "type", "move_request",
                "board", board,
                "player", currentBot + 1
        ), true, getBots().get(currentBot));
    }

    private boolean isCurrentBot(Bot bot) {
        return getBots().get(currentBot).equals(bot);
    }

    private boolean handleGameState() {
        int state = getGameState();

        return switch (state) {
            case -1 -> false;
            case 0 -> {
                sendDraw();
                yield true;
            }
            case 1, 2 -> {
                sendWin();
                yield true;
            }
            default -> throw new IllegalStateException("Unexpected value: " + state);
        };
    }

    private int getGameState() {
        for (int cell : board) {
            if (cell == 0) {
                return -1; // game still running
            }
        }

        for (int[] pattern : winPatterns) {
            if (board[pattern[0]] != 0 && board[pattern[0]] == board[pattern[1]] && board[pattern[1]] == board[pattern[2]]) {
                return board[pattern[0]]; // 1 or 2 wins the game
            }
        }

        return 0; // Draw
    }

    private void reset() {
        board = new int[9];
        currentBot = 0;
        getBots().clear();
    }

    private void sendMoveRequest(String error) {
        sendData(Map.of(
                "type", "move_request",
                "board", board,
                "error", error
        ), true, getBots().get(currentBot));
    }

    private void sendDraw() {
        sendData(Map.of(
                "type", "game_over",
                "result", "draw"
        ), true, getBots());
    }

    private void sendWin() {
        sendData(Map.of(
                "type", "game_over",
                "result", "win",
                "winner", getBots().get(currentBot).getToken()
        ), true, getBots());
    }
}
