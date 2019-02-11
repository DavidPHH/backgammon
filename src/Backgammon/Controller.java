package Backgammon;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import Backgammon.Classes.Board;
import javafx.scene.text.Text;
import javafx.stage.Stage;
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
        GridPane[] p = {Q2, Q1, Q3, Q4};
        Board.setInitialpos(p);

        //Default gameInfo string to be displayed
        gameInfo.setText("\nGame commands:\n" +
                "1. /quit\n" +
                "2. /commands\n" +
                "3. /move (origin: int) (destination: int)\n" +
                "4. /tests (used to move pieces in sprint 1)\n" +
                "Game information will be displayed here\n" +
                "Input / before commands:\n\n" +
                "Finally, click on the 'i' button above to open/close this section.\n");

        //Initialising the positions of the infoButton and the user input text field
        GridPane.setValignment(infoButton, VPos.CENTER);
        GridPane.setHalignment(infoButton,HPos.CENTER);
        GridPane.setHalignment(pCommands,HPos.LEFT);
        GridPane.setValignment(pCommands,VPos.BOTTOM);
        GridPane.setValignment(gameInfo,VPos.TOP);
        GridPane.setHalignment(gameInfo,HPos.RIGHT);

        //Initialising the text area so that it doesn't interfere with the board itself.
        gameInfo.setEditable(false);

        for(int i = 0;i < 2;i++){
            players[i] = new Player("Player "+(i+1));
        }
        playerOne.getChildren().add(new Text(players[0].getPlayerName()+"\nPips:"+players[0].getPipsLeft()));
        playerTwo.getChildren().add(new Text(players[1].getPlayerName()+"\nPips:"+players[1].getPipsLeft()));

        //Event Handlers on info button to display gameInfo on hover
        infoButton.addEventHandler(MouseEvent.ANY, e -> {
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
                try{
                    org = Integer.parseInt(splot[1]) - 1;
                    dest = Integer.parseInt(splot[2]) - 1;
                    if(org < 0 || dest < 0 || org > 23 || dest > 23)
                        throw new ArrayIndexOutOfBoundsException();
                }
                catch (Exception ex){
                    gameInfo.appendText("\nInvalid syntax. Expected /move int int");
                    break;
                }

                Move move = new Move(org, dest, Board.getStrip(org).pieceColor);
                gameInfo.appendText("\n"+ move);
                Board.makeMove(move);
                break;
            case "/test" :

                Strip blackBar =  new Strip(blackBarVBox, 24);      //for the moment just stored in 24
                //because it was the next available index, but should probably change later for gameplay reasons
                Strip blackBearOff =  new Strip(blackBearOffVBox, 25); //same as above, 25 may be changed

                Strip whiteBar =  new Strip(whiteBarVBox, 26);
                Strip whiteBearOff =  new Strip(whiteBearOffVBox, 27);



                Thread t = new Thread(() -> {

                    //for(int i=0; i<2; i++) {      //possible to avoid duplicating white/black test code with loop?
                                                    //tried but IDE didn't like the fact that i wasn't final
                                                    //when used in situations like bearOff[i].pop()

                    //blacks test run code
                    try {
                        Platform.runLater(() -> blackBar.insert(new Piece(Color.BLACK)));
                        Thread.sleep(800);
                        Platform.runLater(() -> blackBar.pop());
                        Platform.runLater(() -> Board.insertToStrip(new Piece(Color.BLACK), 0));

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                    for (int i = 0; i < 23; i++) {
                        Move m = new Move(i, i + 1, Color.BLACK);
                        try {
                            Thread.sleep(500);
                            Platform.runLater(() -> Board.testMove(m));
                        } catch (Exception ex) {
                            break;
                        }
                    }

                    try {
                        Thread.sleep(500);
                        Platform.runLater(() -> Board.getStrip(23).pop());
                        Platform.runLater(() -> blackBearOff.insert(new Piece(Color.BLACK)));
                        Thread.sleep(1500);
                        Platform.runLater(() -> blackBearOff.pop());
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                    //white's test run starts here
                    try {

                        Platform.runLater(() -> whiteBar.insert(new Piece(Color.WHITE)));
                        Thread.sleep(800);
                        Platform.runLater(() -> whiteBar.pop());
                        Platform.runLater(() -> Board.insertToStrip(new Piece(Color.WHITE), 23));

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                    for (int i = 23; i > 0; i--) {
                        Move m = new Move(i, i - 1, Color.WHITE);
                        try {
                            Thread.sleep(500);
                            Platform.runLater(() -> Board.testMove(m));
                        } catch (Exception ex) {
                            break;
                        }
                    }

                    try {
                        Thread.sleep(500);
                        Platform.runLater(() -> Board.getStrip(0).pop());
                        Platform.runLater(() -> whiteBearOff.insert(new Piece(Color.WHITE)));
                        Thread.sleep(1500);
                        Platform.runLater(() -> whiteBearOff.pop());
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    //}

                });
                t.start();
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

            Text txt = new Text("No pieces\nleft in\nthis strip");
            //This statement doesn't stop the constant inputting of strings
            if(!box.getChildren().contains(txt)){
                box.setStyle("-fx-background-color: red;-fx-opacity: .5");
                box.getChildren().add(txt);
            }

            PauseTransition removeAfter = new PauseTransition(Duration.seconds(2));
            PauseTransition removeColour = new PauseTransition(Duration.seconds(2));
            removeAfter.setOnFinished(e -> box.getChildren().clear());
            removeColour.setOnFinished((e -> box.setStyle(null)));

            removeAfter.play();
            removeColour.play();
        } else
            strip.pop();
    }
}