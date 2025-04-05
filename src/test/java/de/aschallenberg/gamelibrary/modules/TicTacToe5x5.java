package de.aschallenberg.gamelibrary.modules;

public class TicTacToe5x5 extends TicTacToeModule {
    @Override
    public int[][] getWinPatterns() {
        return new int[][]{
                // Reihen
                {0, 1, 2, 3}, {1, 2, 3, 4},
                {5, 6, 7, 8}, {6, 7, 8, 9},
                {10, 11, 12, 13}, {11, 12, 13, 14},
                {15, 16, 17, 18}, {16, 17, 18, 19},
                {20, 21, 22, 23}, {21, 22, 23, 24},

                // Spalten
                {0, 5, 10, 15}, {5, 10, 15, 20},
                {1, 6, 11, 16}, {6, 11, 16, 21},
                {2, 7, 12, 17}, {7, 12, 17, 22},
                {3, 8, 13, 18}, {8, 13, 18, 23},
                {4, 9, 14, 19}, {9, 14, 19, 24},

                // Diagonalen (von links oben nach rechts unten)
                {0, 6, 12, 18}, {6, 12, 18, 24},
                {1, 7, 13, 19}, {5, 11, 17, 23},
                {2, 8, 14, 20},

                // Diagonalen (von rechts oben nach links unten)
                {3, 7, 11, 15}, {4, 8, 12, 16},
                {8, 12, 16, 20}, {9, 13, 17, 21},
                {14, 18, 22, 24}
        };
    }

    @Override
    public int getBoardSize() {
        return 25;
    }
}
