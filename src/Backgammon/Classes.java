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
        static int maxMoves;
        static void setInitialPos(GridPane[] p, VBox[] bar, VBox[] bearOff, GridPane bfxml) {
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

        static boolean valid(Move move){        //temporary method that takes a move as input and returns whether it's valid or not

            int org = move.orgStrip-1;
            int dest = move.destStrip-1;
            int diff = org - dest;

            if (move.orgStrip < 0 || move.destStrip < 0 || move.orgStrip > 23 || move.destStrip > 23) { // can probably be removed later
                System.out.println("Out of bounds");
                return false;
            }
            if(stripArray[org].pieceColor!=move.color){
                System.out.println("No " + move.color + " pieces on origin strip " + move.orgStrip);
                return false;
            }
            if(diff != Board.die.getDice1() && diff != Board.die.getDice2() ){
                System.out.println("Difference between orgStrip and destStrip is " + diff + ", which was not one of the dice rolls");
                return false;
            }
            if(stripArray[dest].pieceColor!=move.color&&stripArray[dest].quantity>1){
                System.out.println("destStrip is not able to be landed on, as it has more than one of the opponent's pieces on it");
                return false;
            }
            return true;
        }

        static Move[] findAllValidMoves(Color color) { // Maybe change this to some other method, depends what comes in handy
            Move[] validMoves = new Move[30];
            int i = 0;
            for (Strip aStrip : stripArray) {
                if (aStrip.pieceColor == color) {
                    for (Strip bStrip : stripArray) {
                        Move temp = new Move(aStrip.stripID, bStrip.stripID, color);
                        if (valid(temp)) {
                            validMoves[i++] = temp;
                        }

                    }
                }
            }


            return validMoves;
        }

        static Color nextTurn() {
            currentTurn = currentTurn == Color.BLACK ? Color.WHITE : Color.BLACK;
            boardfxml.setId("board" + currentTurn.getValue());
            currentMoves = 0;
            return currentTurn;
        }

        static void rollStart(Player[] players) {
            die.findStartingPlayer(players);
            boardfxml.setId("board" + currentTurn.getValue());
            maxMoves = 2;
        }

        static void rollDice() {
            die.roll();
            if(die.getDice1() == die.getDice2())
                maxMoves = 4;
            else
                maxMoves = 2;
        }

        static void cheat(){
            clearBoard();

            Piece [] black = new Piece[15];
            Piece [] white = new Piece[15];

            for(int i = 0;i < 15;i++){
                black[i] = new Piece(Color.BLACK);
                white[i] = new Piece(Color.WHITE);
            }

            stripArray[0].insert(white,0,2);
            stripArray[2].insert(white,3,5);
            stripArray[3].insert(white,6,8);
            Bar.insert(white,9,11);
            BearOff.insert(white,12,14);

            for(int i = 23,j = 0;i >= 19;i--,j = j+2){
                stripArray[i].insert(black[j]);
                stripArray[i].insert(black[j+1]);
            }
            Bar.insert(black,10,12);
            BearOff.insert(black,13,14);

        }

        static void clearBoard(){
            // Remove all pieces in the board itself
            for(int i = 0;i < stripArray.length;i++){
                while(stripArray[i].quantity != 0)
                    stripArray[i].pop();
            }

            // Remove all pieces in the bar and bear off
            while(Bar.piecesIn(Color.WHITE) != 0)
                Bar.remove(Color.WHITE);
            while(Bar.piecesIn(Color.BLACK) != 0)
                Bar.remove(Color.BLACK);
            while(BearOff.piecesIn(Color.WHITE) != 0)
                BearOff.remove(Color.WHITE);
            while(BearOff.piecesIn(Color.BLACK) != 0)
                BearOff.remove(Color.BLACK);
        }
    }
}

class Move {
    int orgStrip;
    int destStrip;
    Color color;

    Move(int orgStrip, int destStrip, Color color) {
        this.color = color;
        //This is here since the pip numbers change depending on which color's turn it is.
        if(this.color == Color.BLACK){
            this.orgStrip = 23-orgStrip;
            this.destStrip = 23-destStrip;
        }
        else{
            this.orgStrip = orgStrip;
            this.destStrip = destStrip;
        }
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
        String url = "Backgammon/res/DoublingCube" + num + ".png";  // deliberately have a little blank space on left side of
        Image image = new Image(url);                              // image to help line it up with spot on background image
        imgView = new ImageView();
        imgView.setImage(image);
    }

    DoublingCube() {
        this(2);
    }
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

    void insert(Piece[] pieces, int start, int stop) {
        for (int i = start; i <= stop; i++) {
            insert(pieces[i]);
        }
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
    
    int piecesIn(Color color){
        if(color == Color.WHITE)
            return pieces[Color.WHITE.getValue()].size();
        else{
            return pieces[Color.BLACK.getValue()].size();
        }
    }
}

class Dice {
    private int dice1, dice2;

    ArrayList<Integer> findStartingPlayer(Player[] players) {
        Random rand = new Random();
        ArrayList<Integer> rollStartRolls = new ArrayList<>();

        //Keeps rolling until both players have different rolls
        do {
            dice1 = rand.nextInt(6) + 1;
            dice2 = rand.nextInt(6) + 1;

            rollStartRolls.add(dice1);
            rollStartRolls.add(dice2);

            if (dice1 > dice2) {
                Classes.Board.currentTurn = players[0].getColor();
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