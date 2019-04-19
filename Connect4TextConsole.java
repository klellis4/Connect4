package ui;
import core.Connect4;
import core.Connect4ComputerPlayer;
import core.Connect4Server;
import javafx.application.Application;


import java.util.Scanner;

/**
 * This is the Connect4 game's console. It creates a new Connect4 board,
 * and allows 2 players to play the game, or 1 player to play against
 * the computer until someone wins or they tie.
 *
 * @author Kelly Ellis
 * @version 1.0
 */
public class Connect4TextConsole {

    public static void main(String[] args) {
        Connect4 game = new Connect4();
        boolean turnSwitch = true;
        Scanner input = new Scanner(System.in);

        System.out.println("Please enter 'G' if you'd like to play with the GUI, or enter " +
                "'C' if you'd like to play on the console.");
        char playing = input.next().charAt(0);

        if (playing == 'G') {
            //Application.launch(Connect4GUI.class, args);
            Application.launch(Connect4Server.class, args);

        }

        if (playing == 'C') {
            System.out.println("Begin game. Enter 'P' if you want to play against another player; " +
                    "enter 'C' to play against the computer.");
            char selection = input.next().charAt(0);
            if (selection == 'P') {
                do {
                    // Switches the players each turn
                    turnSwitch = !turnSwitch;
                    game.printBoard();
                    char player;

                    if (turnSwitch) {
                        player = 'O';
                    } else {
                        player = 'X';
                    }
                    System.out.println("Player " + player + " - your turn. Choose a column number from 1-7. ");

                    boolean position = false;
                    while (!position) {
                        try {
                            position = game.dropPiece(player, input.nextInt());
                            // If game piece can't be dropped, column is full
                            if (!position) {
                                System.out.println("Column is already filled.");
                                System.out.println("Please try again: ");
                            }
                        } catch (Exception exc) {
                            // Invalid column number was entered
                            System.out.println("Invalid input.");
                            System.out.println("Please try again: ");
                            input.nextLine();
                        }
                    }
                    System.out.println();
                }
                // While there is no winner and no tie, keep printing the board.
                while (!game.boardIsFull() && !game.checkWin());
                game.printBoard();

                // If someone wins, print which player won
                if (game.checkWin()) {
                    System.out.printf("Player %s won the game.", (turnSwitch ? "O" : "X"));
                }
                // Otherwise, it must be a tie
                else {
                    System.out.println("It's a tie! The game is over.");
                }
                input.close();

            }
            // Computer playing
            else if (selection == 'C') {
                System.out.println("Start game against the computer.");

                do {
                    // Switches the players each turn
                    turnSwitch = !turnSwitch;
                    game.printBoard();
                    char player;

                    if (turnSwitch) {
                        player = 'O'; // computer's turn
                    } else {
                        player = 'X';
                    }

                    boolean position = false;
                    while (!position) {
                        if (player == 'X') {
                            System.out.println("It is your turn. Choose a column number from 1-7. ");
                            try {
                                position = game.dropPiece(player, input.nextInt());
                                if (!position) {
                                    System.out.println("Column is already filled.");
                                    System.out.println("Please try again: ");
                                }
                            } catch (Exception exc) {
                                System.out.println("Invalid input.");
                                System.out.println("Please try again: ");
                                input.nextLine();
                            }
                        } else if (player == 'O') {
                            System.out.println("Computer's turn...");
                            try {
                                int column = Connect4ComputerPlayer.chooseColumn();
                                position = Connect4ComputerPlayer.dropComputerPiece('O', column);
                                if (!position) {
                                    System.out.println("Column is full. The computer will choose again...");

                                }
                            } catch (Exception exc) {
                                System.out.println("Invalid input. Computer will try again.");
                            }
                        }
                    }
                    System.out.println();
                }

                // While there is no winner and no tie, keep printing the board.
                while (!game.boardIsFull() && !game.checkWin());
                game.printBoard();

                // If someone wins, print which player won
                if (game.checkWin()) {
                    System.out.printf("%s won the game.", (turnSwitch ? "The computer" : "You"));
                }
                // Otherwise, it must be a tie
                else {
                    System.out.println("It's a tie! The game is over.");
                }
                input.close();
            }
        }
    }
}
