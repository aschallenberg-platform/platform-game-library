package de.aschallenberg.gamelibrary.modules;

public class TicTacToe3x3 extends TicTacToeModule {
    @Override
    public int[][] getWinPatterns() {
        return new int[][]{
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Reihen
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Spalten
                {0, 4, 8}, {2, 4, 6}             // Diagonalen
        };
    }

    @Override
    public int getBoardSize() {
        return 9;
    }
}
