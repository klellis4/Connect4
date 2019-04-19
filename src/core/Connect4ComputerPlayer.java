package core;

/**
 * This is the class for the Connect4 game computer player's logic.
 *
 * @author Kelly Ellis
 * @version 1.0
 */

import java.util.Random;

public class Connect4ComputerPlayer extends Connect4 {

    /**
     * Method to randomly choose a column. Uses Java's Random() method.
     *
     * @return Returns the column randomly chosen as an integer
     */
    public static int chooseColumn() {
        Random rand = new Random();
        int column;
        column = rand.nextInt(7);
        return column;
    }

    /**
     * Method to drop the computer player's piece into the board.
     * Uses the same logic as the human player's dropPiece method in
     * Connect4.java.
     *
     * @param player The player's token. X for the human, O for the computer
     * @param column The randomly chosen column number from chooseColumn()
     * @return Returns true if the piece was successfully dropped into the column.
     * Returns false otherwise.
     */
    public static boolean dropComputerPiece(char player, int column) {
        boolean drop = false;
        if (column < 0 || column > 7) {
            throw new ArrayIndexOutOfBoundsException("Column is invalid.");
        }
        // Iterates over the given column's rows to find the top-most
        // available empty spot, and then drops that player's game
        // piece in that spot
        for (int i = board.length - 1; i >= 0; i--) {
            if (board[i][column-1] == ' ') {
                board[i][column-1] = player;
                drop = true;
                break;
            }
        }
        return drop;
    }
}
