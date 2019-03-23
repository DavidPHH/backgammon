package Backgammon;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
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
            BearOff.isBearoff = true;
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

        static void makeMove(Move move, int type) {
            // Type refers to whether a move comes from /move or /listmove
            // Moves from /listmove have already been checked if it is valid so it doesn't need to check again
            // -1 == /move  1 == listMove
            if (type == -1) {
                if (!validMove(move))
                    return;
            }
            // Normal move
            if (move.orgStrip != -1 && move.destStrip >= 0) {
                int dist = 0;
                // Getting which dice number was used.
                if (currentTurn == Color.WHITE)
                    dist = move.orgStrip - move.destStrip;
                else if (currentTurn == Color.BLACK)
                    dist = move.destStrip - move.orgStrip;

                isMoveAHit(getStrip(move.destStrip));
                stripArray[move.orgStrip].pop();
                stripArray[move.destStrip].insert(new Piece(move.color));
                if (maxMoves == 2) { // No doubles
                    if (dist == die.getDice1()) // Removes the used dice from being used again in this turn
                        die.resetDice(1);
                    else
                        die.resetDice(2);
                    currentMoves++;
                } else if (maxMoves == 4) { // Player rolled doubles
                    int moves = (die.getDice1() / dist); // The number of times the player moved in one go
                    currentMoves += moves; // No need to reset die here since they both have the same number
                }
            } else if (move.orgStrip == -1) { // Moving from the bar
                isMoveAHit(getStrip(move.destStrip));
                Bar.remove(currentTurn);
                stripArray[move.destStrip].insert(new Piece(move.color));
                currentMoves++;
            } else if (move.destStrip == -2) { // Moving to the bear-off
                Piece freedom = new Piece(stripArray[move.orgStrip].pop());
                BearOff.insert(freedom);
                currentMoves++;
                if (currentTurn == Color.WHITE)
                    Main.players[0].setPiecesLeft(Main.players[0].getPiecesLeft() - 1);
                else if (currentTurn == Color.BLACK) {
                    Main.players[1].setPiecesLeft(Main.players[1].getPiecesLeft() - 1);
                }
            }
        }

        static void testMove(Move move) { // same as makeMove but allows both colors on the same strip for test purposes
            stripArray[move.orgStrip].pop();
            stripArray[move.destStrip].insert(new Piece(move.color));
        }

        static boolean validMove(Move move) {
            if (move.orgStrip < -1 || move.destStrip < -3 || move.orgStrip > 23 || move.destStrip > 23 || (move.orgStrip == move.destStrip)) // If outside of array, it's an invalid move
                return false;
            if (move.destStrip == -1) // Can probably be put in previous if, brain not working rn
                return false;
            Strip dest = null;
            Strip org = null;
            if (move.orgStrip != -1) {
                org = getStrip(move.orgStrip);
            }
            if (move.destStrip != -2)
                dest = getStrip(move.destStrip);

            /* TODO
                Logic to allow moves to use the double moves i.e. move 16 forward if roll double 4
            */

            // User wants to move to the bear-off, this checks if it is allowed. This is done before the bar check
            // as moving to the bear-off has a different check for move according to the die. Bear-off == -2
            if (move.destStrip == -2) {
                int diff;
                if (currentTurn == Color.WHITE) {
                    diff = move.orgStrip + 1; // Adding 1 since destination will be from org to 0th position +1 to the bear-off

                    int count = 0;
                    for (int i = 0; i < 6; i++) { //Checks if the bear-off can be attained in the first place
                        count += stripArray[i].quantity;
                    }
                    if (count == Main.players[0].getPiecesLeft()) {
                        if (diff <= die.getDice1()) // If the first die will bring you to bear-off
                            return true;
                        else if (diff <= die.getDice2()) // If the second die will bring you to bear-off
                            return true;
                        else if (diff <= (die.getDice1() + die.getDice2())) { // Combination of the die
                            Move mDie1 = new Move(move.orgStrip, move.orgStrip - die.getDice1(), currentTurn);
                            Move mDie2 = new Move(move.orgStrip, move.orgStrip - die.getDice2(), currentTurn);
                            //The individual moves could potentially be blocked by an opposing piece
                            if (validMove(mDie1)) { // If the first dice leads to a valid move
                                mDie2.orgStrip = mDie1.destStrip;
                                mDie2.destStrip = -2;
                                return validMove(mDie2); // Returns whether or not the second move is valid
                            } else if (validMove(mDie2)) { // If the first die leads to a valid move at the start, but not the first
                                mDie1.orgStrip = mDie2.destStrip;
                                mDie1.destStrip = -2;
                                return validMove(mDie1); // Returns whether or not the second move is valid
                            } else {
                                return false;
                            }
                        }
                    }
                    return false;
                } else if (currentTurn == Color.BLACK) {
                    diff = (23 - move.orgStrip) + 1;
                    int count = 0;
                    for (int i = 23; i >= 18; i--) {
                        count += stripArray[i].quantity;
                    }
                    if (count == Main.players[1].getPiecesLeft()) {
                        if (diff <= die.getDice1()) // If the first die will bring you to bear-off
                            return true;
                        else if (diff <= die.getDice2()) // If the second die will bring you to bear-off
                            return true;
                        else if (diff <= (die.getDice1() + die.getDice2())) { // Combination of the die
                            Move mDie1 = move;
                            mDie1.destStrip = move.orgStrip + die.getDice1();
                            Move mDie2 = move;
                            mDie2.destStrip = move.orgStrip + die.getDice2();
                            //The individual moves could potentially be blocked by an opposing piece
                            if (validMove(mDie1)) { // If the first dice leads to a valid move
                                mDie2.orgStrip = mDie1.destStrip;
                                mDie2.destStrip = -2;
                                return validMove(mDie2); // Returns whether or not the second move is valid
                            } else if (validMove(mDie2)) { // If the first die leads to a valid move at the start, but not the first
                                mDie1.orgStrip = mDie2.destStrip;
                                mDie1.destStrip = -2;
                                return validMove(mDie1); // Returns whether or not the second move is valid
                            } else
                                return false;
                        }
                    }
                }
            }

            //If the player has a piece in the Bar and they try move a piece not on the bar
            //Bar is referred to as -1
            if (Bar.piecesIn(currentTurn) > 0) {
                if (move.orgStrip != -1) // Checks to see if user is moving from the bar
                    return false;
                else{ // Checking to see if the move matches the dice roll
                    int dist = 0;
                    if(currentTurn == Color.WHITE){ // Moves from bar (-1) -> stripArray[23] ....
                        dist = 23 - move.destStrip+1; // +1 takes into account moving off bar
                    }else if(currentTurn == Color.BLACK){
                        dist = move.destStrip +1;
                    }
                    if(dist == die.getDice1() || dist == die.getDice2()){
                        if((currentTurn != dest.pieceColor) && dest.quantity > 1) // Destination has opposing pieces
                            return false;
                        else if((currentTurn != dest.pieceColor) && dest.quantity == 1) // Destination has only 1 opposing piece so a hit
                            return true;
                        else {
                            return true;
                        }
                    }else if(dist == die.getDice1() + die.getDice2()){
                        Move wDie1;
                        Move wDie2;
                        if(currentTurn == Color.WHITE){
                            wDie1 = new Move(move.orgStrip,move.destStrip + die.getDice2(),currentTurn);
                            wDie2 = new Move(move.orgStrip,move.destStrip + die.getDice1(),currentTurn);
                        }else if(currentTurn == Color.BLACK){
                            wDie1 = new Move(0,0,currentTurn);
                            wDie2 = new Move(0,0,currentTurn);
                            wDie1.orgStrip = move.orgStrip;
                            wDie1.destStrip = (move.orgStrip + die.getDice2());
                            wDie2.destStrip = (move.orgStrip + die.getDice1());
                        }else
                            return false;

                        if(validMove(wDie1)){ // Checking if the individual moves are valid moves, before combining them
                            System.out.println("First was valid");
                            return checkSecondMoveFromBar(wDie1,wDie2,move);
                        }else if(validMove(wDie2)){
                            return checkSecondMoveFromBar(wDie2,wDie1,move);
                        }
                        return false;
                    }
                    return false;
                }
            }
            // Ensures user does not go backwards
            if (currentTurn == Color.BLACK) {
                if (move.orgStrip > move.destStrip)
                    return false;
            } else if (currentTurn == Color.WHITE)
                if (move.orgStrip < move.destStrip)
                    return false;

            //Ensure the player uses a move related to the dice roll. Moved from Eric's valid function
            int displayedDest = (move.color == Color.WHITE) ? (move.destStrip + 1) : (23 - move.destStrip) + 1;    //yes, math for 23-org+1 could be simplified, but I
            int displayedOrg = (move.color == Color.WHITE) ? (move.orgStrip + 1) : (23 - move.orgStrip) + 1;      //kept it that way so that it's easier to make sense of,
            int diff = displayedOrg - displayedDest;
            if (diff != Board.die.getDice1() && diff != Board.die.getDice2()) {
                return false;
            }
            // Just checking a normal move, no bar/bear-off
            if (org.quantity == 0 || (org.pieceColor != currentTurn)) // Trying to move opponents piece or move nothing
                return false;

            if ((org.pieceColor != dest.pieceColor) && dest.quantity > 1) { // If the dest strip has pieces of the opposite color,
                return false; // it's an invalid move
            } else if ((org.pieceColor != dest.pieceColor) && dest.quantity == 1) { // This move is a hit to bar
                return true;
            } else if ((org.pieceColor != dest.pieceColor) && dest.quantity == 0)// If the dest piece is empty
                return true;
            // If the player is moving a piece that isn't his
            return move.color == dest.pieceColor;

        }

        static boolean checkSecondMoveFromBar(Move firstMove, Move secondMove, Move move){
            secondMove.orgStrip = firstMove.destStrip;
            secondMove.destStrip = move.destStrip;
            boolean tempAdded = false;
            // Input a temp piece if empty, for checking if it's a valid move.
            if(stripArray[secondMove.orgStrip].pieceColor == Color.NONE){
                stripArray[secondMove.orgStrip].insert(new Piece(currentTurn));
                tempAdded = true;
            }else if(currentTurn == Color.BLACK && stripArray[secondMove.orgStrip].pieceColor == Color.WHITE){
                stripArray[secondMove.orgStrip].pop();
                stripArray[secondMove.orgStrip].insert(new Piece(currentTurn));
                tempAdded = true;
            }else if(currentTurn == Color.WHITE && stripArray[secondMove.orgStrip].pieceColor == Color.BLACK){
                stripArray[secondMove.orgStrip].pop();
                stripArray[secondMove.orgStrip].insert(new Piece(currentTurn));
                tempAdded = true;
            }

            int count = 0;
            int piecesInBar = Bar.piecesIn(currentTurn);
            Piece[] temp = new Piece[piecesInBar];

            while(Bar.piecesIn(currentTurn) > 0){ // Remove the pieces in the Bar temporarily so second move can be tested
                temp[count] = new Piece(Bar.remove(currentTurn));
                count++;
            }
            count--;

            if(validMove(secondMove)){
                while(count >= 0){
                    Bar.insert(temp[count]);
                    count--;
                }
                if(tempAdded)
                    stripArray[secondMove.orgStrip].pop(); // Remove the temp piece
                return true;
            }else{ // If invalid move, puts the pieces back
                while(count >= 0){
                    Bar.insert(temp[count]);
                    count--;
                }
                if(tempAdded)
                    stripArray[secondMove.orgStrip].pop(); // Remove the temp piece
            }
            return false;
        }

        static void hitMove(Strip dest) {
            Piece ripPiece = new Piece(dest.pop());
            Bar.insert(ripPiece);
        }

        static void isMoveAHit(Strip dest) {
            if ((currentTurn != dest.pieceColor) && dest.quantity == 1) // Destination has only 1 opposing piece so a hit
                hitMove(dest);
        }

        static boolean valid(Move move, boolean showErrors) {        //temporary method that takes a move as input and returns whether it's valid or not
            //the reason I made a new one instead of updating validMove() is so that makeMove()
            //(which incorporates validMove) would still reliably be usable even as I tamper with valid()
            int org = move.orgStrip;
            int dest = move.destStrip;
            int displayedDest = (move.color == Color.WHITE) ? (dest + 1) : (23 - dest) + 1;    //yes, math for 23-org+1 could be simplified, but I
            int displayedOrg = (move.color == Color.WHITE) ? (org + 1) : (23 - org) + 1;      //kept it that way so that it's easier to make sense of,
            int diff = displayedOrg - displayedDest;                                   //expressing it as a reverse of previous steps applied to it.

            if (org < 0 || dest < 0 || org > 23 || dest > 23) {                   // can probably be removed later since it
                if (showErrors)                                                   // will likely only be used in findAllValidMoves()
                    System.out.println("Out of bounds");                        // which should naturally stay within those bounds
                return false;
            }
            if (stripArray[org].pieceColor != move.color) {                         //maybe also remove?
                if (showErrors)
                    System.out.println("No " + move.color + " pieces on origin strip " + displayedOrg);
                return false;
            }
            if (diff != Board.die.getDice1() && diff != Board.die.getDice2()) {
                if (showErrors)
                    System.out.println("Difference between orgStrip and destStrip is " + diff + ", which was not one of the dice rolls");
                return false;
            }
            if (stripArray[dest].pieceColor != move.color && stripArray[dest].quantity > 1) {
                if (showErrors)
                    System.out.println("destStrip is not able to be landed on, as it has more than one of the opponent's pieces on it");
                return false;
            }
            return true;
        }

        static ArrayList<Move> findAllValidMoves(Color color) {
            ArrayList<Move> validMovesList = new ArrayList<>();
            for (Strip aStrip : stripArray) {
                //if (aStrip.pieceColor == color) {
                for (Strip bStrip : stripArray) {
                    Move temp = new Move(aStrip.stripID, bStrip.stripID, color);
                    if (valid(temp, false)) {
                        validMovesList.add(temp);
                    }

                }
                //}
            }

            return validMovesList;

            /*
            findAllValidMoves() Proposed Pseudo-code:

                Assumes existence of following methods:
                - findBarMoves()            i.e. moves starting on the bar and ending on the board
                - findBoardMoves()          i.e. moves starting and ending on board, more or less what findAllValidMoves() does now
                - findBearOffMoves()        i.e. moves starting on tbe board and ending in the bear-off
                - removeMovesStartingOn()   i.e. exactly what it sound like, takes an arrayList of moves and an int,
                                            and modifies that arrayList (shouldn't need to return it) to remove any moves
                                            with an orgStrip of that int
                - allHomeBoard()            Returns boolean on whether all your pieces are in your home board, needed to determine
                                            whether you can start bearing-off pieces yet
                - removeDuplicateMoves()    Removes combinations of moves that have the same orgStrip for the first move,
                                            same destStrip for the last move, and none of the strips landed on in between
                                            resulted in hits. (Need two different versions for 2-move combos vs 4-move combos?)


                  ArrayList<Moves> allMoves = new ArrayList<>;

                 if(bar.quantity >= maxMoves){              // i.e., if you have more (or same number of) pieces on the bar than you have dice
                    allMoves.add(findBarMoves());          // rolls to use, you will only be able to move those pieces, and not any of findBoardMoves()
                 }else if(allHomeBoard){
                    allMoves.add(findBoardMoves());
                    allMoves.add(findBearOffMoves());
                 }else{
                    allMoves.add(findBarMoves());
                    allMoves.add(findBoardMoves());
                 }

                 ArrayList<Move> copyAllMoves = new ArrayList<Move>;        // made so that when combining moves into pairs (or quadruplets when a double is rolled),
                                                                           // you can temporarily change which moves are valid without affecting the master copy


                 //code to combine multiple moves in pairs to print -

                 ArrayList<> allCombos = new ArrayList<>  // Should it be an ArrayList of strings? Currently findAllValidMoves returns an ArrayList of Moves
                                                         // and then it's converted to strings in the controller, but once we start involving bar moves
                                                         // and bearOff moves, that might not be possible, so maybe best to convert to strings here, and then pass that

                 if(maxMoves==2){
                    int i = 0;
                    for(Move firstMove: allMoves){
                        copyAllMoves = allMoves;
                        if(bar.quantity==0 || firstMove.orgStrip == Bar){      // how to code orgStrip==Bar, since currently orgStrip is an int only?
                                                                                // maybe reserve an int like 0 to represent the bar, and then 1-24
                                                                                // can actually correspond to what you'd expect on the board?

                                                                          // if statement means that if there are no possible bar moves which
                                                                          // would have taken priority, then the first move of the pair can be anything,
                                                                          // but if there are bar moves possible (i.e. if bar.quantity>0),
                                                                          // then the first move has to be one of those.

                           if(stripArray[firstMove.destStrip].colour!=currentTurn && valid(new Move(firstMove.destStrip, firstMove.destStrip+dice1, currentTurn){
                                copyAllMoves.add(new Move(firstMove.destStrip, firstMove.destStrip+dice1, currentTurn));
                           }

                           // i.e. assuming the first move is made and there is now a piece on destStrip where there wasn't before,
                           // does that produce any new valid moves that weren't available before? Checks both dice1 and dice2.

                           if(stripArray[firstMove.destStrip].colour!=currentTurn && valid(new Move(firstMove.destStrip, firstMove.destStrip+dice2, currentTurn){
                                copyAllMoves.add(new Move(firstMove.destStrip, firstMove.destStrip+dice2, currentTurn));
                           }

                           //Conversely, also need to check if any moves are no longer possible now. This would only happen if there are
                           //no more pieces left on orgStrip after the move is made, i.e. if there is currently only one piece on orgStrip

                           if(stripArray[firstMove.orgStrip].quantity == 1){
                              removeMovesStartingOn(copyAllMoves, firstMove.orgStrip);
                           }

                           //keep in mind we also need to remove barMoves which are no longer possible,
                           //above if statement might already be sufficient if we go the route of assigning the bar an index

                           for(Move secondMove: copyAllMoves){
                                String letterCode = (i<26)?Character.toString('A'+i):Character.toString('A'+(i/26)-1)+Character.toString('A'+i%26);
                                allCombos.add(letterCode + ": " + firstMove.isHitToString + ", " + secondMove.isHitToString);
                                //either add letterCode here or in printMoves function, but not both
                                //probably will ultimately be removed from printMoves so that everything can be passed as one string
                           }

                         }

                     }

                 }

               //also need to provide code for when maxMoves = 4

              removeDuplicateMoves();

              return allCombos;

             */
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
            if (die.getDice1() == die.getDice2())
                maxMoves = 4;
            else
                maxMoves = 2;
        }

        static void cheat() {
            clearBoard();

            Piece[] black = new Piece[15];
            Piece[] white = new Piece[15];

            for (int i = 0; i < 15; i++) {
                black[i] = new Piece(Color.BLACK);
                white[i] = new Piece(Color.WHITE);
            }

            stripArray[0].insert(white, 0, 2);
            stripArray[2].insert(white, 3, 5);
            stripArray[3].insert(white, 6, 8);
            Bar.insert(white, 9, 11);
            BearOff.insert(white, 12, 14);

            for (int i = 23, j = 0; i >= 19; i--, j = j + 2) {
                stripArray[i].insert(black[j]);
                //stripArray[i].insert(black[j + 1]);
            }
            Bar.insert(black, 10, 12);
            BearOff.insert(black, 13, 14);

            Main.players[0].setPiecesLeft(12);
            Main.players[1].setPiecesLeft(13);

        }

        static void clearBoard() {
            // Remove all pieces in the board itself
            for (Strip currentStrip : stripArray) {
                while (currentStrip.quantity != 0)
                    currentStrip.pop();
            }

            // Remove all pieces in the bar and bear off
            while (Bar.piecesIn(Color.WHITE) != 0)
                Bar.remove(Color.WHITE);
            while (Bar.piecesIn(Color.BLACK) != 0)
                Bar.remove(Color.BLACK);
            while (BearOff.piecesIn(Color.WHITE) != 0)
                BearOff.remove(Color.WHITE);
            while (BearOff.piecesIn(Color.BLACK) != 0)
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
        if (this.color == Color.BLACK) {
            this.orgStrip = 23 - orgStrip;
            this.destStrip = 23 - destStrip;

            if (orgStrip == -1) {
                this.orgStrip = -1;
            }
            if (destStrip == -2)
                this.destStrip = -2;
        } else {
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

    String isHitToString() {
        if (Classes.Board.valid(this, false)) {
            return ((color == Color.WHITE) ? (orgStrip + 1) : (23 - orgStrip + 1)) +
                    "-" +
                    ((color == Color.WHITE) ? (destStrip + 1) : (23 - destStrip + 1)) +
                    ((Classes.Board.stripArray[destStrip].quantity == 1 && Classes.Board.stripArray[destStrip].pieceColor != color) ? "*" : "");
        } else
            return "Invalid move";
    }
}

class Piece {
    Color color;
    ImageView imgView;

    Piece(Color color) {
        this.color = color;
        String url = ((color == Color.WHITE) ? "Backgammon/res/piece-white.png" : "Backgammon/res/piece-black.png");
        imgView = new ImageView(url);
        imgView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            //TODO what happens when a piece is clicked
        });
    }

    ImageView toBearOff() {
        String url = ((color == Color.WHITE) ? "Backgammon/res/side-piece-white.png" : "Backgammon/res/side-piece-black.png");
        return new ImageView(url);
    }

}

class Strip {
    VBox vBox;
    int stripID;
    int quantity = 0; // Amount of pieces in this strip
    Color pieceColor = Color.NONE;

    Strip(VBox strip, int stripID) {
        this.vBox = strip;
        this.stripID = stripID;
    }

    private void updateSpacing() {
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

    void removeHighlight() {
        for (Node n : vBox.getChildren()) {
            n.setEffect(null);
        }
    }

    void highlightStrip() {
        DropShadow ds = new DropShadow(10, javafx.scene.paint.Color.GREENYELLOW);
        final ObservableList<Node> children = vBox.getChildren();
        Node temp = children.get(0);
        if (this.stripID >= 0 && this.stripID <= 11) {
            for (Node n : vBox.getChildren()) {
                n.setEffect(null);
                temp = n;
            }
        }
        temp.setEffect(ds);
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
    public boolean isBearoff = false;

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
        ImageView v = piece.imgView;
        if (isBearoff)
            v = piece.toBearOff();
        boxes[color].getChildren().add(v);
    }

    Color remove(Color color) {
        int x = color.getValue();
        int len = pieces[x].size();
        if (len == 0)
            return null;
        pieces[x].remove(len - 1);
        boxes[x].getChildren().remove(len - 1);
        return color;
    }

    //Finds the number of pieces in the Bar for COLOR's turn
    int piecesIn(Color color) {
        if (color == Color.WHITE)
            return pieces[Color.WHITE.getValue()].size();
        else {
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

    void resetDice(int diceNo) {
        if (diceNo == 1)
            this.dice1 = 0;
        else
            this.dice2 = 0;
    }
}