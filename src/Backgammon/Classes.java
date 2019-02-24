package Backgammon;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

class Classes {

    static class Board {
        private static GridPane boardfxml;
        private static Strip[] stripArray = new Strip[24];
        static Color currentTurn;
        static Bar Bar;
        static Bar BearOff;
        static Dice die = new Dice();
        static int currentMoves;

        static void setInitialpos(GridPane[] p, VBox[] bar, VBox[] bearOff, GridPane bfxml) {
            boardfxml = bfxml;
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
            currentMoves++;
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
            // If the player is moving a piece that isn't his
            return move.color == dest.pieceColor;
        }

        static Move[] findAllvalidMoves() { // Maybe change this to some other method, depends what comes in handy
            return null;
        }

        static Color nextTurn() {
            currentTurn = currentTurn == Color.BLACK ? Color.WHITE : Color.BLACK;
            boardfxml.setId("board" + currentTurn.getValue());
            currentMoves = 0;
            return currentTurn;
        }

        static void rollStart(Player[] players) {
            die.findStartingPlayer(players);
        }

        static void rollDice() {
            die.roll();
            System.out.println(die.getDice1() + " " + die.getDice2());
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

class DiceFace {

    ImageView imgView;
    int number;

    DiceFace(int num) {
        number = num;
        String url = "Backgammon/res/DiceFace" + (num) + ".png";
        // By derivative work: PhJDie_Faces.png: Nanami Kamimura - Die_Faces.png,
        // CC BY-SA 3.0, https://commons.wikimedia.org/w/index.php?curid=4162818

        Image image = new Image(url);
        imgView = new ImageView();
        imgView.setImage(image);
    }
}

class DoublingCube {

    ImageView imgView;

    DoublingCube(int num) {
        String url = "Backgammon/res/diceNum" + num + ".png";   // ultimately plan to make these a little more stylish
        Image image = new Image(url);                           // thinking white numbers on dark red at the moment
        imgView = new ImageView();
        imgView.setImage(image);
    }

    DoublingCube() {
        this(2);
    }

 /*   public void doubleCurrent(){
        number *= 2;
        String url = "Backgammon/res/diceNum" + number + ".png";
        Image image = new Image(url);
        imgView = new ImageView();
        imgView.setImage(image);

    }*/
}


@SuppressWarnings("unchecked")
class Bar {
    private VBox[] boxes;
    private ArrayList<Piece>[] pieces = new ArrayList[2];

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

class Dice {
    private int dice1, dice2;

    ArrayList<Integer> findStartingPlayer(Player[] players) {
        Random rand = new Random();
        ArrayList<Integer> rollStartRolls = new ArrayList<>();

        do {
            dice1 = rand.nextInt(6) + 1;
            dice2 = rand.nextInt(6) + 1;

            rollStartRolls.add(dice1);
            rollStartRolls.add(dice2);

            if (dice1 > dice2) {
                Classes.Board.currentTurn = players[0].getColor();
                /*System.out.println(players[0].getColor());
                System.out.println(Classes.Board.currentTurn.getValue());*/
            } else if (dice2 > dice1) {
                Classes.Board.currentTurn = players[1].getColor();
                System.out.println(players[1].getColor());
            }
        } while (dice1 == dice2);

        return rollStartRolls;
    }

    void roll() {
        Random rand = new Random();
        dice1 = rand.nextInt(6) + 1;
        dice2 = rand.nextInt(6) + 1;
    }

    int getDice1() {
        return dice1;
    }

    int getDice2() {
        return dice2;
    }
}