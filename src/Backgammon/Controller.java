package Backgammon;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;


public class Controller {

    @FXML public GridPane Q1;
    @FXML public GridPane Q2;
    @FXML public GridPane Q3;
    @FXML public GridPane Q4;

    //Player commands textfield
    @FXML
    private TextField pCommands;
    @FXML
    private TextArea gameInfo;
    @FXML
    private Button infoButton;

    private StringBuilder textString = new StringBuilder();
    private Boolean vis = true;
    private VBox[] slitArray;

    public static class Board{


    }
    public class slit{
        public slit(){
            GridPane[] p = {Q1, Q2, Q3, Q4};
            slitArray = new VBox[24];                    // MOVE THIS TO CLASS
            for (GridPane pane: p) {                     //Like this?
                for (int i = 0; i < pane.getChildren().size(); i++) {
                    slitArray[i] = (VBox) Q1.getChildren().get(i);
                }
            }
        }
    }

    /*Function for user input in the text field */
    @FXML
    public void onEnter(ActionEvent e){
        String inputString = pCommands.getText().toLowerCase();
        //User wants to end program
        if(inputString.equals("\\quit")){
            Platform.exit();
        }
        //User wants command list
        else if(inputString.equals("\\commands")){
            textString.append("\n").append(pCommands).append("\n").append("Game commands\n______________\n1." +
                    " \\Quit\n2. \\Commands");
            gameInfo.setText(textString.toString());
            pCommands.setText("");
        }
        //User makes a moves
        else if(inputString.startsWith("\\move ")){
            /*
            makeMove function here
             */
            System.out.println("Function not present");
        }
        else if(inputString.equals("\\test")){
            /*Put test function here for sprint 1 test requirement*/
        }
        //Echoes user input per sprint requirements
        else if(!inputString.equals("")){
            textString.append("\n").append(pCommands.getText());
            gameInfo.setText(textString.toString());
            gameInfo.setScrollTop(640);
            pCommands.setText("");
        }
    }

    /*Function for the information button
        Changes visibility of the text area
     */
    @FXML
    public void infoB(){
        if(vis){
            gameInfo.setVisible(false);
            infoButton.setStyle("-fx-background-color: yellow");
            vis = false;
        }
        else{
            gameInfo.setVisible(true);
            infoButton.setStyle("-fx-background-color: lightgrey");
            vis = true;
        }
    }

    public void initialize(){
        //Default gameInfo string to be displayed
        textString.append(pCommands.getText()).append("\n").append("Game commands\n______________\n1." +
                " \\Quit\n2. \\Commands\n").
                append("Game information will be displayed here\nInput \\ before commands \n");
        gameInfo.setText(textString.toString());
        gameInfo.setEditable(false);

        /* Event handlers on button
            Shows gameInfo text area when mouse hovers over the info button
            Disappears when mouse leaves
            Done this way as I couldn't get the CSS code to work, left in application.css commented out
            if someone wants to take a look
         */
        infoButton.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<>(){
                    @Override
                    public void handle(MouseEvent e){
                        if(!vis)
                            gameInfo.setVisible(true);
                    }
                });

        infoButton.addEventHandler(MouseEvent.MOUSE_EXITED,new EventHandler<>(){
            @Override
            public void handle(MouseEvent e){
                if(!vis)
                    gameInfo.setVisible(false);
            }
        });
    }

    public void click(MouseEvent event) {
//        VBox box = (VBox) event.getSource();
//        Image image = new Image("Backgammon/res/piece-black.png");
//        ImageView img = new ImageView();
//        img.setImage(image);
//        System.out.println(event.getSource());
//        box.getChildren().add(img);
//        img.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<>() {
//
//            @Override
//            public void handle(MouseEvent event) {
//                //something
//            }
//        });

//        String id = event.getPickResult().getIntersectedNode().getId();
//        System.out.println(id);

    }
}
