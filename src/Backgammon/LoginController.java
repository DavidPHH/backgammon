package Backgammon;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    public TextField txtPlayer1;
    public TextField txtPlayer2;
    public TextField txtPort;
    public CheckBox checkbox;

    public void click(ActionEvent actionEvent) throws IOException {
        String p1 = txtPlayer1.getText();
        String p2 = txtPlayer2.getText();
        if (!checkbox.isSelected()) {
            // TODO connect to server
            return;
        }

        if (p1.equals(""))
            p1 = "Player 1";
        if (p2.equals(""))
            p2 = "Player 2";

        Main.players[0] = new Player(p1, Color.WHITE);
        Main.players[1] = new Player(p2, Color.BLACK);

        // Swap scenes
        Parent root = FXMLLoader.load(getClass().getResource("board.fxml"));
        Scene scene = new Scene(root, 1000, 715);
        scene.getStylesheets().addAll(this.getClass().getResource("application.css").toExternalForm());
        Main.window.setScene(scene);

    }

    public void checkBox(ActionEvent actionEvent) {
        boolean vis = !checkbox.isSelected();
        txtPort.setVisible(vis);
        txtPlayer2.setPromptText(vis ? "Server IP" : "Player 2");
        txtPlayer2.setText("");
    }
}
