package Backgammon;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import java.io.IOException;

public class Winscreen {

    public Label winnerName;
    public Label player1;
    public Label player2;

    void setup(Player winner, Player loser) {
        winnerName.setText(winner.getPlayerName().toUpperCase() + " WINS!");
        player1.setText(winner.getScore() > loser.getScore() ? winner.toString() : loser.toString());
        player2.setText(loser.getScore() < winner.getScore() ? loser.toString() : winner.toString());
        winner.reset();
        loser.reset();

    }

    public void click(ActionEvent actionEvent) throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("board.fxml")), 1000, 715);
        scene.getStylesheets().addAll(this.getClass().getResource("application.css").toExternalForm());
        Main.window.setScene(scene);
    }
}
