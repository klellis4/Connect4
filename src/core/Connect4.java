package core;

/**
 * This is the class for the Connect4 game. It creates a board
 * for the game to be played on, and holds the methods that allow
 * the players to drop their game pieces, and check for a winner.
 *
 * @author Kelly Ellis
 * @version 1.0
 */

public class Connect4 {

    public static char[][] board;

    /**
     *  Constructor that creates a 2D array as the game board with 42 spaces,
     *  and initializes it with spaces that represent each 'hole' on the board.
     */
    public Connect4() {
        board = new char[6][7];

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = ' ';
            }
        }
    }

    /**
     * Prints the game board by iterating through each index in the 2D array,
     * and adds a | delimiter around spaces
     */
    public void printBoard() {
        for (int i = 0; i < board.length; i++) {
            System.out.print("|");
            for (int j = 0; j < board[i].length; j++) {
                System.out.printf("%c|", board[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Makes sure the board isn't full by checking the top row for an empty spot
     *
     * @return Returns false if there is at least 1 empty spot left, returns true
     * if the board is full.
     */
    public boolean boardIsFull() {
        for (int i = 0; i < board[0].length; i++) {
            if (board[0][i] == ' ') {
                // spot is empty
                return false;
            }
        }
        return true;
    }

    /**
     * Method to drop the game piece into the game board.
     *
     * @param player The player whose turn it is, either X or O.
     * @param column The column the player input, where they want to drop their game piece.
     * @return Returns true if the game piece was successfully dropped into the column. Returns
     * false otherwise.
     */
    public boolean dropPiece(char player, int column) {
        boolean drop = false;
        if (column < 0 || column > 7) {
            throw new ArrayIndexOutOfBoundsException("Entered column is invalid.");
        }
        // Iterates over the given column's rows to find the top-most
        // available empty spot, and then drops that player's game
        // piece in that spot
        for (int i = board.length - 1; i >= 0; i--) {
            if (board[i][column-1] == ' ') {
                board[i][column-1] = player;
//            if (board[i][column] == ' ') {
//                board[i][column] = player;
                drop = true;
                break;
            }
        }
        return drop;
    }

    /**
     * Checks for a winner of the game. A player can win by getting 4
     * pieces consecutively in a row, in a column, or diagonally.
     *
     * @return Returns true if there are either 4 X's or 4 O's consecutively
     * in a row, column, or diagonal. Returns false otherwise.
     */
    public boolean checkWin() {

        // Variables for each possible way to win, initialized to false
        boolean fourInACol = false;
        boolean fourInARow = false;
        boolean fourDiagonalTRBL = false;
        boolean fourDiagonalTLBR = false;

        // Checks columns for possible winner
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 6; j++) {
                // Makes sure that there are 4 of the same game pieces in a column across 4 rows
                if (board[i][j] == board[i + 1][j] && board[i][j] == board[i + 2][j]
                        && board[i][j] == board[i + 3][j] && board[i][j] != ' ') {
                    fourInACol = true;
                    break;
                }
            }
        }

        // Checks rows for possible winner
        for (int i = 0; i <= 5; i++) {
            // Iterates over columns
            for (int j = 0; j <= 3; j++) {
                // Makes sure that there are 4 of the same game pieces in a row across 4 columns
                if (board[i][j] == board[i][j + 1] && board[i][j] == board[i][j + 2]
                        && board[i][j] == board[i][j + 3] && board[i][j] != ' ') {
                    fourInARow = true;
                    break;
                }
            }
        }

        // Checks diagonal (from top right to bottom left) for possible winner
        for (int i = 0; i <= 2; i++) {
            for (int j = 3; j <= 6; j++) {
                // Makes sure that there are 4 of the same game pieces consecutively
                if (board[i][j] == board[i + 1][j - 1] && board[i][j] == board[i + 2][j - 2]
                        && board[i][j] == board[i + 3][j  - 3] && board[i][j] != ' ') {
                    fourDiagonalTRBL = true;
                    break;
                }
            }
        }

        // Checks diagonal (from top left to bottom right) for possible winner
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 3; j++) {
                // Makes sure that there are 4 of the same game pieces consecutively in a diagonal
                if (board[i][j] == board[i + 1][j + 1] && board[i][j] == board[i + 2][j + 2]
                        && board[i][j] == board[i + 3][j + 3] && board[i][j] != ' ') {
                    fourDiagonalTLBR = true;
                    break;
                }
            }
        }

        // If any of the booleans are true, there's a winner (return true)
        if (fourInARow || fourInACol || fourDiagonalTLBR || fourDiagonalTRBL) {
            return true;
        }
        else {
            return false;
        }

    }
}
