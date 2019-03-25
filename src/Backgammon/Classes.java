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
        static Strip[] stripArray;
        static Color currentTurn;
        static Bar Bar;
        static Bar BearOff;
        static Dice die;
        static int currentMoves;
        static int maxMoves;

        static void setInitialPos(GridPane[] p, VBox[] bar, VBox[] bearOff, GridPane bfxml) {
            // Resets things in case this is being called for a new game
            currentTurn = null;
            stripArray = new Strip[24];
            die = new Dice();
            currentMoves = 0;
            maxMoves = 0;


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
            // < 0 ==  /move  >= 0 == listMove
            if (type < 0) {
                if (!validMove(move, 0))
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

                isMoveAHit(getStrip(move.destStrip)); // Checks if the move is a hit
                stripArray[move.orgStrip].pop();
                stripArray[move.destStrip].insert(new Piece(move.color));

                changingCurrentMoves(dist);
            } else if (move.orgStrip == -1) { // Moving from the bar
                moveFromBar(move);
            } else if (move.destStrip == -2) { // Moving to the bear-off
                moveToBearOff(move);
            }
        }

        static void moveFromBar(Move move) {
            int dist = getMoveDistFromBar(move);
            isMoveAHit(getStrip(move.destStrip));
            Piece freedom = new Piece(Bar.remove(currentTurn));
            stripArray[move.destStrip].insert(freedom);
            changingCurrentMoves(dist);
        }

        static int getMoveDistFromBar(Move move) {
            int dist = 0; // Getting the number the user input for the move
            if (currentTurn == Color.WHITE) { // Moves from bar (-1) -> stripArray[23] ....
                dist = 23 - move.destStrip + 1; // +1 takes into account moving off bar
            } else if (currentTurn == Color.BLACK) {
                dist = move.destStrip + 1;
            }
            return dist;
        }

        static void changingCurrentMoves(int dist) {
            if (maxMoves == 2) { // No doubles
                if (dist == die.getDice1()) { // Removes the used dice from being used again in this turn
                    die.resetDice(1);
                    currentMoves++;
                } else if (dist == die.getDice2()) {
                    die.resetDice(2);
                    currentMoves++;
                } else if (dist == die.getDice1() + die.getDice2()) { // Player used both dice rolls in their move
                    currentMoves = currentMoves + 2;
                }
            } else if (maxMoves == 4) { // Player rolled doubles
                int moves = (dist / die.getDice1()); // The number of times the player moved in one go
                currentMoves += moves; // Changes currentMoves by the number of times contained within dist
            }
        }

        static void moveToBearOff(Move move) {
            Piece freedom = new Piece(stripArray[move.orgStrip].pop());
            BearOff.insert(freedom);

            if (currentTurn == Color.WHITE) // Takes away a piece from the overall pieces left that a player has
                Main.players[0].setPiecesLeft(Main.players[0].getPiecesLeft() - 1);
            else if (currentTurn == Color.BLACK) {
                Main.players[1].setPiecesLeft(Main.players[1].getPiecesLeft() - 1);
            }

//            if (Main.players[currentTurn.getValue()].getPiecesLeft() == 0) // Ends the game if the player bore off their last piece
//                endGame(); // Done in controller

            int dist = 0;
            if (currentTurn == Color.WHITE) // Gets the amount moved
                dist = move.orgStrip + 1;
            else if (currentTurn == Color.BLACK)
                dist = (23 - move.orgStrip) + 1;

            if (dist == 0) // There has been an error
                return;

            if (die.getDice1() > die.getDice2()) { // Changes currentMoves based on which dice roll was used
                if (dist <= die.getDice2()) {
                    die.resetDice(2);
                    currentMoves++;
                } else if (dist <= die.getDice1()) {
                    die.resetDice(1);
                    currentMoves++;
                } else if (dist <= (die.getDice1()) + die.getDice2())
                    currentMoves = currentMoves + 2;

            } else if (die.getDice2() > die.getDice1()) {
                if (dist <= die.getDice1()) {
                    die.resetDice(1);
                    currentMoves++;
                } else if (dist <= die.getDice2()) {
                    die.resetDice(2);
                    currentMoves++;
                } else if (dist <= (die.getDice1()) + die.getDice2())
                    currentMoves = currentMoves + 2;
            } else if (maxMoves == 4) { // doubles involved
                if (dist > die.getDice1() * 3)
                    currentMoves = currentMoves + 4;
                else if (dist > (2 * die.getDice1()) && dist < (3 * die.getDice1()))
                    currentMoves = currentMoves + 3;
                else if (dist > (die.getDice1()) && dist < (2 * die.getDice1()))
                    currentMoves = currentMoves + 2;
            }
        }

        static void testMove(Move move) { // same as makeMove but allows both colors on the same strip for test purposes
            stripArray[move.orgStrip].pop();
            stripArray[move.destStrip].insert(new Piece(move.color));
        }

        static boolean validMove(Move move, int tests) {
            if (move.orgStrip < -1 || move.destStrip < -2 || move.orgStrip > 23 || move.destStrip > 23 || (move.orgStrip == move.destStrip)) // If outside of array, it's an invalid move
                return false;
            if (move.destStrip == -1) // Can probably be put in previous if, brain not working rn
                return false;
            Strip dest = null;
            Strip org = null;
            if (move.orgStrip != -1) {
                org = getStrip(move.orgStrip);
            }
            if (move.destStrip != -2) {
                dest = getStrip(move.destStrip);
            }
            /* TODO
                Logic to allow moves to use the double moves for Bar and Bear-off i.e. move 16 forward if roll double 4
                This is only for when the user uses /move not /listmove
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
                        else if (diff <= (die.getDice1() + die.getDice2()) && tests != -1) { // Combination of the die
                            Move mDie1 = new Move(move.orgStrip, move.orgStrip - die.getDice1(), currentTurn);
                            Move mDie2 = new Move(move.orgStrip, move.orgStrip - die.getDice2(), currentTurn);
                            //The individual moves could potentially be blocked by an opposing piece
                            if (validMove(mDie1, 0)) { // If the first dice leads to a valid move
                                mDie2.orgStrip = mDie1.destStrip;
                                mDie2.destStrip = -2;
                                return validMove(mDie2, 0); // Returns whether or not the second move is valid
                            } else if (validMove(mDie2, 0)) { // If the first die leads to a valid move at the start, but not the first
                                mDie1.orgStrip = mDie2.destStrip;
                                mDie1.destStrip = -2;
                                return validMove(mDie1, 0); // Returns whether or not the second move is valid
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
                        else if (diff <= (die.getDice1() + die.getDice2()) && tests != -1) { // Combination of the die
                            Move mDie1 = move;
                            mDie1.destStrip = move.orgStrip + die.getDice1();
                            Move mDie2 = move;
                            mDie2.destStrip = move.orgStrip + die.getDice2();
                            //The individual moves could potentially be blocked by an opposing piece
                            if (validMove(mDie1, 0)) { // If the first dice leads to a valid move
                                mDie2.orgStrip = mDie1.destStrip;
                                mDie2.destStrip = -2;
                                return validMove(mDie2, 0); // Returns whether or not the second move is valid
                            } else if (validMove(mDie2, 0)) { // If the first die leads to a valid move at the start, but not the first
                                mDie1.orgStrip = mDie2.destStrip;
                                mDie1.destStrip = -2;
                                return validMove(mDie1, 0); // Returns whether or not the second move is valid
                            } else
                                return false;
                        }
                    }
                    return false;
                }
                return false;
            }

            //If the player has a piece in the Bar and they try move a piece not on the bar
            //Bar is referred to as -1
            if (move.orgStrip == -1) {
                if (Bar.piecesIn(currentTurn) == 0) // Checks to see if user is moving from the bar
                    return false;
                else { // Checking to see if the move matches the dice roll
                    int dist = getMoveDistFromBar(move);

                    if (dist == die.getDice1() || dist == die.getDice2()) { // Roll matches a single dice
                        if ((currentTurn != dest.pieceColor) && dest.quantity > 1) // Destination has opposing pieces
                            return false;
                        else if ((currentTurn != dest.pieceColor) && dest.quantity == 1) // Destination has only 1 opposing piece so a hit
                            return true;
                        else {
                            return true;
                        }
                    } else if (dist == die.getDice1() + die.getDice2() && tests != -1) { // Roll matches both dice
                        Move wDie1;
                        Move wDie2;
                        if (currentTurn == Color.WHITE) {
                            wDie1 = new Move(move.orgStrip, move.destStrip + die.getDice2(), currentTurn);
                            wDie2 = new Move(move.orgStrip, move.destStrip + die.getDice1(), currentTurn);
                        } else if (currentTurn == Color.BLACK) {
                            wDie1 = new Move(0, 0, currentTurn);
                            wDie2 = new Move(0, 0, currentTurn);
                            wDie1.orgStrip = move.orgStrip;
                            wDie1.destStrip = (move.orgStrip + die.getDice2());
                            wDie2.destStrip = (move.orgStrip + die.getDice1());
                        } else
                            return false;

                        if (validMove(wDie1, 0)) { // Checking if the individual moves are valid moves, before combining them
                            return checkSecondMoveFromBar(wDie1, wDie2, move);
                        } else if (validMove(wDie2, 0)) {
                            return checkSecondMoveFromBar(wDie2, wDie1, move);
                        }
                        return false;
                    }
                    return false;
                }
            }

            if (Bar.piecesIn(currentTurn) > 0)
                return false;

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

            // Testing combination moves
            if (maxMoves == 2 && diff != die.getDice1() && diff != die.getDice2() && (diff != die.getDice1() + die.getDice2() || tests == -1)) {
                return false;
            } else if (maxMoves == 2 && maxMoves - currentMoves == 2 && tests != -1) {
                if (diff == (die.getDice1() + die.getDice2()) && (die.getDice1() != 0 || die.getDice2() == 0))
                    return checkCombinedMove(move, 2);
            } else if (maxMoves - currentMoves == 4 && tests != -1) { // Player rolled doubles and has access to 4 moves
                // In a double move both dice values are the same. If diff % dice value does not = 0, then the
                // player has not input a multiple of the dice value. If it s greater than 4 times the dice value,
                // the player has exceeded the allowed move.
                if (diff <= (4 * die.getDice1()) && diff % die.getDice1() != 0)
                    return false;
                else if (diff > (4 * die.getDice1())) // If the move is bigger than the max allowed move
                    return false;
                else if (diff % die.getDice1() == 0 && diff <= 4 * die.getDice1() && diff > die.getDice1())
                    checkCombinedMove(move, diff / die.getDice1());
            } else if (maxMoves - currentMoves == 3 && tests != -1) { // Player rolled doubles and has access to 3 moves
                if (diff <= (3 * die.getDice1()) && diff % die.getDice1() != 0)
                    return false;
                else if (diff > (3 * die.getDice1()))
                    return false;
                    // Check for combo moves, lets through a single move
                else if (diff % die.getDice1() == 0 && diff <= 3 * die.getDice1() && diff > die.getDice1()) {
                    return checkCombinedMove(move, diff / die.getDice1());
                }
            } else if (maxMoves == 4 && maxMoves - currentMoves <= 2 && tests != -1) {
                if (diff <= ((maxMoves - currentMoves) * die.getDice1()) && diff % die.getDice1() != 0) {
                    return false;
                } else if (diff > ((maxMoves - currentMoves) * die.getDice1()))
                    return false; // Player rolled doubles, has already moved twice, and then tries to combine more than allowed number of moves
                else if (diff == (2 * die.getDice1())) // 2 moves in 1
                    return checkCombinedMove(move, 2);
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

        //Checking if the second move from the Bar is a valid move
        static boolean checkSecondMoveFromBar(Move firstMove, Move secondMove, Move move) {
            secondMove.orgStrip = firstMove.destStrip;
            secondMove.destStrip = move.destStrip;
            boolean tempAdded = addTempPiece(secondMove); // Adds a temp piece if required

            int count = 0;
            int piecesInBar = Bar.piecesIn(currentTurn);
            Piece[] temp = new Piece[piecesInBar];

            while (Bar.piecesIn(currentTurn) > 0) { // Remove the pieces in the Bar temporarily so second move can be tested
                temp[count] = new Piece(Bar.remove(currentTurn));
                count++;
            }
            count--;

            if (validMove(secondMove, 0)) {
                while (count >= 0) {
                    Bar.insert(temp[count]);
                    count--;
                }
                if (tempAdded)
                    stripArray[secondMove.orgStrip].pop(); // Remove the temp piece
                return true;
            } else { // If invalid move, puts the pieces back
                while (count >= 0) {
                    Bar.insert(temp[count]);
                    count--;
                }
                if (tempAdded)
                    stripArray[secondMove.orgStrip].pop(); // Remove the temp piece
            }
            return false;
        }

        // Checks if the individual moves that make up a combined move are valid
        static boolean checkCombinedMove(Move move, int no_of_moves) {
            if (no_of_moves == 2) { // 2 moves in 1
                Move moveA;
                Move moveB;
                if (currentTurn == Color.WHITE) { // Create the 2 possible original moves
                    moveA = new Move(move.orgStrip, move.destStrip + die.getDice2(), currentTurn);
                    moveB = new Move(move.orgStrip, move.destStrip + die.getDice1(), currentTurn);
                } else if (currentTurn == Color.BLACK) {
                    moveA = new Move(0, 0, currentTurn);
                    moveB = new Move(0, 0, currentTurn);
                    moveA.orgStrip = move.orgStrip;
                    moveA.destStrip = move.orgStrip + die.getDice1();
                    moveB.orgStrip = move.orgStrip;
                    moveB.destStrip = move.orgStrip + die.getDice2();
                } else // Shouldn't get here, but to remove warnings. But fair play if it makes it here.
                    return false;

                if (validMove(moveA, 0)) {
                    moveB.orgStrip = moveA.destStrip;
                    moveB.destStrip = move.destStrip;
                    return checkFinalMove(move, moveB);
                } else if (validMove(moveB, 0)) {
                    moveA.orgStrip = moveB.destStrip;
                    moveA.destStrip = move.destStrip;
                    return checkFinalMove(move, moveA);
                }
            } else {
                Move moveA = new Move(move.orgStrip, move.orgStrip + die.getDice1(), currentTurn);
                if (currentTurn == Color.BLACK) {
                    moveA.orgStrip = move.orgStrip;
                    moveA.destStrip = move.orgStrip + die.getDice1();
                }
                if (no_of_moves == 3) { // At this point, each move will move the same amount
                    if (validMove(moveA, 0)) {
                        Move moveB = new Move(moveA.destStrip, move.destStrip, currentTurn); // Combo move of the remaining moves
                        return validMove(moveB, 0);
                    }
                    // No need to test other moves as they are all the same move. 3
                    return false;
                } else if (no_of_moves == 4) {
                    if (validMove(moveA, 0)) {
                        Move moveB = new Move(moveA.destStrip, move.destStrip, currentTurn);
                        return validMove(moveB, 0);
                    }
                }
                return false;
            }
            return false;
        }

        static boolean checkFinalMove(Move fullMove, Move finalMove) {
            boolean tempAdded = addTempPiece(finalMove);
            if (validMove(finalMove, 0)) {
                if (tempAdded)
                    stripArray[finalMove.orgStrip].pop();
                return true;
            }
            if (tempAdded)
                stripArray[finalMove.orgStrip].pop();

            return false;
        }

        static boolean addTempPiece(Move secondMove) {
            // Input a temp piece if empty, for checking if it's a valid move.
            if (stripArray[secondMove.orgStrip].pieceColor == Color.NONE) {
                stripArray[secondMove.orgStrip].insert(new Piece(currentTurn));
                return true;
            } else if (currentTurn == Color.BLACK && stripArray[secondMove.orgStrip].pieceColor == Color.WHITE) {
                stripArray[secondMove.orgStrip].pop();
                stripArray[secondMove.orgStrip].insert(new Piece(currentTurn));
                return true;
            } else if (currentTurn == Color.WHITE && stripArray[secondMove.orgStrip].pieceColor == Color.BLACK) {
                stripArray[secondMove.orgStrip].pop();
                stripArray[secondMove.orgStrip].insert(new Piece(currentTurn));
                return true;
            }
            return false;
        }

        static void removeTempPiece(Move move) {
            stripArray[move.orgStrip].pop();
        }

        static void hitMove(Strip dest) {
            Piece ripPiece = new Piece(dest.pop());
            Bar.insert(ripPiece);
        }

        static void isMoveAHit(Strip dest) { // If the move is a hit, removes the opposing piece in preparation for the move
            if ((currentTurn != dest.pieceColor) && dest.quantity == 1) // Destination has only 1 opposing piece so a hit
                hitMove(dest);
        }

        static boolean checkHitMove(Strip dest) {
            return ((currentTurn != dest.pieceColor) && dest.quantity == 1);
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
                if (showErrors)                                                   // will likely only be used in findBoardMoves()
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

        static ArrayList<Move> findAllValidMoves() {
            ArrayList<Move> boardMoves = new ArrayList<>();
            for (int i = -1; i < 24; i++) {
                for (int j = -2; j < 24; j++) {
                    Move curr = new Move(i, j, currentTurn);
                    if (validMove(curr, -1)) {
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

       /* static ArrayList<Move> findBarMoves(){
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
*/
        //Also dummy

        /*static ArrayList<Move>findBearOffMoves(){
            ArrayList<Move> bearOffMoves = new ArrayList<>();
            bearOffMoves.add(new Move(21, -2, currentTurn));
            bearOffMoves.add(new Move(22, -2, currentTurn));
            bearOffMoves.add(new Move(23, -2, currentTurn));

            return bearOffMoves;
        }*/

        //still dummy

       /* static boolean allHomeBoard(){
            return false;
        }*/

        //more dummy


        static void removeDuplicateCombos(ArrayList<MoveCombo> combos) {
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
                - findBearOffMoves()        i.e. moves starting on tbe board and ending in the bear-off                                   ~~TODO~~  (unnecessary if validMove already catches them)
                - removeMovesStartingOn()   i.e. exactly what it sound like, takes an arrayList of moves and an int,                      DONE in-line instead
                                            and modifies that arrayList to remove any moves with an orgStrip of that int
                - allHomeBoard()            Returns boolean on whether all your pieces are in your home board, needed to determine        ~~TODO~~  (only used for findBearOffMoves, so also possibly unnecessary)
                                            whether you can start bearing-off pieces yet
                - removeDuplicateCombos()   Removes combinations of moves that have the same orgStrip for the first move,                 TODO  NB still very much necessary
                                            same destStrip for the last move, the firstMove's destStrip is the secondMove's orgStrip
                                            and none of the strips landed on in between resulted in hits.
                                            (Need two different versions for 2-move combos vs 4-move combos?)
                                            (should be a method of MoveCombo class?)

                                            Should it also remove Combos that are moving separate pieces, but still result in the same board state?
                                            Like "6-7 9-10" and "9-10 6-7"?

                                                                //Also TODO fix lines 705-716 (at time of writing) about adding new moves if first move opens up new options

                */


            //Bar.insert(new Piece(currentTurn));
            //Bar.insert(new Piece(currentTurn));


            ArrayList<Move> allMoves = new ArrayList<>(findAllValidMoves());

            //below may be redundant

                 /*if(Bar.piecesIn(currentTurn) >= maxMoves){              // i.e., if you have more (or same number of) pieces on the bar than you have dice
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
                 }*/

            ArrayList<Move> copyAllMoves;                             // made so that when combining moves into pairs (or quadruplets when a double is rolled),
            // you can temporarily change which moves are valid without affecting the master copy


            ArrayList<MoveCombo> allCombos = new ArrayList<>();

            //code to combine multiple moves in pairs to print -

            System.out.println("allMoves initially looks like: " + allMoves);

            if (maxMoves == 2) {
                for (Move firstMove : allMoves) {
                    copyAllMoves = new ArrayList<>(allMoves);
                    if(firstMove.destStrip == -2 && firstMove.orgStrip != -1 && getStrip(firstMove.orgStrip).pieceColor == currentTurn){
                        if(validMove(firstMove,1)){
                            allCombos.add(new MoveCombo(1,firstMove));
                        }
                    }
                    else if (Bar.piecesIn(currentTurn) == 0) {      // how to code orgStrip==Bar, since currently orgStrip is an int only?
                        // maybe reserve an int like 0 to represent the bar, and then 1-24
                        // can actually correspond to what you'd expect on the board?

                        //System.out.println("made it");

                        // Update: seem to have gone with -1 representing bar
                        // which importantly can also end up as 24 due to the way Moves constructor handles different colors

                        // if statement means that if there are no possible bar moves which
                        // would have taken priority, then the first move of the pair can be anything,
                        // but if there are bar moves possible (i.e. if bar.quantity>0),
                        // then the first move has to be one of those.

                        if (firstMove.destStrip > -1 && stripArray[firstMove.destStrip].pieceColor != currentTurn && validMove(new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice1(), currentTurn), -1)) {
                            copyAllMoves.add(new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice1(), currentTurn));
                            System.out.println("Added: " + new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice1(), currentTurn)); //for troubleshooting
                        }   //doesn't seem to be catching anything, why?

                        // i.e. assuming the first move is made and there is now a piece on destStrip where there wasn't before,
                        // does that produce any new valid moves that weren't available before? Checks both dice1 and dice2.

                        if (firstMove.destStrip > -1 && stripArray[firstMove.destStrip].pieceColor != currentTurn && validMove(new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice2(), currentTurn), -1)) {
                            copyAllMoves.add(new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice2(), currentTurn));
                            System.out.println("Added: " + new Move(firstMove.destStrip, firstMove.destStrip + Board.die.getDice2(), currentTurn));
                        }

                        //Conversely, also need to check if any moves are no longer possible now. This would only happen if there are
                        //no more pieces left on orgStrip after the move is made, i.e. if there is currently only one piece on orgStrip

                        if (firstMove.orgStrip >= 0 && firstMove.orgStrip < 24 && stripArray[firstMove.orgStrip].quantity <= 1) {  //<=1 to make testing easier, really could be ==1
                            //System.out.println("Removing moves starting on same");
                            copyAllMoves.removeIf(m -> m.orgStrip == firstMove.orgStrip);
                            //System.out.println("Removed successfully");
                        } else if (Bar.piecesIn(currentTurn) <= 1) {  // i.e. when orgStrip = -1 (Bar)
                            copyAllMoves.removeIf(m -> m.orgStrip == firstMove.orgStrip);
                        }

                        // >=0 and <24 conditions included so that it doesn't try to dereference something like stripArray[-1] for bar moves

                        //keep in mind we also need to remove any barMoves which are no longer possible,
                        //above if statement might already be sufficient if we go the route of assigning the bar an index that's actually included in stripArray
                        //if not, need separate function

                        for (Move secondMove : copyAllMoves) {
                            if(firstMove.destStrip == -1  || firstMove.orgStrip == -1){
                                System.out.println("test");
                            }
                            else if(getStrip(firstMove.orgStrip).pieceColor != currentTurn)
                                System.out.println("test2");
                            else if (Math.abs(firstMove.orgStrip - firstMove.destStrip) + Math.abs(secondMove.orgStrip - secondMove.destStrip) == Board.die.getDice1() + Board.die.getDice2()) {
                                //commented out because still testing, so will rarely actually match dice numbers. Seems to work fine though
                                //System.out.println("Combined distance = " + (Math.abs(firstMove.orgStrip-firstMove.destStrip) + Math.abs(secondMove.orgStrip-secondMove.destStrip)));
                                //System.out.println("Should be: " + (Board.die.getDice1() + Board.die.getDice2()));
                                allCombos.add(new MoveCombo(2, firstMove, secondMove));
                            }
                        }
                    } else if (Bar.piecesIn(currentTurn) > 0) { // Getting the bar moves to show up on the list
                        if (Bar.piecesIn(currentTurn) == 1 && maxMoves - currentMoves == 2) { // There can be a follow up move
                            Piece yoink = new Piece(Bar.remove(currentTurn)); // Just to check a follow up move
                            int dist = prepForTestBarCombinedMoves(firstMove);
                            int want = (die.getDice1() + die.getDice2()) - dist;
                            Move temp = createNextMoveForBar(firstMove,want);
                            boolean tempAdded = addTempPiece(temp);
                            if (validMove(temp, 0)) {
                                if (tempAdded)
                                    removeTempPiece(temp);
                                Bar.insert(yoink);
                                allCombos.add(new MoveCombo(2, firstMove, temp));
                            } else {
                                if (tempAdded)
                                    removeTempPiece(temp);
                            }
                        } else {
                            allCombos.add(new MoveCombo(1, firstMove));
                        }
                    }
                }

                // Need to add the possible follow on move. i.e. 13 -7 7 -5 if player rolls a 6 and 2, and 7 doesn't have a piece originally
                for (int i = 0; i < allMoves.size(); i++) {
                    if(allMoves.get(i).destStrip == -2){
                        System.out.println("test");
                    }else{
                        Move temp = new Move(allMoves.get(i).destStrip, allMoves.get(i).orgStrip - (die.getDice1() + die.getDice2()), currentTurn);
                        if (currentTurn == Color.BLACK) { // This is done as initialising a new black move will change the desired org and dest strip
                            temp.orgStrip = allMoves.get(i).destStrip;
                            temp.destStrip = allMoves.get(i).orgStrip + (die.getDice1() + die.getDice2());
                        }

                        boolean tempAdded = false;
                        if(temp.destStrip != -2){
                            tempAdded = addTempPiece(temp); // Add a temporary piece if required for checking if it's a valid follow up move
                        }
                        if (validMove(temp, 0)) {
                            if (tempAdded) {
                                removeTempPiece(temp);
                            }
                            allCombos.add(new MoveCombo(2, allMoves.get(i), temp));
                        } else {
                            if (tempAdded) {
                                removeTempPiece(temp);
                            }
                        }
                    }
                }
            } else if (maxMoves == 4) {
                if (Bar.piecesIn(currentTurn) == 0) {
                    for (Move firstMove : allMoves) { // All the individual moves
                        allCombos.add(new MoveCombo(1, firstMove));
                    }
                    if (maxMoves - currentMoves >= 2) {
                        for (Move firstMove : allMoves) { // All 2 move combos
                            Move secondMove = createNextMove(firstMove);
                            boolean tempAdded = addTempPiece(secondMove); // For checking the temp move
                            if (validMove(secondMove, 0)) {
                                if (tempAdded) {
                                    removeTempPiece(secondMove);
                                }
                                allCombos.add(new MoveCombo(2, firstMove, secondMove));
                            } else {
                                if (tempAdded) {
                                    removeTempPiece(secondMove);
                                }
                            }
                        }
                    }
                    if (maxMoves - currentMoves >= 3) { // Getting the 3 move combinations
                        for (Move firstMove : allMoves) { // I could use my checkCombinedMoves function, but then it wouldn't return the middle hit moves
                            Move secondMove = createNextMove(firstMove);
                            boolean tempAdded = addTempPiece(secondMove); // For checking the temp move
                            if (validMove(secondMove, 0)) { // Checks if the second move is valid. If yes, move on to checking the third
                                if (tempAdded) {
                                    removeTempPiece(secondMove);
                                }

                                // Creating the third move
                                Move thirdMove = createNextMove(secondMove);

                                boolean tempAdded2 = addTempPiece(thirdMove);

                                if (validMove(thirdMove, 0)) { // Checking if the third move is valid, if yes then create the 3 move combo
                                    if (tempAdded2) {
                                        removeTempPiece(thirdMove);
                                    }
                                    allCombos.add(new MoveCombo(3, firstMove, secondMove, thirdMove));
                                } else {
                                    if (tempAdded2) {
                                        removeTempPiece(thirdMove);
                                    }
                                }
                            } else {
                                if (tempAdded) {
                                    removeTempPiece(secondMove);
                                }
                            }
                        }
                    }
                    if (maxMoves - currentMoves == 4) {
                        for (Move firstMove : allMoves) { // I could use my checkCombinedMoves function, but then it wouldn't return the middle hit moves
                            Move secondMove = createNextMove(firstMove);
                            boolean tempAdded = addTempPiece(secondMove); // For checking the temp move
                            if (validMove(secondMove, 0)) { // Checks if the second move is valid. If yes, move on to checking the third
                                if (tempAdded) {
                                    removeTempPiece(secondMove);
                                }

                                // Creating the third move
                                Move thirdMove = createNextMove(secondMove);

                                boolean tempAdded2 = addTempPiece(thirdMove);

                                if (validMove(thirdMove, 0)) { // Checking if the third move is valid, if yes then create the 3 move combo
                                    if (tempAdded2) {
                                        removeTempPiece(thirdMove); // Removes the temp piece
                                    }

                                    // Creating the fourth move
                                    Move fourthMove = createNextMove(thirdMove);
                                    boolean tempAdded3 = addTempPiece(fourthMove);

                                    if (validMove(fourthMove, 0)) {
                                        if (tempAdded3) {
                                            removeTempPiece(fourthMove);
                                        }
                                        allCombos.add(new MoveCombo(4, firstMove, secondMove, thirdMove, fourthMove));
                                    } else {
                                        if (tempAdded3) {
                                            removeTempPiece(fourthMove);
                                        }
                                    }
                                } else {
                                    if (tempAdded2) {
                                        removeTempPiece(thirdMove);
                                    }
                                }
                            } else {
                                if (tempAdded) {
                                    removeTempPiece(secondMove);
                                }
                            }
                        }
                    }
                }else if(Bar.piecesIn(currentTurn) > 0){
                    if(Bar.piecesIn(currentTurn) == 1 && maxMoves - currentMoves > 1 && maxMoves - currentMoves <= 4){
                        Piece yoink = new Piece(Bar.remove(currentTurn));
                        int dist = prepForTestBarCombinedMoves(allMoves.get(0)); // The value is the same for all moves
                        int want = (die.getDice1() + die.getDice2()) - dist;
                        for(Move first: allMoves){
                            Move second = createNextMoveForBar(first,want);
                            boolean tempAdded = addTempPiece(second);
                            if(validMove(second,0)){
                                if(tempAdded)
                                    removeTempPiece(second);

                                if(maxMoves - currentMoves > 2){
                                    Move third = createNextMoveForBar(second,want);
                                    boolean tempAdded2 = addTempPiece(third);
                                    if(validMove(third,0)){
                                        if(tempAdded2)
                                            removeTempPiece(third);

                                        if(maxMoves - currentMoves > 3){
                                            Move fourth = createNextMoveForBar(third,want);
                                            boolean tempAdded3 = addTempPiece(fourth);
                                            if(validMove(fourth,0)){
                                                if(tempAdded3){
                                                    removeTempPiece(fourth);
                                                }
                                                allCombos.add(new MoveCombo(4,first,second,third,fourth));
                                            }else{
                                                if(tempAdded3)
                                                    removeTempPiece(fourth);
                                            }
                                        }else{
                                            allCombos.add(new MoveCombo(3,first,second,third));
                                            if(tempAdded2)
                                                removeTempPiece(third);
                                        }
                                    }
                                }else{
                                    allCombos.add(new MoveCombo(2,first,second));
                                    if(tempAdded)
                                        removeTempPiece(second);
                                }
                            }
                            Bar.insert(yoink);
                        }

                    }else{
                        for(Move firstMove : allMoves)
                            allCombos.add(new MoveCombo(1,firstMove));
                    }
                }
            }

            //More TODO's

            //also need to provide code for when maxMoves = 4

            //also need to provide code for situations when only one valid move is found (and 3 as well I guess)

            //if (plays with 2 moves are present)
            //remove plays with less than 2 moves

            //repeat for 3 and less, and 3 and less

            removeDuplicateCombos(allCombos);


            System.out.println("All moves starts here");
            for (int i = 0; i < allMoves.size(); i++)
                System.out.println(allMoves.get(i).isHitToString());
            System.out.println("all moves stops here");
            return allCombos;

        }

        // Creating the next move when testing combined moves
        static Move createNextMove(Move prevMove){
            Move nextMove = new Move(prevMove.destStrip, prevMove.orgStrip - (die.getDice1() + die.getDice2()), currentTurn);
            if (currentTurn == Color.BLACK) {
                nextMove.orgStrip = prevMove.destStrip;
                nextMove.destStrip = prevMove.orgStrip + (die.getDice1() + die.getDice2());
            }
            return nextMove;
        }

        static Move createNextMoveForBar(Move prevMove, int want){
            Move temp = new Move(prevMove.destStrip, prevMove.destStrip - want, currentTurn);
            if (currentTurn == Color.BLACK) {
                temp.orgStrip = prevMove.destStrip;
                temp.destStrip = prevMove.destStrip + want;
            }

            return temp;
        }

        static int prepForTestBarCombinedMoves(Move firstMove){
            int dist = 23 - firstMove.destStrip + 1;
            if (currentTurn == Color.BLACK) {
                dist = firstMove.destStrip + 1;
            }
            return dist;
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
                stripArray[i].insert(black[j + 1]);
            }
            Bar.insert(black, 10, 12);
            BearOff.insert(black, 13, 14);

            Main.players[0].setPiecesLeft(12);
            Main.players[1].setPiecesLeft(13);

            Board.currentTurn = Color.WHITE;
            Board.currentMoves = 0;
            boardfxml.setId("board" + currentTurn.getValue());
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
       /* if (Classes.Board.validMove(this,0))
            return "Move: Origin: " + (orgStrip + 1) + " Destination: " + (destStrip + 1);
        else
            return "Invalid move";*/

        if (Classes.Board.validMove(this, 0)) {
            return ((orgStrip == -1 || orgStrip == 24) ? "Bar" : ((color == Color.WHITE) ? (orgStrip + 1) : (23 - orgStrip + 1))) +   //might be able to remove || == 24 check
                    "-" +
                    (destStrip == -2 ? "Off" : ((color == Color.WHITE) ? (destStrip + 1) : (23 - destStrip + 1)) +
                            ((Classes.Board.stripArray[destStrip].quantity == 1 && Classes.Board.stripArray[destStrip].pieceColor != color) ? "*" : ""));
        } else
            return "Invalid move";
    }

    String isHitToString() {
        String m = "";

        boolean tempAdded = false;
        if (this.orgStrip != -1) {
            tempAdded = Classes.Board.addTempPiece(this);
        }
        if (!Classes.Board.validMove(this, 0))
            m = "this - ";

        if (tempAdded)
            Classes.Board.removeTempPiece(this);
        return m + ((orgStrip == -1 || orgStrip == 24) ? "Bar" : ((color == Color.WHITE) ? (orgStrip + 1) : (23 - orgStrip + 1))) +   //might be able to remove || == 24 check
                "-" +
                (destStrip == -2 ? "Off" : ((color == Color.WHITE) ? (destStrip + 1) : (23 - destStrip + 1)) +
                        ((Classes.Board.stripArray[destStrip].quantity == 1 && Classes.Board.stripArray[destStrip].pieceColor != color) ? "*" : ""));

    }
}

class MoveCombo {
    Move[] moves = new Move[4];
    int numMovesPerCombo;   //needed? or just change moves to ArrayList and can then use moves.size()?

    MoveCombo(int num, Move... args) {
        int i = 0;
        for (Move m : args) {
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