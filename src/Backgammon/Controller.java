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
import java.util.Random;



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
    @FXML
    private VBox diceBox;

    private Player[] players;
    private Boolean vis;
    private Boolean gameStart;
    private Boolean hasRolled;

    public void initialize() {
        players = Main.players;
        VBox[] bar = {whiteBarVBox, blackBarVBox};
        VBox[] bearOff = {whiteBearOffVBox, blackBearOffVBox};
        GridPane[] quadrants = {Q2, Q1, Q3, Q4};
        Board.setInitialpos(quadrants, bar, bearOff);
        //Default gameInfo string to be displayed
        gameInfo.setText("\nGame commands:" +
                "\n1. /start to start the game"+
                "\n2. /next to pass turn to other player" +
                "\n3. /quit" +
                "\n4. /commands" +
                "\n5. /move (origin: int) (destination: int)" +
                "\n6. /test (used to move pieces in sprint 1)" +
                "\n7. /dice for, well, dice stuff" +
                "\n" +
                "Finally, click on the 'i' button above to open/close this section.\n");

        // Initialising the boolean variables
        vis = true;
        gameStart = false;
        //TODO in sprint 3, change hasRolled to initialise to false
        hasRolled = true;

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
                        "\n1. /start to start the game"+
                        "\n2. /next to pass turn to other player" +
                        "\n3. /quit" +
                        "\n4. /commands" +
                        "\n5. /move (origin: int) (destination: int)" +
                        "\n6. /test (used to move pieces in sprint 1)" +
                        "\n7. /dice for, well, dice stuff" +
                        "\n");
                pCommands.setText("");
                break;
            case "/move":
                pCommands.setText("");
                if(Board.currentMoves < 2){
                    String[] splot = inputString.split(" "); // Did you really use splot as the past tense of split?  ...I like it.
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
                    Move move = new Move(org, dest, Board.currentTurn);
                    gameInfo.appendText("\n" + move);
                    Board.makeMove(move);
                }
                else{
                    gameInfo.appendText("\nYou cannot move again, please type /next to allow the next player to move");
                }
                break;
            case "/start":
                pCommands.setText("");
                if(!gameStart) {
                    Board.rollStart(players);
                    gameInfo.appendText("\n"+players[0].getPlayerName() +" rolled: "+Board.die.getDice1()+", "
                            +players[1].getPlayerName()+" rolled: " +Board.die.getDice2()+"\n");
                    if(players[0].getColor() == Board.currentTurn)
                        gameInfo.appendText("\n" + players[0].getPlayerName()+"'s turn");

                    else
                        gameInfo.appendText("\n" + players[1].getPlayerName()+"'s turn");

                }

                gameStart = true;
                hasRolled = true;
                break;
            case "/roll":
                pCommands.setText("");
                if(!hasRolled){
                    Board.rollDice();

                    //Printing of the results of the player's roll
                    if(players[0].getColor() == Board.currentTurn)
                    {
                        gameInfo.appendText("\n" + players[0].getPlayerName() +
                                " rolled: " + Board.die.getDice1() + ", " + Board.die.getDice2()+"\n");
                    }
                    else{
                        gameInfo.appendText("\n" + players[1].getPlayerName() +
                                " rolled: " + Board.die.getDice1() + ", " + Board.die.getDice2()+"\n");
                    }
                    hasRolled = true;
                }
                else{
                    gameInfo.appendText("\nYou cannot roll again\n");
                }
                break;
            case "/next":
                pCommands.setText("");
                Board.nextTurn();
                // Printing the new player's turn
                if(players[0].getColor() == Board.currentTurn)
                    gameInfo.appendText("\n" + players[0].getPlayerName()+"'s turn");
                else
                    gameInfo.appendText("\n" + players[1].getPlayerName()+"'s turn");
                Board.rollDice();

                /* Printing of the results of the player's roll. This is only here for sprint 2 as required */
                if(players[1].getColor() == Board.currentTurn)
                {
                    gameInfo.appendText("\n" + players[1].getPlayerName() +
                            " rolled: " + Board.die.getDice1() + ", " + Board.die.getDice2()+"\n");
                }
                else{
                    gameInfo.appendText("\n" + players[0].getPlayerName() +
                            " rolled: " + Board.die.getDice1() + ", " + Board.die.getDice2()+"\n");
                }
                break;
            case "/test":       //produces IndexOutOfBoundsException when running too many at once
                pCommands.setText("");
                gameInfo.appendText("\nRunning test...");
                new Thread(() -> {
                    test(Color.BLACK);
                    test(Color.WHITE);
                }).start();
                break;
            case "/dice":
                Random rand = new Random();
                DiceFace[] dice = new DiceFace[7];      // no particular reason it's 7, other than that's just what I
                new Thread(() -> {                      // felt looked best
                    int n;
                    for (int i = 0; i < 7 ; i++) {
                        do {                                        // included just so it wouldn't generate
                            n = rand.nextInt(6) + 1;                // repeat numbers in a row
                        } while (i > 0 && n == dice[i - 1].number);
                        dice[i] = new DiceFace(n);
                    }
                    int i=0;
                    for(DiceFace d: dice){
                        try {
                            if(i==0 && !diceBox.getChildren().isEmpty())
                                Platform.runLater(() ->  diceBox.getChildren().remove(0));  // removes existing picture
                            Platform.runLater(() -> diceBox.getChildren().add(d.imgView));  // if one's already there
                            Thread.sleep(100 + (60*i++));   // pauses for a longer amount of time after each change
                            if(i!=dice.length)
                                Platform.runLater(() ->  diceBox.getChildren().remove(0)); // doesn't remove the final result
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }

                    }

                }).start();
                break;

            default:
                gameInfo.appendText("\n" + pCommands.getText());
                pCommands.setText("");
                break;
        }
    }

    // Toggles the text area when info Button is clicked
    @FXML
    public void infoB() {
        gameInfo.setMouseTransparent(vis);
        gameInfo.setVisible(!vis);
        vis = !vis;
    }

    // precursor to eventual feature of user being able to make their moves through the GUI
    // as well as through the commands textField
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
            int finalY = y;
            Platform.runLater(() -> Board.getStrip(finalY).pop());
            Platform.runLater(() -> Board.BearOff.insert(testPiece));
            Thread.sleep(1500);
            Platform.runLater(() -> Board.BearOff.remove(testPiece.color));

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}