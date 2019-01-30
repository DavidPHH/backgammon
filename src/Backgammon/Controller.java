package Backgammon;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;


public class Controller {

    @FXML public GridPane Q1;
    @FXML public GridPane Q2;
    @FXML public GridPane Q3;
    @FXML public GridPane Q4;

    public static class Board{


    }
    public class slit{

    }

    VBox[] slitArray;
    public void initialize(){
        GridPane[] p = {Q1, Q2, Q3, Q4};
        slitArray = new VBox[24];                    // MOVE THIS TO CLASS
        for (GridPane pane: p) {
            for (int i = 0; i < pane.getChildren().size(); i++) {
                slitArray[i] = (VBox) Q1.getChildren().get(i);
            }
        }
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
