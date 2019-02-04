package Backgammon;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        int offset = 0;
        for (GridPane pane: p) {
            for (int i = 0; i < pane.getChildren().size(); i++) {
                slitArray[i+6*offset] = (VBox) pane.getChildren().get(i);
                //System.out.println(i+6*offset);
            }
            offset++;
        }

        Image black = new Image("Backgammon/res/piece-black.png");
        Image white = new Image("Backgammon/res/piece-white.png");
        ImageView[] startB = new ImageView[15];
        ImageView[] startW = new ImageView[15];

        for(int i=0; i<15; i++)
        {
            startB[i] = new ImageView();
            startB[i].setImage(black);
        }
        for(int i=0; i<15; i++)
        {
            startW[i] = new ImageView();
            startW[i].setImage(white);
        }

        slitArray[0].getChildren().addAll(startB[0], startB[1], startB[2], startB[3], startB[4]);
        slitArray[4].getChildren().addAll(startW[0], startW[1], startW[2]);
        slitArray[6].getChildren().addAll(startW[3], startW[4], startW[5], startW[6], startW[7]);
        slitArray[11].getChildren().addAll(startB[5], startB[6]);
        slitArray[12].getChildren().addAll(startW[8], startW[9], startW[10], startW[11], startW[12]);
        slitArray[16].getChildren().addAll(startB[7], startB[8], startB[9]);
        slitArray[18].getChildren().addAll(startB[10], startB[11], startB[12], startB[13], startB[14]);
        slitArray[23].getChildren().addAll(startW[13], startW[14]);



    }

   // @FXML
    //VBox test;

    /*public void click(MouseEvent event) {
        VBox box = (VBox) event.getSource();
        Image image = new Image("Backgammon/res/piece-black.png");
        ImageView img = new ImageView();
        img.setImage(image);
        System.out.println(event.getSource());
        box.getChildren().add(img);
        img.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<>() {

            @Override
            public void handle(MouseEvent event) {
                //something
            }
        });

        String id = event.getPickResult().getIntersectedNode().getId();
        System.out.println(id);

    }*/

    public void click(MouseEvent event) {
        VBox box = (VBox) event.getSource();
        box.getChildren().remove(box.getChildren().size()-1);
      }


}
