package Backgammon;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

class Classes {

    static class Board {
        private static Strip[] stripArray = new Strip[24];
        static Color currentTurn;
        static Bar Bar;
        static Bar BearOff;

        static void setInitialpos(GridPane[] p, VBox[] bar, VBox[] bearOff) {
            Bar = new Bar(bar);
            BearOff = new Bar(bearOff);
            int offset = 0;
            for (GridPane pane : p) {
                for (int j = 0; j < pane.getChildren().size(); j++) {
                    int x = j + 6 * offset;
                    stripArray[x] = new Strip((VBox) pane.getChildren().get(offset == 0 || offset == 1 ? 5 - j : j), x);
                }
                offset++;
            }
            //for the top row, goes right to left assigning index values of 0-11
            //for the bottom row, goes left to right assigning index values of 12-23



            Piece[] black = new Piece[15];
            Piece[] white = new Piece[15];
            for (int i = 0; i < 15; i++) {
                black[i] = new Piece(Color.BLACK);
                white[i] = new Piece(Color.WHITE);
            }
            stripArray[0].insert(black, 5, 6);
            stripArray[5].insert(white, 3, 7);
            stripArray[7].insert(white, 0, 2);
            stripArray[11].insert(black, 0, 4);

            stripArray[12].insert(white, 8, 12);
            stripArray[16].insert(black, 7, 9);
            stripArray[18].insert(black, 10, 14);
            stripArray[23].insert(white, 13, 14);
        }

        static void insertToStrip(Piece piece, int stripID) {
            stripArray[stripID].insert(piece);
        }

        static Strip getStrip(int index) {
            return stripArray[index];
        }

        static Strip getStrip(VBox box) {
            for (Strip strip : stripArray) {
                if (strip.vBox.equals(box))
                    return strip;
            }
            return null;
        }

        static void makeMove(Move move) { //TODO add logic for pushing off pieces to the bar - ATM it's an invalid move if there's 1 piece.
            if (!validMove(move))
                return;
            stripArray[move.orgStrip].pop();
            stripArray[move.destStrip].insert(new Piece(move.color));
        }

        static void testMove(Move move) { // same as makeMove but allows both colors on the same strip for test purposes
            stripArray[move.orgStrip].pop();
            stripArray[move.destStrip].insert(new Piece(move.color));
        }

        static boolean validMove(Move move) {
            if (move.orgStrip < 0 || move.destStrip < 0 || move.orgStrip > 23 || move.destStrip > 23) // If outside of array, it's an invalid move
                return false;

            Strip dest = getStrip(move.destStrip);
            Strip org = getStrip(move.orgStrip);

            if (org.quantity == 0)   // If there is no piece to move, it's an invalid move
                return false;

            if ((org.pieceColor != dest.pieceColor)) // If the dest strip has pieces of the opposite color,
                return (dest.pieceColor == Color.NONE); // it's an invalid move
            return true;
        }

        static Move[] findAllvalidMoves() { // Maybe change this to some other method, depends what comes in handy
            return null;
        }

        static Color nextTurn() {
            currentTurn = currentTurn == Color.BLACK ? Color.WHITE : Color.BLACK;
            return currentTurn;
        }

    }
}

class Move {
    int orgStrip;
    int destStrip;
    Color color;

    Move(int orgStrip, int destStrip, Color color) {
        this.color = color;
        this.orgStrip = orgStrip;
        this.destStrip = destStrip;
    }

    @Override
    public String toString() {
        if (Classes.Board.validMove(this))
            return "Move: Origin: " + (orgStrip + 1) + " Destination: " + (destStrip + 1);
        else
            return "Invalid move";
    }
}

class Piece {
    Color color;
    ImageView imgView;

    Piece(Color color) {
        this.color = color;
        String url = ((color == Color.WHITE) ? "Backgammon/res/piece-white.png" : "Backgammon/res/piece-black.png");
        Image image = new Image(url);
        imgView = new ImageView();
        imgView.setImage(image);
        imgView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            //TODO what happens when a piece is clicked
        });
    }
}

class Strip {
    VBox vBox;
    int stripID; // Maybe not needed
    int quantity = 0; // Amount of pieces in this strip
    Color pieceColor = Color.NONE;

    Strip(VBox strip, int stripID) {
        this.vBox = strip;
        this.stripID = stripID;
    }

    void insert(Piece[] pieces, int start, int stop) {
        for (int i = start; i <= stop; i++) {
            insert(pieces[i]);
        }
    }

    void insert(Piece piece) {
        pieceColor = piece.color;
        vBox.getChildren().add(piece.imgView);
        quantity++;
    }

    void pop() {
        vBox.getChildren().remove(--quantity);
        if (quantity == 0)
            pieceColor = Color.NONE;
    }
}

class DiceFace{

    ImageView imgView;
    int number;

    DiceFace (int num){
        number = num;
        String url = "Backgammon/res/DiceFace" + (num) + ".png";
                    // By derivative work: PhJDie_Faces.png: Nanami Kamimura - Die_Faces.png,
                    // CC BY-SA 3.0, https://commons.wikimedia.org/w/index.php?curid=4162818

        Image image = new Image(url);
        imgView = new ImageView();
        imgView.setImage(image);
        }
}


@SuppressWarnings("unchecked")
class Bar {
    VBox[] boxes;
    ArrayList<Piece>[] pieces = new ArrayList[2];

    public Bar(VBox[] boxes) {
        this.boxes = boxes;
        for (int i = 0; i < 2; i++)
            pieces[i] = new ArrayList<>();
    }

    void insert(Piece piece) {
        int color = piece.color.getValue();
        pieces[color].add(piece);
        boxes[color].getChildren().add(piece.imgView);
    }

    void remove(Color color) {
        int x = color.getValue();
        int len = pieces[x].size();
        if (len == 0)
            return;
        pieces[x].remove(len - 1);
        boxes[x].getChildren().remove(len - 1);
    }
}
