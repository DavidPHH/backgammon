package Backgammon;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

enum Color{
    NONE,
    WHITE,
    BLACK
}

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("board.fxml"));
        primaryStage.setTitle("Backgammon");
        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().addAll(this.getClass().getResource("application.css").toExternalForm());
        //Done temporarily
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
