package core;

import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * This class creates the Connect4 Server for players to play on
 *
 * @author Kelly Ellis, Hassan/Bansal
 * @version 1.0
 */
public class Connect4Server extends Application {

    private int sessionNo = 1;

    @Override
    /**
     * Creates the stage and scene for the server info. Creates a new thread
     * for the new socket(s)
     */
    public void start(Stage primaryStage) {
        TextArea text = new TextArea();

        Scene scene = new Scene(new ScrollPane(text), 450, 200);
        primaryStage.setTitle("Connect4 Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread( () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(8004);
                Platform.runLater(() -> text.appendText(new Date() + ": Server started at socket 8004\n"));

                while (true) {
                    Platform.runLater(() -> text.appendText(new Date() + ": Wait for players to join session " +
                            sessionNo + '\n'));

                    Socket player1 = serverSocket.accept();

                    Platform.runLater(() -> {
                        text.appendText(new Date() + ": Player 1 joined session " +
                                sessionNo + '\n');
                        text.appendText("Player 1's IP address: " +
                                player1.getInetAddress().getHostAddress() + '\n');
                    });

                    new DataOutputStream(player1.getOutputStream()).writeInt(1);

                    // connect to player2
                    Socket player2 = serverSocket.accept();

                    Platform.runLater(() -> {
                        text.appendText(new Date() + ": Player 2 joined session " + sessionNo + '\n');
                        text.appendText("Player 2's IP address " +
                                player2.getInetAddress().getHostAddress() + '\n');
                    });

                    new DataOutputStream(player2.getOutputStream()).writeInt(2);

                    Platform.runLater(() -> text.appendText(new Date() +
                            ": Start a thread for session " + sessionNo++ + '\n'));

                    new Thread(new HandleASession(player1, player2)).start();
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    /**
     * Handles new sessions with 2 players.
     */
    class HandleASession implements Runnable {
        private Socket player1;
        private Socket player2;

        private char[][] board = new char[6][7];

        /**
         * Creates the game board with 2 players
         * @param player1 First player to connect to server
         * @param player2 Second player to connect to server
         */
        public HandleASession(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;

            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 7; j++) {
                    board[i][j] = ' ';
                }
            }
        }

        /**
         * The run method for the thread. Sets up I/O streams for each player and
         * controls the game logic/flow. Receives moves from each player and continuously
         * checks for wins/ties and updates each player with the other player's moves.
         */
        public void run() {
            try {

                DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
                DataOutputStream toPlayer1 = new DataOutputStream(player1.getOutputStream());
                DataInputStream fromPlayer2 = new DataInputStream(player2.getInputStream());
                DataOutputStream toPlayer2 = new DataOutputStream(player2.getOutputStream());

                // let player1 know to start
                toPlayer1.writeInt(1);

                // Continuously serve the players and determine and report
                // the game status to the players
                while (true) {
                    // receive move from player1
                    int row = fromPlayer1.readInt();
                    int column = fromPlayer1.readInt();
                    board[row][column] = 'X';

                    // Check if player 1 wins
                    if (isWon('X')) {
                        toPlayer1.writeInt(1);
                        toPlayer2.writeInt(1);
                        sendMove(toPlayer2, row, column);
                        player1 = null;
                        player2 = null;
                        break;
                    }
                    else if (isFull()) {
                        // it's a tie
                        toPlayer1.writeInt(3);
                        toPlayer2.writeInt(3);
                        sendMove(toPlayer2, row, column);
                        player1 = null;
                        player2 = null;
                        break;
                    }
                    else {
                        // it's player 2's turn now
                        toPlayer2.writeInt(4);
                        sendMove(toPlayer2, row, column);
                    }

                    // receive move from player 2
                    row = fromPlayer2.readInt();
                    column = fromPlayer2.readInt();
                    board[row][column] = 'O';

                    if (isWon('O')) {
                        toPlayer1.writeInt(2);
                        toPlayer2.writeInt(2);
                        sendMove(toPlayer1, row, column);
                        player1 = null;
                        player2 = null;
                        break;
                    }
                    else {
                        // it's player 1's turn now
                        toPlayer1.writeInt(4);
                        sendMove(toPlayer1, row, column);

                    }
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Sends each player's moves to the other player
         *
         * @param out The output stream for the player to send moves to
         * @param row The row that the player put their piece into
         * @param column The column that the player put their piece into
         * @throws IOException
         */
        private void sendMove(DataOutputStream out, int row, int column) throws IOException {
            out.writeInt(row); // send row
            out.writeInt(column);
        }

        /**
         * Checks for a tie
         *
         * @return Returns true if the game board is full and there is a tie.
         * Returns false otherwise.
         */
        private boolean isFull() {

            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 7; j++) {
                    if (board[i][j] == ' ')
                        return false;
                }
            }
            return true;
        }

        /**
         * Checks for a win by checking for 4 in a row in the rows, columns,
         * and each diagonal.
         *
         * @param token Takes in the player's game piece - X or O
         * @return Returns true if there is a win, false otherwise.
         */
        private boolean isWon(char token) {

            boolean rowWin = false;
            boolean colWin = false;
            boolean diagTLWin = false;
            boolean diagBLWin = false;

            // check rows
            for (int i = 0; i <= 5; i++) {
                for (int j = 0; j <=3; j++) {
                    if (board[i][j] == token && board[i][j+1] == token && board[i][j+2] == token
                            && board[i][j+3] == token) {
                        rowWin = true;
                        break;
                    }
                }
            }

            // check columns
            for (int i = 0; i <= 2; i++) {
                for (int j = 0; j <= 6; j++) {
                    if (board[i][j] == token && board[i+1][j] == token && board[i+2][j] == token
                    && board[i+3][j] == token) {
                        colWin = true;
                        break;
                    }
                }
            }

            // check diagonal from bottom left to top right
            for (int i = 0; i <=2; i++) {
                for (int j = 3; j <= 6; j++) {
                    if (board[i][j] == token && board[i+1][j-1] == token && board[i+2][j-2] == token
                    && board[i+3][j-3] == token) {
                        diagBLWin = true;
                        break;

                    }
                }
            }

            // check diagonal from top left to bottom right
            for (int i = 0; i <= 2; i++) {
                for (int j = 0; j <= 3; j++) {
                    if (board[i][j] == token && board[i+1][j+1] == token && board[i+2][j+2] == token
                    && board[i+3][j+3] == token) {
                        diagTLWin = true;
                        break;

                    }
                }
            }

            if (rowWin || colWin || diagBLWin || diagTLWin) {
                return true;
            }
            else
                return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

