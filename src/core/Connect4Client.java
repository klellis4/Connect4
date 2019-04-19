package core;

import javafx.scene.shape.Circle;
import java.io.*;
import java.net.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.effect.InnerShadow;

/**
 * This class sets up the Connect4 Client that can then connect to a Connect4 Server
 * and play against another player.
 *
 * @author Kelly Ellis, Hassan/Bansal
 * @version 1.0
 */

public class Connect4Client extends Application {

    private boolean myTurn = false;
    private char myToken = ' ';
    private char otherToken = ' ';
    private Label title = new Label();
    private Label status = new Label();
    private int rowSelected;
    private int columnSelected;
    private DataInputStream fromServer;
    private DataOutputStream toServer;

    private boolean continueToPlay = true;
    private boolean waiting = true;
    private String host = "localhost";

    private Cell[][] cell = new Cell[6][7];

    @Override
    /**
     * Creates the game board to be played on.
     */
    public void start(Stage primaryStage) {

        GridPane pane = new GridPane();

        for (int c = 0; c < 7; c++) {
            for (int r = 0; r < 6; r++) {
                pane.add(cell[r][c] = new Cell(r, c), c, r);
            }
        }

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(title);
        borderPane.setCenter(pane);
        borderPane.setBottom(status);

        Scene scene = new Scene(borderPane, 700,700);
        primaryStage.setTitle("Connect 4");
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
    }

    /**
     * Connects the client to the server and sets up I/O streams. Then
     * creates a new thread to control and play the game
     */
    public void connectToServer() {
        try {
            Socket socket = new Socket(host, 8004);
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        // control the game on a separate thread
        new Thread(() -> {
            try {
                // get notification from server
                int player = fromServer.readInt();

                // am i player 1 or 2?
                if (player == 1) {
                    myToken = 'X';
                    otherToken = 'O';
                    Platform.runLater(() -> {
                        title.setText("Player 1 - Your color is red");
                        status.setText("Waiting for second player to join...");
                    });

                    // receive startup notification from server
                    fromServer.readInt(); // ignored

                    // other player joined
                    Platform.runLater(() ->
                            status.setText("Player 2 has joined. You play first. " +
                                    "Click on a circle to place your game piece there."));

                    // it is my turn
                    myTurn = true;
                }
                else if (player == 2) {
                    myToken = 'O';
                    otherToken = 'X';
                    Platform.runLater(() -> {
                        title.setText("Player 2 - Your color is yellow");
                        status.setText("Waiting for Player 1 to move...");
                    });
                }

                // continue to play
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
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    /**
     * Waits for the player's move
     *
     * @throws InterruptedException When the thread is interrupted
     */
    private void waitForPlayerAction() throws InterruptedException {
        while (waiting) {
            Thread.sleep(100);
        }
        waiting = true;
    }

    /**
     * Sends the player's selected row and column to the server
     *
     * @throws IOException
     */
    private void sendMove() throws IOException {
        toServer.writeInt(rowSelected);
        toServer.writeInt(columnSelected);
    }

    /**
     * Receives information from the server about the game status when
     * there's a winner, a tie, or to let each player know when to move.
     *
     * @throws IOException
     */
    private void receiveInfoFromServer() throws IOException {
        // receive game status
        int gameStatus = fromServer.readInt();

        if (gameStatus == 1) {
            // player 1 won, stop playing
            continueToPlay = false;
            if (myToken == 'X') {
                Platform.runLater(() -> status.setText("You won!"));
            }
            else if (myToken == 'O') {
                Platform.runLater(() -> status.setText("Player 1 won!"));
                receiveMove();
            }
        }
        else if (gameStatus == 2) {
            // player 2 won, stop playing
            continueToPlay = false;
            if (myToken == 'O') {
                Platform.runLater(() -> status.setText("You won!"));
            }
            else if (myToken == 'X') {
                Platform.runLater(() -> status.setText("Player 2 won!"));
                receiveMove();
            }
        }
        else if (gameStatus == 3) {
            // game is tied
            continueToPlay = false;
            Platform.runLater(() -> status.setText("It's a tie!"));
            if (myToken == 'O') {
                receiveMove();
            }

        }
        else {
            receiveMove();
            Platform.runLater(() -> status.setText("Your turn - click on a circle to place your game piece there."));
            myTurn = true;
        }
    }

    /**
     * Receives a move from the other player from the server, and
     * updates the game board with that player's move.
     *
     * @throws IOException
     */
    private void receiveMove() throws IOException {
        // get other player's move
        int row = fromServer.readInt();
        int column = fromServer.readInt();
        Platform.runLater(() -> {
            cell[row][column].setToken(otherToken);
        });

    }

    /**
     * Inner cell class that helps with the game board set up and
     * allows for a game piece to be placed
     */
    public class Cell extends Pane {
        private int row;
        private int column;

        private char token = ' ';

        /**
         * Creates the cells that are placed on the game board. Cells are squares
         * with circles inside them.
         *
         * @param row The row that cell is placed at
         * @param column The column that cell is placed at
         */
        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
            this.setPrefSize(600, 600);

            setStyle("-fx-background-color: blue; -fx-border-color: darkblue");

            InnerShadow effect = new InnerShadow();
            effect.setOffsetX(4.0f);
            effect.setOffsetY(4.0f);
            this.setEffect(effect);

            Circle circle = new Circle(45);
            circle.setCenterY(50);
            circle.setCenterX(50);
            circle.setTranslateY(5);
            circle.setTranslateX(3);
            circle.setFill(Color.WHITE);
            circle.setStroke(Color.BLACK);
            this.getChildren().addAll(circle);
            this.setOnMouseClicked(e -> handleMouseClick());
        }

        /**
         * Drops the player's piece
         *
         * @param c Takes in the player's token - X or O
         */
        public void setToken(char c) {
            token = c;
            repaint();
        }

        /**
         * Helper method that creates the colored game pieces when a player moves.
         */
        protected void repaint() {
            // red
            if (token == 'X') {
                Circle circle = new Circle(40);
                circle.setCenterY(50);
                circle.setCenterX(50);
                circle.setFill(Color.RED);

                circle.setTranslateY(5);
                circle.setTranslateX(3);

                InnerShadow shadow = new InnerShadow();
                shadow.setOffsetX(5.0f);
                shadow.setOffsetY(5.0f);
                circle.setEffect(shadow);

                this.getChildren().addAll(circle);
            }
            // yellow
            else if (token == 'O') {
                Circle circle = new Circle(40);
                circle.setCenterX(50);
                circle.setCenterY(50);
                circle.setFill(Color.YELLOW);

                circle.setTranslateY(5);
                circle.setTranslateX(3);

                InnerShadow shadow = new InnerShadow();
                shadow.setOffsetX(5.0f);
                shadow.setOffsetY(5.0f);
                circle.setEffect(shadow);

                getChildren().add(circle);
            }
        }

        /**
         * Mouse Click handler when a player wants to drop a piece. It drops
         * that player's piece by using the helper method setToken()
         */
        private void handleMouseClick() {
            if (token == ' ' && myTurn) {
                setToken(myToken);
                myTurn = false;
                rowSelected = row;
                columnSelected = column;
                status.setText("Waiting for the other player to move");
                waiting = false;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
