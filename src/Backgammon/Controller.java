package Backgammon;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import Backgammon.Classes.Board;


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

    //Player commands textfield
    @FXML
    private TextField pCommands;
    @FXML
    private TextArea gameInfo;
    @FXML
    private Button infoButton;

    private Boolean vis = true;

    public void initialize() {
        GridPane[] p = {Q1, Q2, Q3, Q4};
        Board.setInitialpos(p);

        //Default gameInfo string to be displayed
        gameInfo.setText("\nGame commands:\n" +
                "1. /quit\n" +
                "2. /commands\n" +
                "Game information will be displayed here\n" +
                "Input / before commands:\n");
        gameInfo.setEditable(false);
        textAreaGrid.setMouseTransparent(true);

        /* Event handlers on button
            Shows gameInfo text area when mouse hovers over the info button
            Disappears when mouse leaves
            Done this way as I couldn't get the CSS code to work, left in application.css commented out
            if someone wants to take a look
         */
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
                        "\n");
                pCommands.setText("");
                break;
            case "/move":
                pCommands.setText("");
                String[] splot = inputString.split(" ");
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
        if (vis) {
            gameInfo.setVisible(false);
            infoButton.setStyle("-fx-background-color: yellow");
            vis = false;
        } else {
            gameInfo.setVisible(true);
            infoButton.setStyle("-fx-background-color: lightgrey");
            vis = true;
        }
    }

    public void click(MouseEvent event) {
        VBox box = (VBox) event.getSource();
        Strip strip = Board.getStrip(box);
        if (strip == null)
            return;
        if (strip.quantity == 0) {
            System.out.println("No pieces left in strip");
        } else
            strip.pop();

    }
}