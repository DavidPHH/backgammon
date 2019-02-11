package Backgammon;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import Backgammon.Classes.Board;
import javafx.scene.text.Text;
import javafx.util.Duration;


public class Controller {

    @FXML
    public GridPane Q1;
    @FXML
    public GridPane Q2;
    @FXML
    public GridPane Q3;
    @FXML
    public GridPane Q4;
    @FXML
    private GridPane textAreaGrid;
    @FXML
    public VBox blackBarVBox;
    @FXML
    public VBox whiteBarVBox;
    @FXML
    public VBox blackBearOffVBox;
    @FXML
    public VBox whiteBearOffVBox;
    @FXML
    private GridPane paneId;

    //Player commands textfield
    @FXML
    private TextField pCommands;
    @FXML
    private TextArea gameInfo;
    @FXML
    private Button infoButton;
    @FXML
    private HBox playerOne;
    @FXML
    private HBox playerTwo;

    private Player[] players = new Player[2];
    private Boolean vis = true;

    public void initialize() {
        VBox[] bar = {whiteBarVBox, blackBarVBox};
        VBox[] bearOff = {whiteBearOffVBox, blackBearOffVBox};
        GridPane[] quadrants = {Q2, Q1, Q3, Q4};
        Board.setInitialpos(quadrants, bar, bearOff);
        //Default gameInfo string to be displayed
        gameInfo.setText("\nGame commands:\n" +
                "1. /quit\n" +
                "2. /commands\n" +
                "3. /move (origin: int) (destination: int)\n" +
                "4. /tests (used to move pieces in sprint 1)\n" +
                "Game information will be displayed here\n" +
                "Input / before commands:\n\n" +
                "Finally, click on the 'i' button above to open/close this section.\n");

        for (int i = 0; i < 2; i++) {
            players[i] = new Player("Player " + (i + 1));
        }
        playerOne.getChildren().add(new Text(players[0].getPlayerName() + "\nPips:" + players[0].getPipsLeft()));
        playerTwo.getChildren().add(new Text(players[1].getPlayerName() + "\nPips:" + players[1].getPipsLeft()));

        infoButton.addEventHandler(MouseEvent.ANY, e -> { // Game info is displayed while mouse hovers over info button.
            EventType ev = e.getEventType();
            EventType ex = MouseEvent.MOUSE_EXITED;
            EventType ent = MouseEvent.MOUSE_ENTERED;
            if (!vis && (ev.equals(ex) || ev.equals(ent))) {
                gameInfo.setVisible(!ev.equals(ex));
            }
        });
    }

    /*Function for user input in the text field */
    @FXML
    public void onEnter(ActionEvent e) {
        String inputString = pCommands.getText().toLowerCase();
        if (inputString.equals(""))
            return;
        switch (inputString.split(" ")[0]) {
            case "/quit":
                Platform.exit();
                break;
            case "/commands":
                gameInfo.appendText("\nGame commands:" +
                        "\n1. /quit" +
                        "\n2. /commands" +
                        "\n3. /move (origin: int) (destination: int)" +
                        "\n4. /test (used to move pieces in sprint 1)" +
                        "\n");
                pCommands.setText("");
                break;
            case "/move":
                pCommands.setText("");
                String[] splot = inputString.split(" ");    //Did you really use splot as the past tense of split?  ...I like it.
                int org, dest;
                try {
                    org = Integer.parseInt(splot[1]) - 1;
                    dest = Integer.parseInt(splot[2]) - 1;
                    if (org < 0 || dest < 0 || org > 23 || dest > 23)
                        throw new ArrayIndexOutOfBoundsException();
                } catch (Exception ex) {
                    gameInfo.appendText("\nInvalid syntax. Expected /move int int");
                    break;
                }

                Move move = new Move(org, dest, Board.getStrip(org).pieceColor);
                gameInfo.appendText("\n" + move);
                Board.makeMove(move);
                break;
            case "/test":
                pCommands.setText("");
                gameInfo.appendText("\nRunning test...");
                new Thread(() -> {
                    test(Color.BLACK);
                    test(Color.WHITE);
                }).start();
                break;

            default:
                gameInfo.appendText("\n" + pCommands.getText());
                pCommands.setText("");
                break;
        }
    }

    /*Function for the information button
        Changes visibility of the text area
     */
    @FXML
    public void infoB() {
        if (vis){
            gameInfo.setMouseTransparent(true);
            gameInfo.setVisible(false);
            infoButton.setStyle("-fx-background-color: yellow");
            vis = false;
        } else {
            gameInfo.setMouseTransparent(false);
            gameInfo.setVisible(true);
            infoButton.setStyle("-fx-background-color: lightgrey");
            vis = true;
        }
    }

    //precursor to eventual feature of user being able to make their moves through the GUI
    //as well as through the commands textField
    public void click(MouseEvent event) {
        VBox box = (VBox) event.getSource();
        Strip strip = Board.getStrip(box);
        if (strip == null)
            return;
        if (strip.quantity == 0) {
            if (box.getChildren().size() > 0)
                return;
            Text txt = new Text("No pieces\nleft in\nthis strip");
            box.setStyle("-fx-background-color: red;-fx-opacity: .5");
            box.getChildren().add(txt);
            PauseTransition removeAfter = new PauseTransition(Duration.seconds(2));
            PauseTransition removeColour = new PauseTransition(Duration.seconds(2));
            removeAfter.setOnFinished(e -> box.getChildren().clear());
            removeColour.setOnFinished((e -> box.setStyle(null)));

            removeAfter.play();
            removeColour.play();
        } else
            strip.pop();
    }

    private void test(Color color) {
        Piece testPiece = new Piece(color);
        int x = 0, y = 23, z = 1;
        if (color == Color.WHITE) {
            x = 23;
            y = 0;
            z = -1;
        }
        try {
            Platform.runLater(() -> Board.Bar.insert(testPiece));
            Thread.sleep(800);
            Platform.runLater(() -> Board.Bar.remove(testPiece.color));
            int finalX = x;
            Platform.runLater(() -> Board.insertToStrip(testPiece, finalX));
            for (int i = x; i != y; i += z) {
                Move m = new Move(i, i + z, color);
                Thread.sleep(500);
                Platform.runLater(() -> Board.testMove(m));
            }
            Thread.sleep(500);
            Platform.runLater(() -> Board.getStrip(23).pop());
            Platform.runLater(() -> Board.BearOff.insert(testPiece));
            Thread.sleep(1500);
            Platform.runLater(() -> Board.BearOff.remove(testPiece.color));

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}