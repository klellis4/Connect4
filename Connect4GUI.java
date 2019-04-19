package ui;
import core.Connect4;
import core.Connect4ComputerPlayer;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is for the Connect4 GUI application. It allows the user
 * to play against another player or against the computer.
 *
 * @author Kelly Ellis
 * @version 2.0
 */
public class Connect4GUI extends Application{
    private boolean redTurn = true;
    private boolean turnSwitch;
    private Piece[][] board = new Piece[7][6];
    private Pane pane = new Pane();

    Connect4 connect4 = new Connect4();
    Connect4ComputerPlayer computer = new Connect4ComputerPlayer();

    /**
     * Sets up the Pane for the game and puts everything on it. For Player Vs. Computer game.
     *
     * @return Returns the pane that was made
     */
    public Parent computerGrid() {
        Pane grid = new Pane();
        Shape gridShape =  makeBoard();
        grid.getChildren().add(pane);
        grid.getChildren().add(gridShape);
        grid.getChildren().addAll(highlightsComputer());
        return grid;
    }

    /**
     * Creates the Connect4 game board to be played on
     *
     * @return Returns the board that was made
     */
    public Shape makeBoard() {
        Shape board = new Rectangle(800, 700);

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 7; x++) {
                Circle circle = new Circle(50);
                circle.setCenterX(50);
                circle.setCenterY(50);
                circle.setTranslateX(x * 105 + 25);
                circle.setTranslateY(y * 105 + 25);

                board = Shape.subtract(board, circle);

                InnerShadow effect = new InnerShadow();
                effect.setOffsetX(10.0f);

                board.setEffect(effect);

                StackPane stack = new StackPane();
                stack.getChildren().addAll(circle, board);
            }
        }
        board.setFill(Color.DARKBLUE);
        return board;
    }

    /**
     * Creates the highlights for the columns when the mouse is moved on them. Also
     * contains the game logic. When a highlighted column is clicked on, a game piece
     * is dropped into that column, and it allows the game to continue being played
     * until a win or a tie is found.
     *
     * @return Returns the highlighted columns as a list
     */
    public List<Rectangle> highlightsComputer() {
        List<Rectangle> highlight = new ArrayList<>();

        // creates highlights for each column
        for (int x = 0; x < 7; x++) {
            Rectangle columnSelected = new Rectangle(100, 700);
            columnSelected.setTranslateX(x * (105) + 25);
            columnSelected.setFill(Color.TRANSPARENT);
            columnSelected.setOnMouseEntered(e -> columnSelected.setFill(Color.rgb(50, 200, 50, 0.3)));
            columnSelected.setOnMouseExited(e -> columnSelected.setFill(Color.TRANSPARENT));

            // Drops piece and plays game when column is clicked
            final int column = x;
            columnSelected.setOnMouseClicked(e -> {
                dropPiece(new Piece(redTurn), column);
                turnSwitch = !turnSwitch;
                char playerTurn;
                if (turnSwitch) {
                    playerTurn = 'X';
                }
                else {
                    playerTurn = 'O';
                }

                connect4.dropPiece('X', column + 1); // background game to check for win

                if (connect4.checkWin()) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Winner");
                    alert.setHeaderText(null);
                    if (playerTurn == 'X'){
                        alert.setContentText("Computer won!");
                        alert.showAndWait();

                    }
                    else if (playerTurn == 'O') {
                        alert.setContentText("You won!");
                        alert.showAndWait();
                    }
                    return;
                }

                if (connect4.boardIsFull()) {
                    Alert alert2 = new Alert(AlertType.INFORMATION);
                    alert2.setTitle("Tie");
                    alert2.setHeaderText(null);
                    alert2.setContentText("It's a tie!");
                    alert2.showAndWait();
                    return;
                }

                int col = computer.chooseColumn();
                computer.dropComputerPiece('O', col + 1); // background game
                dropPiece(new Piece(!redTurn), col);

                if (connect4.checkWin()) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Winner");
                    alert.setHeaderText(null);
                    if (playerTurn == 'X'){
                        alert.setContentText("Computer won!");
                        alert.showAndWait();
                    }
                    else if (playerTurn == 'O') {
                        alert.setContentText("You won!");
                        alert.showAndWait();
                    }
                    return;
                }

                if (connect4.boardIsFull()) {
                    Alert alert2 = new Alert(AlertType.INFORMATION);
                    alert2.setTitle("Tie");
                    alert2.setHeaderText(null);
                    alert2.setContentText("It's a tie!");
                    alert2.showAndWait();
                    return;
                }
            });

            highlight.add(columnSelected);
        }
        return highlight;
    }

    /**
     * Drops the player's game piece into the selected column
     *
     * @param piece Takes in the player's colored game piece to be dropped.
     * @param column Takes in the column that was clicked on.
     */
    public void dropPiece(Piece piece, int column) {
        int row = 5;
        do {
            // Decrease rows until an empty valid one is found
            if (!getPiece(column, row).isPresent())
                break;
            row--;
        }
        while (row >= 0);

        if (row < 0)
            return;

        board[column][row] = piece;
        pane.getChildren().add(piece);
        piece.setTranslateX(column * 105 + 25);

        TranslateTransition drop = new TranslateTransition(Duration.seconds(0.7), piece);
        drop.setToY(row * 105 + 25);
        drop.play();
    }

    /**
     * Helper method for dropPiece method
     *
     * @param column Column clicked on
     * @param row The potential row for a piece to be dropped into
     * @return Returns an empty optional if the column chosen isn't valid.
     * Returns the value of the chosen column and row otherwise.
     */
    public Optional<Piece> getPiece(int column, int row) {
        if (column < 0 || column >= 7 || row < 0 || row >= 6) {
            return Optional.empty();
        }
        return Optional.ofNullable(board[column][row]);
    }

    /**
     * Creates the game pieces that are dropped into the board.
     */
    public static class Piece extends Circle {
        private final boolean red;
        public Piece(boolean red) {
            // change piece's color for each player
            super(45, red ? Color.RED : Color.YELLOW);
            this.red = red;
            setCenterX(50);
            setCenterY(50);
            InnerShadow shadow = new InnerShadow();
            shadow.setOffsetX(5.0f);
            shadow.setOffsetY(5.0f);
            setEffect(shadow);
        }
    }

    /**
     * Sets up the pane for the game and puts the game board on it. For Player Vs Player game.
     *
     * @return Returns the pane that was made
     */
    public Parent playerGrid() {
        Pane grid = new Pane();
        Shape gridShape =  makeBoard();
        grid.getChildren().add(pane);
        grid.getChildren().add(gridShape);
        grid.getChildren().addAll(highlightsPlayer());
        return grid;
    }

    /**
     * Creates the highlights for the selected columns when the mouse moves on it for
     * the Player Vs Player game, and allows for continuous play until someone wins or
     * the game is tied.
     *
     * @return Returns the column highlights as a list
     */
    public List<Rectangle> highlightsPlayer() {
        List<Rectangle> highlight = new ArrayList<>();

        // make highlights for each column
        for (int x = 0; x < 7; x++) {
            Rectangle columnSelected = new Rectangle(100, 700);
            columnSelected.setTranslateX(x * (105) + 25);
            columnSelected.setFill(Color.TRANSPARENT);
            columnSelected.setOnMouseEntered(e -> columnSelected.setFill(Color.rgb(50, 200, 50, 0.3)));
            columnSelected.setOnMouseExited(e -> columnSelected.setFill(Color.TRANSPARENT));


            // Drop piece and play game when a column is clicked on
            final int column = x;
            columnSelected.setOnMouseClicked(e -> {
                dropPiece(new Piece(redTurn), column);
                turnSwitch = !turnSwitch;
                char playerTurn;
                if (turnSwitch) {
                    playerTurn = 'X';
                }
                else {
                    playerTurn = 'O';
                }
                connect4.dropPiece(playerTurn, column + 1); // game running in background
                redTurn = !redTurn;
                if (connect4.checkWin()) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Winner");
                    alert.setHeaderText(null);
                    if (playerTurn == 'X'){
                        alert.setContentText("Red player won!");
                        alert.showAndWait();
                    }
                    else if (playerTurn == 'O') {
                        alert.setContentText("Yellow player won!");
                        alert.showAndWait();
                    }
                }
                if (connect4.boardIsFull()) {
                    Alert alert2 = new Alert(AlertType.INFORMATION);
                    alert2.setTitle("Tie");
                    alert2.setHeaderText(null);
                    alert2.setContentText("It's a tie!");
                    alert2.showAndWait();
                }
            });
            highlight.add(columnSelected);
        }
        return highlight;
    }


    /**
     * Starts by asking the player to choose Player Vs. Player or Computer Vs. Player.
     * Then plays the game depending on which is chosen.
     *
     * @param stage The stage that everything is placed on.
     */
    public void start(Stage stage) {
        StackPane pane = new StackPane();
        Button player = new Button();
        Button computerBut = new Button();
        computerBut.setTranslateY(40);
        player.setText("Player Vs. Player Game");
        computerBut.setText("Computer Vs. Player Game");
        pane.getChildren().addAll(player, computerBut);
        Scene scene = new Scene(pane, 250, 250);
        stage.setTitle("Connect Four");
        stage.setScene(scene);
        stage.show();

        // player vs. player game
        player.setOnMouseClicked(e -> {
            stage.setScene(new Scene(playerGrid()));
            stage.setResizable(true);
            stage.show();

        });

        // computer vs. player game
        computerBut.setOnMouseClicked(e -> {
            stage.setScene(new Scene(computerGrid()));
            stage.setResizable(true);
            stage.show();

        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
