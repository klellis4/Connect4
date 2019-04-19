package ui;
import core.Connect4;
import core.Connect4ComputerPlayer;
import core.Connect4Server;
import javafx.application.Application;
import javafx.application.Platform;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * This is the Connect4 game's console. It creates a new Connect4 board,
 * and allows 2 players to play the game, or 1 player to play against
 * the computer until someone wins or they tie.
 *
 * @author Kelly Ellis
 * @version 1.0
 */
public class Connect4TextConsole2 {

    private static DataInputStream fromServer;
    private static DataOutputStream toServer;
    private static boolean myTurn = false;
    private static char myToken = ' ';
    private static char otherToken = ' ';
    private static int rowSelected;
    private static int columnSelected;
    private static boolean continueToPlay = true;
    private static boolean waiting = true;
    static Connect4 game = new Connect4();
    static Scanner input = new Scanner(System.in);
    static int columnChosen;


    public static void main(String[] args) {

        boolean turnSwitch = true;


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
                connectToServer();
            }
//                do {
//                    // Switches the players each turn
//                    turnSwitch = !turnSwitch;
//                    game.printBoard();
//                    char player;
//
//                    if (turnSwitch) {
//                        player = 'O';
//                    } else {
//                        player = 'X';
//                    }
//                    System.out.println("Player " + player + " - your turn. Choose a column number from 1-7. ");
//
//                    boolean position = false;
//                    while (!position) {
//                        try {
//                            position = game.dropPiece(player, input.nextInt());
//                            // If game piece can't be dropped, column is full
//                            if (!position) {
//                                System.out.println("Column is already filled.");
//                                System.out.println("Please try again: ");
//                            }
//                        } catch (Exception exc) {
//                            // Invalid column number was entered
//                            System.out.println("Invalid input.");
//                            System.out.println("Please try again: ");
//                            input.nextLine();
//                        }
//                    }
//                    System.out.println();
//                }
//                // While there is no winner and no tie, keep printing the board.
//                while (!game.boardIsFull() && !game.checkWin());
//                game.printBoard();
//
//                // If someone wins, print which player won
//                if (game.checkWin()) {
//                    System.out.printf("Player %s won the game.", (turnSwitch ? "O" : "X"));
//                }
//                // Otherwise, it must be a tie
//                else {
//                    System.out.println("It's a tie! The game is over.");
//                }
//                input.close();
//
//            }
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

private static void connectToServer() {

    try {
        Socket socket = new Socket("localhost", 8004);
        fromServer = new DataInputStream(socket.getInputStream());
        toServer = new DataOutputStream(socket.getOutputStream());
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    new Thread(() -> {
        try {
            int player = fromServer.readInt();

            if (player == 1) {
                myToken = 'X';
                otherToken = 'O';
                System.out.println("Player 1 - You're X's");
                System.out.println("Waiting for second player to join...");

                fromServer.readInt();

                game.printBoard();
                System.out.println("Player 2 has joined. You play first. " +
                            "Choose a column number from 1-7. ");
                columnChosen = input.nextInt();

                myTurn = true;

            } else if (player == 2) {
                myToken = 'O';
                otherToken = 'X';

                System.out.println("Player 2 - You're O's");
                System.out.println("Waiting for Player 1 to move...");

            }

            while (continueToPlay) {
                if (player == 1) {
                    waitForPlayerAction();
                    sendMove();
                    receiveInfoFromServer();
                } else if (player == 2) {
                    receiveInfoFromServer();
                    waitForPlayerAction();
                    sendMove();
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }).start();
}


    private static void waitForPlayerAction () throws InterruptedException {
        while (waiting) {
            Thread.sleep(100);
        }
        waiting = true;
    }

    private static void sendMove() throws IOException {
        toServer.writeInt(rowSelected);
        toServer.writeInt(columnChosen);
    }

    private static void receiveInfoFromServer() throws IOException {
        int gameStatus = fromServer.readInt();

        if (gameStatus == 1) {
            continueToPlay = false;
            if (myToken == 'X') {
                System.out.println("You won!");
            }
            else if (myToken == 'O');
            {
                System.out.println("Player 1 won.");
            }
        }
        else if (gameStatus == 2) {
            continueToPlay = false;
            if (myToken == 'O') {
                System.out.println("You won!");
            }
            else if (myToken == 'X') {
                System.out.println("Player 2 won.");
            }
        }
        else if (gameStatus == 3) {
            continueToPlay = false;
            System.out.println("It's a tie!");
            if (myToken == 'O') {
                receiveMove();
            }
        }
        else {
            receiveMove();
            game.printBoard();
            System.out.println("Your turn - select a column from 1-7.");
            columnChosen = input.nextInt();
            myTurn = true;
        }
    }

    private static void receiveMove() throws IOException {
        //int row = fromServer.readInt();
        int column = fromServer.readInt();
        game.dropPiece(myToken, column);
        game.printBoard();
    }
}

