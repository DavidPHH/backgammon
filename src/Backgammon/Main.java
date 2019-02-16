package Backgammon;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

enum Color{
    WHITE(0),
    BLACK(1),
    NONE(2);
    private final int value;
    Color(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}

public class Main extends Application {
    static Player players[] = new Player[2];
    static Stage window;
    @Override
    public void start(Stage primaryStage) throws Exception{
        window = primaryStage;
        window.setTitle("Backgammon");
        window.setResizable(false);  // Done temporarily

        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root, 1000, 715);
        scene.getStylesheets().addAll(this.getClass().getResource("application.css").toExternalForm());
        window.setScene(scene);
        window.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
