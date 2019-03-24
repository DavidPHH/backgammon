package Backgammon;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.Random;

class Classes {

    static class Board {
        private static GridPane boardfxml;
        static Strip[] stripArray = new Strip[24];
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

        static void makeMove(Move move) {
            if (!validMove(move))
                return;
            // Normal move
            if(move.orgStrip != -1 && move.destStrip >= 0){
                stripArray[move.orgStrip].pop();
                stripArray[move.destStrip].insert(new Piece(move.color));
                currentMoves++;
            }
            else if(move.orgStrip == -1){ // Moving from the bar
                Bar.remove(currentTurn);
                stripArray[move.destStrip].insert(new Piece(move.color));
                currentMoves++;
            }
            else if(move.destStrip == -2){ // Moving to the bear-off
                stripArray[move.orgStrip].pop();
                BearOff.insert(new Piece(currentTurn));
                currentMoves++;
            }
        }

        static void testMove(Move move) { // same as makeMove but allows both colors on the same strip for test purposes
            stripArray[move.orgStrip].pop();
            stripArray[move.destStrip].insert(new Piece(move.color));
        }

        static boolean validMove(Move move) { //TODO Logic for checking bear-off moves
            if (move.orgStrip < -1 || move.destStrip < -1 || move.orgStrip > 23 || move.destStrip > 23) // If outside of array, it's an invalid move
                return false;
            
            Strip dest = getStrip(move.destStrip);
            Strip org = null;
            if(move.orgStrip != -1){
                org = getStrip(move.orgStrip);
            }

            //If the player has a piece in the Bar and they try move a piece not on the bar
            //Bar is referred to as -1
            if(Bar.piecesIn(currentTurn) > 0){
                if(move.orgStrip != -1) // Checks to see if user is moving from the bar
                    return false;
                else if((currentTurn != dest.pieceColor) && dest.quantity > 1) // Destination has opposing pieces
                    return false;
                else if((currentTurn != dest.pieceColor) && dest.quantity == 1) // Destination has only 1 opposing piece so a hit
                    hitMove(dest);
                else {
                    return true;
                }
            }

            // Ensures user does not go backwards
            if(currentTurn == Color.BLACK){
                if(move.orgStrip > move.destStrip)
                    return false;
            }
            else if(currentTurn == Color.WHITE)
                if(move.orgStrip < move.destStrip)
                    return false;

            //At this point, we are checking to see if the end point allows for a valid move
            if(org.quantity == 0 || (org.pieceColor != currentTurn)) // Trying to move opponents piece or move nothing
                return false;

            if ((org.pieceColor != dest.pieceColor) && dest.quantity > 1){ // If the dest strip has pieces of the opposite color,
                return false; // it's an invalid move
            }
            else if((org.pieceColor != dest.pieceColor) && dest.quantity == 1){ // This move is a hit to bar
                hitMove(dest);
                return true;
            }
            else if((org.pieceColor != dest.pieceColor) && dest.quantity == 0)// If the dest piece is empty
                return true;
            // If the player is moving a piece that isn't his
            return move.color == dest.pieceColor;

        }

        static void hitMove(Strip dest){
            Piece ripPiece = new Piece(dest.pop());
            Bar.insert(ripPiece);
        }

        static boolean valid(Move move, boolean showErrors){        //temporary method that takes a move as input and returns whether it's valid or not
                                                                   //the reason I made a new one instead of updating validMove() is so that makeMove()
                                                                  //(which incorporates validMove) would still reliably be usable even as I tamper with valid()
            int org = move.orgStrip;
            int dest = move.destStrip;
            int displayedDest = (move.color == Color.WHITE) ? (dest+1) : (23-dest)+1;    //yes, math for 23-org+1 could be simplified, but I
            int displayedOrg = (move.color == Color.WHITE) ? (org+1) : (23-org)+1;      //kept it that way so that it's easier to make sense of,
            int diff = displayedOrg - displayedDest;                                   //expressing it as a reverse of previous steps applied to it.

            if (org < 0 || dest < 0 || org > 23 || dest > 23) {                   // can probably be removed later since it
                if(showErrors)                                                   // will likely only be used in findBoardMoves()
                    System.out.println("Out of bounds");                        // which should naturally stay within those bounds
                return false;
            }
            if(stripArray[org].pieceColor!=move.color){                         //maybe also remove?
                if(showErrors)
                    System.out.println("No " + move.color + " pieces on origin strip " + displayedOrg);
                return false;
            }
            if(diff != Board.die.getDice1() && diff != Board.die.getDice2() ){
                if(showErrors)
                    System.out.println("Difference between orgStrip and destStrip is " + diff + ", which was not one of the dice rolls");
                return false;
            }
            if(stripArray[dest].pieceColor!=move.color&&stripArray[dest].quantity>1){
                if(showErrors)
                    System.out.println("destStrip is not able to be landed on, as it has more than one of the opponent's pieces on it");
                return false;
            }
            return true;
        }

        static ArrayList<Move> findBoardMoves() {
            ArrayList<Move> boardMoves = new ArrayList<>();
            for (Strip aStrip : stripArray) {
                for (Strip bStrip : stripArray) {
                    Move curr = new Move(aStrip.stripID, bStrip.stripID, currentTurn);
                    if (valid(curr, false)) {
                        boardMoves.add(curr);
                    }
                }
            }         //above is what it will be, below is just to make testing more controlled

            /*boardMoves.add(new Move(6, 7, currentTurn));
            boardMoves.add(new Move(6, 8, currentTurn));
            boardMoves.add(new Move(8, 9, currentTurn));
            */

            return boardMoves;
        }

        static ArrayList<Move> findBarMoves(){
            int dice1 = Board.die.getDice1()-1;
            int dice2 = Board.die.getDice2()-1;
            ArrayList<Move> barMoves = new ArrayList<>();

            if(Bar.piecesIn(currentTurn) > 0) {
                if (stripArray[dice1].pieceColor == currentTurn || stripArray[dice1].quantity <= 1) {
                    barMoves.add(new Move(-1, dice1, currentTurn));
                }
                if (stripArray[dice2].pieceColor == currentTurn || stripArray[dice2].quantity <= 1) {
                    barMoves.add(new Move(-1, dice2, currentTurn));
                }
            }

            //barMoves.add(new Move(-1, 3, currentTurn));
            //barMoves.add(new Move(-1, 4, currentTurn));

            return barMoves;
        }

        //Also dummy

        static ArrayList<Move>findBearOffMoves(){
            ArrayList<Move> bearOffMoves = new ArrayList<>();
            bearOffMoves.add(new Move(21, -2, currentTurn));
            bearOffMoves.add(new Move(22, -2, currentTurn));
            bearOffMoves.add(new Move(23, -2, currentTurn));

            return bearOffMoves;
        }

        //still dummy

        static boolean allHomeBoard(){
            return false;
        }

        //more dummy


        static void removeDuplicateCombos(ArrayList<MoveCombo> combos){
           /* ArrayList<MoveCombo> temp = new ArrayList<MoveCombo>(combos);

            if(true)
                for(MoveCombo mc: temp){
                    combos.removeIf(m -> (m!=mc && m.moves[0].orgStrip==mc.moves[0].orgStrip && m.moves[0].destStrip==mc.moves[0].destStrip));
                }
            */

           //How to remove objects from arrayList while iterating through that arraylist without getting concurrentModificationException?
        }

        static ArrayList<MoveCombo> findAllValidCombos() {


            /*findAllValidCombos() Proposed Pseudo-code:

                Assumes existence of following methods:
                - findBarMoves()            i.e. moves starting on the bar and ending on the board                                        DONE
                - findBoardMoves()          i.e. moves starting and ending on board, more or less what findAllValidCombos() used to do    DONE
                - findBearOffMoves()        i.e. moves starting on tbe board and ending in the bear-off                                   TODO
                - removeMovesStartingOn()   i.e. exactly what it sound like, takes an arrayList of moves and an int,                      DONE in-line instead
                                            and modifies that arrayList to remove any moves with an orgStrip of that int
                - allHomeBoard()            Returns boolean on whether all your pieces are in your home board, needed to determine        TODO
                                            whether you can start bearing-off pieces yet
                - removeDuplicateCombos()   Removes combinations of moves that have the same orgStrip for the first move,                 TODO
                                            same destStrip for the last move, the firstMove's destStrip is the secondMove's orgStrip
                                            and none of the strips landed on in between resulted in hits.
                                            (Need two different versions for 2-move combos vs 4-move combos?)
                                            (should be a method of MoveCombo class?)

                                            Should it also remove Combos that are moving separate pieces, but still result in the same board state?
                                            Like "6-7 9-10" and "9-10 6-7"?

                */



            //Bar.insert(new Piece(currentTurn));
            //Bar.insert(new Piece(currentTurn));


                  ArrayList<Move> allMoves = new ArrayList<>();

                 if(Bar.piecesIn(currentTurn) >= maxMoves){              // i.e., if you have more (or same number of) pieces on the bar than you have dice
                    allMoves.addAll(findBarMoves());                     // rolls to use, you will only be able to move those pieces, and not any of findBoardMoves()
                     System.out.println("Only Bar moves are allowed for this turn");
                 }else if(allHomeBoard()){
                    allMoves.addAll(findBoardMoves());
                    allMoves.addAll(findBearOffMoves());
                     System.out.println("Board & BearOff moves are allowed for this turn");
                 }else{
                    allMoves.addAll(findBarMoves());
                    allMoves.addAll(findBoardMoves());
                     System.out.println("Bar & Board moves are allowed for this turn");
                 }

                 ArrayList<Move> copyAllMoves;                             // made so that when combining moves into pairs (or quadruplets when a double is rolled),
                                                                           // you can temporarily change which moves are valid without affecting the master copy



                 ArrayList<MoveCombo> allCombos = new ArrayList<>();

            //code to combine multiple moves in pairs to print -

            System.out.println("allMoves initially looks like: " + allMoves);

            if(maxMoves==2) {
                for (Move firstMove : allMoves) {
                    copyAllMoves = new ArrayList<>(allMoves);
                    if (Bar.piecesIn(currentTurn) == 0 || firstMove.orgStrip == -1 || firstMove.orgStrip == 24) {      // how to code orgStrip==Bar, since currently orgStrip is an int only?
                        // maybe reserve an int like 0 to represent the bar, and then 1-24
                        // can actually correspond to what you'd expect on the board?

                        //System.out.println("made it");

                        // Update: seem to have gone with -1 representing bar
                        // which importantly can also end up as 24 due to the way Moves constructor handles different colors

                        // if statement means that if there are no possible bar moves which
                        // would have taken priority, then the first move of the pair can be anything,
                        // but if there are bar moves possible (i.e. if bar.quantity>0),
                        // then the first move has to be one of those.

                        if (firstMove.destStrip != -1 && stripArray[firstMove.destStrip].pieceColor != currentTurn && validMove(new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice1(), currentTurn))) {
                            copyAllMoves.add(new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice1(), currentTurn));
                            System.out.println("Added: " + new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice1(), currentTurn));
                        }   //doesn't seem to be catching anything, why?

                        // i.e. assuming the first move is made and there is now a piece on destStrip where there wasn't before,
                        // does that produce any new valid moves that weren't available before? Checks both dice1 and dice2.

                        if (firstMove.destStrip != -1 && stripArray[firstMove.destStrip].pieceColor != currentTurn && validMove(new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice2(), currentTurn))) {
                            copyAllMoves.add(new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice2(), currentTurn));
                            System.out.println("Added: " + new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice2(), currentTurn));
                        }

                        //Conversely, also need to check if any moves are no longer possible now. This would only happen if there are
                        //no more pieces left on orgStrip after the move is made, i.e. if there is currently only one piece on orgStrip

                        if (firstMove.orgStrip >= 0 && firstMove.orgStrip < 24 && stripArray[firstMove.orgStrip].quantity <= 1){  //<=1 to make testing easier, really could be ==1
                            //System.out.println("Removing moves starting on same");
                            copyAllMoves.removeIf(m -> m.orgStrip == firstMove.orgStrip);
                            //System.out.println("Removed successfully");
                        } else if (Bar.piecesIn(currentTurn) <= 1){  // i.e. when orgStrip = -1 (Bar)
                            copyAllMoves.removeIf(m -> m.orgStrip == firstMove.orgStrip);
                        }

                        // >=0 and <24 conditions included so that it doesn't try to dereference something like stripArray[-1] for bar moves

                        //keep in mind we also need to remove any barMoves which are no longer possible,
                        //above if statement might already be sufficient if we go the route of assigning the bar an index that's actually included in stripArray
                        //if not, need separate function

                        for (Move secondMove : copyAllMoves) {
                            if (Math.abs(firstMove.orgStrip-firstMove.destStrip) + Math.abs(secondMove.orgStrip-secondMove.destStrip) == Board.die.getDice1() + Board.die.getDice2()){
                            //commented out because still testing, so will rarely actually match dice numbers. Seems to work fine though
                            //System.out.println("Combined distance = " + (Math.abs(firstMove.orgStrip-firstMove.destStrip) + Math.abs(secondMove.orgStrip-secondMove.destStrip)));
                            //System.out.println("Should be: " + (Board.die.getDice1() + Board.die.getDice2()));
                                allCombos.add(new MoveCombo(2, firstMove, secondMove));
                            }
                        }

                    }

                }
            }

               //also need to provide code for when maxMoves = 4

              //also need to provide code for situations when only one valid move is found (and 3 as well I guess)

            //if (plays with 2 moves are present)
                //remove plays with less than 2 moves

            //repeat for 3 and less, and 3 and less

              removeDuplicateCombos(allCombos);

              return allCombos;

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
            for (Strip currentStrip : stripArray) {
                while (currentStrip.quantity != 0)
                    currentStrip.pop();
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
        //Max orgStrip can be 23 since user input is always subtracted by 1 before coming to this point.
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
//        if (Classes.Board.validMove(this))
//            return "Move: Origin: " + (orgStrip + 1) + " Destination: " + (destStrip + 1);
//        else
//            return "Invalid move";


        //if (Classes.Board.valid(this, false)) {
        return ((orgStrip==-1 || orgStrip == 24)?"Bar":((color==Color.WHITE)?(orgStrip+1):(23-orgStrip+1))) +
                "-" +
                ((color==Color.WHITE)?(destStrip+1):(23-destStrip+1)) +
                (destStrip==-2?"Off":((Classes.Board.stripArray[destStrip].quantity == 1 && Classes.Board.stripArray[destStrip].pieceColor != color)?"*":""));
        //} else
        //    return "Invalid move";


    }

    String isHitToString() {
        //if (Classes.Board.valid(this, false)) {
            return ((orgStrip==-1 || orgStrip == 24)?"Bar":((color==Color.WHITE)?(orgStrip+1):(23-orgStrip+1))) +
                    "-" +
                    (destStrip==-2?"Off":((color==Color.WHITE)?(destStrip+1):(23-destStrip+1)) +
                    ((Classes.Board.stripArray[destStrip].quantity == 1 && Classes.Board.stripArray[destStrip].pieceColor != color)?"*":""));
        //} else
        //    return "Invalid move";
    }
}

class MoveCombo{
    Move[] moves = new  Move[4];
    int numMovesPerCombo;   //needed? or just change moves to ArrayList and can then use moves.size()?

    MoveCombo(int num, Move... args){
        int i=0;
        for (Move m: args) {
            moves[i++] = m;
        }
        numMovesPerCombo = num;
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

    private void updateSpacing(){
        int pieceSize = 54;           // deliberately using unnecessary variables to help readability
        int totalLength = quantity * pieceSize;
        int vBoxLength = 260;         // vBox is actually slightly longer but I prefer 260 as it
        // still lets you see a bit of the triangle underneath

        vBox.setSpacing(totalLength > vBoxLength ? -((totalLength - vBoxLength) / (quantity - 1.0)) : -2);
        // the default spacing is essentially nothing, but if there are so many pieces that they start to overflow,
        // then a dynamically updated negative spacing is used to make them overlap instead, proportional to the quantity,
        // such that they never extend past the VBox's borders

        // totalLength is what the default sum of the lengths of all the pieces would be if there was no spacing
        // When that is bigger than vBoxLength, vBoxLength is subtracted from it to determine the difference
        // This difference must be made up for by overlapping pieces so that the total area overlapped is equal to that difference.
        // To determine the spacing between two pieces, that total overlap distance is divided by the number of overlaps
        // there are (which is always one less than the number of pieces),
        // and then negated to make it overlap instead of leaving a gap, which is what a positive value would have done

        // -2 is used as the default instead of 0 because with 0 there was actually a little bit of whitespace in between pieces
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
        updateSpacing();
    }

    Color pop() {
        Color removedColor = pieceColor;
        vBox.getChildren().remove(--quantity);
        if (quantity == 0)
            pieceColor = Color.NONE;
        updateSpacing();

        return removedColor;
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
class  Bar {
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

    //Finds the number of pieces in the Bar for COLOR's turn
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
                //System.out.println(players[1].getColor());
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