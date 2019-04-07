package Backgammon;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static java.lang.Math.min;

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
                if(strip.vBox.equals(box))
                    return strip;
            }
            return null;
        }

        static void makeMove(Move move, int type) {
            // Type refers to whether a move comes from /move or /listmove
            // Moves from /listmove have already been checked if it is valid so it doesn't need to check again
            // < 0 ==  /move  >= 0 == listMove
            if(type < 0) {
                if(!validMove(move, 0))
                    return;
            }
            // Normal move
            if (move.orgStrip != -1 && move.destStrip >= 0) {
                int dist = getMoveDist(move);

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

        static int getMoveDist(Move move){
            int dist = 0;
            // Getting which dice number was used.
            if (currentTurn == Color.WHITE)
                dist = move.orgStrip - move.destStrip;
            else if (currentTurn == Color.BLACK)
                dist = move.destStrip - move.orgStrip;

            return dist;
        }

        static void moveFromBar(Move move) { // Remove the piece from the bar and move it to the board.
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
            } else if(currentTurn == Color.BLACK) {
                dist = move.destStrip + 1;
            }
            return dist;
        }

        static void changingCurrentMoves(int dist) {
            System.out.println(dist);
            if(maxMoves == 2) { // No doubles
                if(dist == die.getDice1()) { // Removes the used dice from being used again in this turn
                    die.resetDice(1);
                    currentMoves++;
                } else if(dist == die.getDice2()) {
                    die.resetDice(2);
                    currentMoves++;
                } else if(dist == die.getDice1() + die.getDice2()) { // Player used both dice rolls in their move
                    currentMoves = currentMoves + 2;
                }
            } else if(maxMoves == 4) { // Player rolled doubles
                int moves = (dist / die.getDice1()); // The number of times the player moved in one go
                currentMoves += moves; // Changes currentMoves by the number of times contained within dist
            }
        }

        static void moveToBearOff(Move move) {
            System.out.println("in bear move " + move.orgStrip);
            Piece freedom = new Piece(stripArray[move.orgStrip].pop());
            BearOff.insert(freedom);

            if(currentTurn == Color.WHITE) // Takes away a piece from the overall pieces left that a player has
                Main.players[0].setPiecesLeft(Main.players[0].getPiecesLeft() - 1);
            else if(currentTurn == Color.BLACK) {
                Main.players[1].setPiecesLeft(Main.players[1].getPiecesLeft() - 1);
            }

            int dist = 0;
            if(currentTurn == Color.WHITE) // Gets the amount moved
                dist = move.orgStrip + 1;
            else if(currentTurn == Color.BLACK)
                dist = (23 - move.orgStrip) + 1;

            if(dist == 0) // There has been an error
                return;
            // TODO Can replace later code with this. Commented it out as it was untested.
           /* if(getDiceMoveAtBearOff(move) == 1 && maxMoves == 2){
                die.resetDice(1);
                currentMoves++;
            }else if(getDiceMoveAtBearOff(move) == 2 && maxMoves == 2){
                die.resetDice(2);
                currentMoves++;
            }*/

            if(die.getDice1() > die.getDice2()) { // Changes currentMoves based on which dice roll was used
                if(dist <= die.getDice2()) {
                    die.resetDice(2);
                    currentMoves++;
                } else if(dist <= die.getDice1()) {
                    die.resetDice(1);
                    currentMoves++;
                } else if(dist <= (die.getDice1()) + die.getDice2())
                    currentMoves = currentMoves + 2;

            } else if(die.getDice2() > die.getDice1()) {
                if(dist <= die.getDice1()) {
                    die.resetDice(1);
                    currentMoves++;
                } else if(dist <= die.getDice2()) {
                    die.resetDice(2);
                    currentMoves++;
                } else if(dist <= (die.getDice1()) + die.getDice2())
                    currentMoves = currentMoves + 2;
            } else if(maxMoves == 4) { // doubles involved
                if(dist > die.getDice1() * 3)
                    currentMoves = currentMoves + 4;
                else if(dist > (2 * die.getDice1()) && dist < (3 * die.getDice1()))
                    currentMoves = currentMoves + 3;
                else if(dist > (die.getDice1()) && dist < (2 * die.getDice1()))
                    currentMoves = currentMoves + 2;
                else {
                    currentMoves++;
                }
            }
        }

        static int getDiceMoveAtBearOff(Move move) {
            int dist = 0;
            if (currentTurn == Color.WHITE) // Gets the amount moved
                dist = move.orgStrip + 1;
            else if (currentTurn == Color.BLACK)
                dist = (23 - move.orgStrip) + 1;

            if (die.getDice1() > die.getDice2()) { // Returns which dice was used.
                if (dist <= die.getDice2()) {
                    return 2;
                } else if (dist <= die.getDice1()) {
                    return 1;
                }
            } else if (die.getDice2() > die.getDice1()) {
                if (dist <= die.getDice1()) {
                    return 1;
                } else if (dist <= die.getDice2()) {
                    return 2;
                }
            }

            return 0;
        }

        static void testMove(Move move) { // same as makeMove but allows both colors on the same strip for test purposes
            stripArray[move.orgStrip].pop();
            stripArray[move.destStrip].insert(new Piece(move.color));
        }

        static boolean validMove(Move move, int tests) {
            if(move.orgStrip < -1 || move.destStrip < -2 || move.orgStrip > 23 || move.destStrip > 23 || (move.orgStrip == move.destStrip)) // If outside of array, it's an invalid move
                return false;
            if(move.destStrip == -1) // Can probably be put in previous if, brain not working rn
                return false;
            Strip dest = null;
            Strip org = null;
            if(move.orgStrip != -1) {
                org = getStrip(move.orgStrip);
            }
            if(move.destStrip != -2) {
                dest = getStrip(move.destStrip);
            }
            /* TODO
                Logic to allow moves to use the double moves for Bar and Bear-off i.e. move 16 forward if roll double 4
                This is only for when the user uses /move not /listmove
            */

            // User wants to move to the bear-off, this checks if it is allowed. This is done before the bar check
            // as moving to the bear-off has a different check for move according to the die. Bear-off == -2
            if(move.destStrip == -2) {
                if(move.orgStrip < 0)
                    return false;
                int diff;
                if(currentTurn == Color.WHITE) {
                    diff = move.orgStrip + 1; // Adding 1 since destination will be from org to 0th position +1 to the bear-off

                    int count = piecesInHomeBoard(); // Check if all pieces are in the home-board.
                    if(count == Main.players[0].getPiecesLeft()) {
                        if(diff == die.getDice1()) {// If the first die will bring you to bear-off
                            return org.quantity > 0 && org.pieceColor == currentTurn;
                        } else if(diff == die.getDice2()) // If the second die will bring you to bear-off
                            return org.quantity > 0 && org.pieceColor == currentTurn;
                        else if(diff <= (die.getDice1() + die.getDice2()) && tests != -1) { // Combination of the die
                            Move mDie1 = new Move(move.orgStrip, move.orgStrip - die.getDice1(), currentTurn);
                            Move mDie2 = new Move(move.orgStrip, move.orgStrip - die.getDice2(), currentTurn);
                            //The individual moves could potentially be blocked by an opposing piece
                            if(validMove(mDie1, 0)) { // If the first dice leads to a valid move
                                mDie2.orgStrip = mDie1.destStrip;
                                mDie2.destStrip = -2;
                                return validMove(mDie2, 0); // Returns whether or not the second move is valid
                            } else if(validMove(mDie2, 0)) { // If the first die leads to a valid move at the start, but not the first
                                mDie1.orgStrip = mDie2.destStrip;
                                mDie1.destStrip = -2;
                                return validMove(mDie1, 0); // Returns whether or not the second move is valid
                            } else {
                                return false;
                            }
                        }
                        // A move to bear-off with a dice cannot be done if there is a valid move
                        else if(die.getDice2() > die.getDice1()) {
                            if(diff < die.getDice2() && org.quantity > 0)
                                return checkBehindBeforeBearOff(move.orgStrip);
                        } else if(die.getDice1() > die.getDice2()) {
                            if(diff < die.getDice1() && org.quantity > 0) {
                                return checkBehindBeforeBearOff(move.orgStrip);
                            }
                        }
                    }
                    return false;
                } else if(currentTurn == Color.BLACK) {
                    diff = (23 - move.orgStrip) + 1;
                    int count = piecesInHomeBoard(); // Check if all pieces are in the home-board
                    if(count == Main.players[1].getPiecesLeft()) {
                        if(diff == die.getDice1()) // If the first die will bring you to bear-off
                            return org.quantity > 0 && org.pieceColor == currentTurn;
                        else if(diff == die.getDice2()) // If the second die will bring you to bear-off
                            return org.quantity > 0 && org.pieceColor == currentTurn;
                        else if(diff <= (die.getDice1() + die.getDice2()) && tests != -1) { // Combination of the die
                            Move mDie1 = new Move(move.orgStrip, move.destStrip, currentTurn);
                            mDie1.destStrip = move.orgStrip + die.getDice1();
                            Move mDie2 = new Move(move.orgStrip, move.destStrip, currentTurn);
                            mDie2.destStrip = move.orgStrip + die.getDice2();
                            //The individual moves could potentially be blocked by an opposing piece
                            if(validMove(mDie1, 0)) { // If the first dice leads to a valid move
                                mDie2.orgStrip = mDie1.destStrip;
                                mDie2.destStrip = -2;
                                return validMove(mDie2, 0); // Returns whether or not the second move is valid
                            } else if(validMove(mDie2, 0)) { // If the first die leads to a valid move at the start, but not the first
                                mDie1.orgStrip = mDie2.destStrip;
                                mDie1.destStrip = -2;
                                return validMove(mDie1, 0); // Returns whether or not the second move is valid
                            } else
                                return false;
                        }
                        // Cannot bear-off if there is a piece behind it
                        else if(die.getDice2() > die.getDice1()) {
                            if(diff < die.getDice2() && org.quantity > 0)
                                return checkBehindBeforeBearOff(move.orgStrip);
                        } else if(die.getDice1() > die.getDice2()) {
                            if(diff < die.getDice1() && org.quantity > 0) {
                                return checkBehindBeforeBearOff(move.orgStrip);
                            }
                        }
                    }
                    return false;
                }
                return false;
            }

            //If the player has a piece in the Bar and they try move a piece not on the bar
            //Bar is referred to as -1
            if(move.orgStrip == -1) {
                if(Bar.piecesIn(currentTurn) == 0) // Checks to see if user is moving from the bar
                    return false;
                else { // Checking to see if the move matches the dice roll
                    int dist = getMoveDistFromBar(move);

                    if(dist == die.getDice1() || dist == die.getDice2()) { // Roll matches a single dice
                        if((currentTurn != dest.pieceColor) && dest.quantity > 1) // Destination has opposing pieces
                            return false;
                        else if((currentTurn != dest.pieceColor) && dest.quantity == 1) // Destination has only 1 opposing piece so a hit
                            return true;
                        else {
                            return true;
                        }
                    } else if(dist == die.getDice1() + die.getDice2() && tests != -1) { // Roll matches both dice
                        Move wDie1;
                        Move wDie2;
                        if(currentTurn == Color.WHITE) {
                            wDie1 = new Move(move.orgStrip, move.destStrip + die.getDice2(), currentTurn);
                            wDie2 = new Move(move.orgStrip, move.destStrip + die.getDice1(), currentTurn);
                        } else if(currentTurn == Color.BLACK) {
                            wDie1 = new Move(0, 0, currentTurn);
                            wDie2 = new Move(0, 0, currentTurn);
                            wDie1.orgStrip = move.orgStrip;
                            wDie1.destStrip = (move.orgStrip + die.getDice2());
                            wDie2.destStrip = (move.orgStrip + die.getDice1());
                        } else
                            return false;

                        if(validMove(wDie1, 0)) { // Checking if the individual moves are valid moves, before combining them
                            return checkSecondMoveFromBar(wDie1, wDie2, move);
                        } else if(validMove(wDie2, 0)) {
                            return checkSecondMoveFromBar(wDie2, wDie1, move);
                        }
                        return false;
                    }
                    return false;
                }
            }

            if(Bar.piecesIn(currentTurn) > 0) // Just a catch to remove warnings.
                return false;

            // Ensures user does not go backwards
            if(currentTurn == Color.BLACK) {
                if(move.orgStrip > move.destStrip)
                    return false;
            } else if(currentTurn == Color.WHITE)
                if(move.orgStrip < move.destStrip)
                    return false;

            //Ensure the player uses a move related to the dice roll. Moved from Eric's valid function
            int displayedDest = (move.color == Color.WHITE) ? (move.destStrip + 1) : (23 - move.destStrip) + 1;    //yes, math for 23-org+1 could be simplified, but I
            int displayedOrg = (move.color == Color.WHITE) ? (move.orgStrip + 1) : (23 - move.orgStrip) + 1;      //kept it that way so that it's easier to make sense of,
            int diff = displayedOrg - displayedDest;

            // Testing combination moves
            if(maxMoves == 2 && diff != die.getDice1() && diff != die.getDice2() && (diff != die.getDice1() + die.getDice2() || tests == -1)) {
                return false;
            } else if(maxMoves == 2 && maxMoves - currentMoves == 2 && tests != -1) {
                if(diff == (die.getDice1() + die.getDice2()) && (die.getDice1() != 0 || die.getDice2() == 0))
                    return checkCombinedMove(move, 2);
            } else if(maxMoves - currentMoves == 4 && tests != -1) { // Player rolled doubles and has access to 4 moves
                // In a double move both dice values are the same. If diff % dice value does not = 0, then the
                // player has not input a multiple of the dice value. If it s greater than 4 times the dice value,
                // the player has exceeded the allowed move.
                if(diff <= (4 * die.getDice1()) && diff % die.getDice1() != 0)
                    return false;
                else if(diff > (4 * die.getDice1())) // If the move is bigger than the max allowed move
                    return false;
                else if(diff % die.getDice1() == 0 && diff <= 4 * die.getDice1() && diff > die.getDice1())
                    checkCombinedMove(move, diff / die.getDice1());
            } else if(maxMoves - currentMoves == 3 && tests != -1) { // Player rolled doubles and has access to 3 moves
                if(diff <= (3 * die.getDice1()) && diff % die.getDice1() != 0)
                    return false;
                else if(diff > (3 * die.getDice1()))
                    return false;
                    // Check for combo moves, lets through a single move
                else if(diff % die.getDice1() == 0 && diff <= 3 * die.getDice1() && diff > die.getDice1()) {
                    return checkCombinedMove(move, diff / die.getDice1());
                }
            } else if(maxMoves == 4 && maxMoves - currentMoves <= 2 && tests != -1) {
                if(diff <= ((maxMoves - currentMoves) * die.getDice1()) && diff % die.getDice1() != 0) {
                    return false;
                } else if(diff > ((maxMoves - currentMoves) * die.getDice1()))
                    return false; // Player rolled doubles, has already moved twice, and then tries to combine more than allowed number of moves
                else if(diff == (2 * die.getDice1())) // 2 moves in 1
                    return checkCombinedMove(move, 2);
            }

            // Just checking a normal move, no bar/bear-off
            if(org.quantity == 0 || (org.pieceColor != currentTurn)) // Trying to move opponents piece or move nothing
                return false;
            if((org.pieceColor != dest.pieceColor) && dest.quantity > 1) { // If the dest strip has pieces of the opposite color,
                return false; // it's an invalid move
            } else if((org.pieceColor != dest.pieceColor) && dest.quantity == 1) { // This move is a hit to bar
                return true;
            } else if((org.pieceColor != dest.pieceColor) && dest.quantity == 0)// If the dest piece is empty
                return true;
            // If the player is moving a piece that isn't his
            return move.color == dest.pieceColor;
        }

        static int piecesInHomeBoard() {
            int count = 0;
            if(currentTurn == Color.WHITE) {
                for (int i = 0; i < 6; i++) { //Checks if the bear-off can be attained in the first place
                    if(stripArray[i].pieceColor == currentTurn) {
                        count += stripArray[i].quantity;
                    }
                }
                return count;
            } else if(currentTurn == Color.BLACK) {
                for (int i = 23; i >= 18; i--) {
                    if(stripArray[i].pieceColor == currentTurn) {
                        count += stripArray[i].quantity;
                    }
                }
                return count;
            }
            return 0;
        }

        static boolean checkBehindBeforeBearOff(int org) {
            if(currentTurn == Color.WHITE) {
                for (int i = org + 1; i < 6; i++) { // Checks to see if there is a piece behind it
                    // Return a false move if there is a piece behind it
                    if(stripArray[i].quantity > 0 && stripArray[i].pieceColor == currentTurn)
                        return false;
                }
                return true;
            } else if(currentTurn == Color.BLACK) {
                for (int i = org - 1; i >= 18; i--) {
                    if(stripArray[i].quantity > 0 && stripArray[i].pieceColor == currentTurn)
                        return false;
                }
                return true;
            }
            return false;
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

            if(validMove(secondMove, 0)) {
                while (count >= 0) {
                    Bar.insert(temp[count]);
                    count--;
                }
                if(tempAdded)
                    stripArray[secondMove.orgStrip].pop(); // Remove the temp piece
                return true;
            } else { // If invalid move, puts the pieces back
                while (count >= 0) {
                    Bar.insert(temp[count]);
                    count--;
                }
                if(tempAdded)
                    stripArray[secondMove.orgStrip].pop(); // Remove the temp piece
            }
            return false;
        }

        // Checks if the individual moves that make up a combined move are valid
        static boolean checkCombinedMove(Move move, int no_of_moves) {
            if(no_of_moves == 2) { // 2 moves in 1
                Move moveA;
                Move moveB;
                if(currentTurn == Color.WHITE) { // Create the 2 possible original moves
                    moveA = new Move(move.orgStrip, move.destStrip + die.getDice2(), currentTurn);
                    moveB = new Move(move.orgStrip, move.destStrip + die.getDice1(), currentTurn);
                } else if(currentTurn == Color.BLACK) {
                    moveA = new Move(0, 0, currentTurn);
                    moveB = new Move(0, 0, currentTurn);
                    moveA.orgStrip = move.orgStrip;
                    moveA.destStrip = move.orgStrip + die.getDice1();
                    moveB.orgStrip = move.orgStrip;
                    moveB.destStrip = move.orgStrip + die.getDice2();
                } else // Shouldn't get here, but to remove warnings. But fair play if it makes it here.
                    return false;

                if(validMove(moveA, 0)) {
                    moveB.orgStrip = moveA.destStrip;
                    moveB.destStrip = move.destStrip;
                    return checkFinalMove(move, moveB);
                } else if(validMove(moveB, 0)) {
                    moveA.orgStrip = moveB.destStrip;
                    moveA.destStrip = move.destStrip;
                    return checkFinalMove(move, moveA);
                }
            } else {
                Move moveA = new Move(move.orgStrip, move.orgStrip + die.getDice1(), currentTurn);
                if(currentTurn == Color.BLACK) {
                    moveA.orgStrip = move.orgStrip;
                    moveA.destStrip = move.orgStrip + die.getDice1();
                }
                if(no_of_moves == 3) { // At this point, each move will move the same amount
                    if(validMove(moveA, 0)) {
                        Move moveB = new Move(moveA.destStrip, move.destStrip, currentTurn); // Combo move of the remaining moves
                        return validMove(moveB, 0);
                    }
                    // No need to test other moves as they are all the same move. 3
                    return false;
                } else if(no_of_moves == 4) {
                    if(validMove(moveA, 0)) {
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
            if(validMove(finalMove, 0)) {
                if(tempAdded)
                    stripArray[finalMove.orgStrip].pop();
                return true;
            }
            if(tempAdded)
                stripArray[finalMove.orgStrip].pop();

            return false;
        }

        static boolean addTempPiece(Move secondMove) {
            // Input a temp piece if empty, for checking if it's a valid move.
            if(stripArray[secondMove.orgStrip].pieceColor == Color.NONE) {
                stripArray[secondMove.orgStrip].insert(new Piece(currentTurn));
                return true;
            } else if(currentTurn == Color.BLACK && stripArray[secondMove.orgStrip].pieceColor == Color.WHITE) {
                stripArray[secondMove.orgStrip].pop();
                stripArray[secondMove.orgStrip].insert(new Piece(currentTurn));
                return true;
            } else if(currentTurn == Color.WHITE && stripArray[secondMove.orgStrip].pieceColor == Color.BLACK) {
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
            if((currentTurn != dest.pieceColor) && dest.quantity == 1) // Destination has only 1 opposing piece so a hit
                hitMove(dest);
        }

        static boolean checkHitMove(int dest) { // returns true or false depending on whether landing on given destStrip is a hit
            return ((currentTurn != stripArray[dest].pieceColor) && stripArray[dest].quantity == 1);
        }

        static boolean valid(Move move, boolean showErrors, boolean isHypothetical) {        //temporary method that takes a move as input and returns whether it's valid or not
            //the reason I made a new one instead of updating validMove() is so that makeMove()
            //(which incorporates validMove) would still reliably be usable even as I tamper with valid()
            int org = move.orgStrip;
            int dest = move.destStrip;
            int displayedDest = (move.color == Color.WHITE) ? (dest + 1) : (23 - dest) + 1;    //yes, math for 23-org+1 could be simplified, but I
            int displayedOrg = (move.color == Color.WHITE) ? (org + 1) : (23 - org) + 1;      //kept it that way so that it's easier to make sense of,
            int diff = displayedOrg - displayedDest;                                   //expressing it as a reverse of previous steps applied to it.

            if(org < 0 || dest < 0 || org > 23 || dest > 23 || diff == 0) {                   // can probably be removed later since it
                if(showErrors)                                                   // will likely only be used in findBoardMoves()
                    System.out.println("Out of bounds");                        // which should naturally stay within those bounds
                return false;
            }
            if(stripArray[org].pieceColor != move.color && !isHypothetical) {                         //maybe also remove?
                if(showErrors)
                    System.out.println("No " + move.color + " pieces on origin strip " + displayedOrg);
                return false;
            }
            if(diff != Board.die.getDice1() && diff != Board.die.getDice2()) {
                if(showErrors)
                    System.out.println("Difference between orgStrip and destStrip is " + diff + ", which was not one of the dice rolls");
                return false;
            }
            if(stripArray[dest].pieceColor != move.color && stripArray[dest].quantity > 1) {
                if(showErrors)
                    System.out.println("destStrip is not able to be landed on, as it has more than one of the opponent's pieces on it");
                return false;
            }
            return true;
        }

        static ArrayList<Move> findAllValidMoves() {
            ArrayList<Move> boardMoves = new ArrayList<>();
            for (int i = -1; i < 24; i++) {
                // Create a new move each time so it's a new move being input into the list. Issues otherwise
                Move move1 = new Move(0, 0, currentTurn);
                Move move2 = new Move(0, 0, currentTurn);
                move1.orgStrip = i;
                move2.orgStrip = i;

                if(currentTurn == Color.WHITE) {
                    if(i == -1) { // Value is bar, white moves from -1 to 24 - dice value
                        move1.destStrip = 24 - die.getDice1();
                        move2.destStrip = 24 - die.getDice2();
                    } else { // Sets the destination to bear-off value if move leaves the board
                        if(i - die.getDice1() < 0)
                            move1.destStrip = -2;
                        else
                            move1.destStrip = i - die.getDice1();

                        if(i - die.getDice2() < 0)
                            move2.destStrip = -2;
                        else
                            move2.destStrip = i - die.getDice2();
                    }
                } else if(currentTurn == Color.BLACK) {
                    if(i + die.getDice1() > 23)
                        move1.destStrip = -2;
                    else
                        move1.destStrip = i + die.getDice1();

                    if(i + die.getDice2() > 23)
                        move2.destStrip = -2;
                    else
                        move2.destStrip = i + die.getDice2();
                }

                if(validMove(move1,-1)){
                    boardMoves.add(move1);
                }
                if(validMove(move2,-1)){
                    boardMoves.add(move2);
                }
            }

            /*for(int i = 0;i < boardMoves.size();i++){
                System.out.println("move " + boardMoves.get(i).isHitToString());
            }*/

            return boardMoves;
        }

        static void removeDuplicateCombos(ArrayList<MoveCombo> combos) {
            ArrayList<MoveCombo> toRemove = new ArrayList<>();

            if(combos.size() > 0 && combos.get(0).numMovesPerCombo == 2) {   //assumes num moves in any combo is same as num moves in all combos
                for (MoveCombo tmp1 : combos) {
                    for (MoveCombo tmp2 : combos) {
                        if(tmp1.moves[0].orgStrip == tmp2.moves[0].orgStrip && tmp1.moves[1].destStrip == tmp2.moves[1].destStrip            // if same starting and end points
                                && !checkHitMove(tmp1.moves[0].destStrip) && !checkHitMove(tmp2.moves[1].destStrip)// and no hits in between
                                && tmp1 != tmp2 && !toRemove.contains(tmp1)) {  //and it's comparing against another combo, not itself, otherwise it would remove everything
                            // and that other combo hasn't already been removed, otherwise it would remove both

                        /*   Can maybe skip these last two conditions by using something like:
                             for(i=0;i<combo.size;i++){
                                 for(j=i;j<combo.size;j++){
                                 }
                             }
                                   emphasis on the j=i
                         */

                            //do we also need check if first's dest == second's org, or is that already implied by the fact that the first's org to second's dest distances will be the same

                            toRemove.add(tmp2);
                        }
                    }
                }
            }

            for (MoveCombo mc1 : combos) {
                for (MoveCombo mc2 : combos) {
                    if(mc1 != mc2 && mc2.equals(mc1) && !toRemove.contains(mc1)) {//mc1 != mc2 as in the pointers aren't equal, as in not the exact same combo
                        toRemove.add(mc2);  //not working?

                        /*for (int i = 0; i < mc1.numMovesPerCombo; i++) {
                            System.out.print(mc2.moves[i] + " ");
                        }
                        System.out.print("was removed for equalling");
                        for (int i = 0; i < mc1.numMovesPerCombo; i++) {
                            System.out.print(mc1.moves[i] + " ");
                        }
                        System.out.println();*/

                    }
                }
            }

            //System.out.println("toRemove: " + toRemove);

            combos.removeAll(toRemove);

            // TODO expand to work for 3-move combos and 4-move combos
            // and catch other kind of duplicates as well
            // 2 other kinds are where it's the same moves in a different order  e.g. "7-4 11-10" and "11-10 7-4"
            // and where, for whatever reason, two plays have been added that are the exact same e.g.  "6-5 4-1" and "6-5 4-1"
            // maybe this second case can be made unnecessary by avoiding adding those exact duplicates in the first place
            // if not, it might also have to be able to be applied to 1-move plays, more observation needed first
        }

        static ArrayList<MoveCombo> findAllValidCombos() {

            ArrayList<Move> allMoves = new ArrayList<>(findAllValidMoves());
            //allMoves.addAll(findBarMoves());

            ArrayList<Move> copyAllMoves;  // made so that when combining moves into pairs (or triples or quadruplets),
            // you can temporarily change which moves are valid without affecting the master copy


            ArrayList<MoveCombo> allCombos = new ArrayList<>();

            //code to combine multiple moves in pairs to print -

            if(maxMoves == 2) {
                for (Move firstMove : allMoves) {
                    copyAllMoves = new ArrayList<>(allMoves);
                    if(firstMove.destStrip == -2) {
                        allCombos.add(new MoveCombo(1, firstMove));
                    } else if(Bar.piecesIn(currentTurn) == 0 || firstMove.orgStrip == -1) {
                        // if statement means that if there are no possible bar moves which
                        // would have taken priority, then the first move of the pair can be anything,
                        // but if there are bar moves possible (i.e. if bar.quantity>0),
                        // then the first move has to be one of those.

                        if((firstMove.orgStrip != -1 || Bar.piecesIn(currentTurn) < 2) && firstMove.destStrip > -1 && stripArray[firstMove.destStrip].pieceColor != currentTurn && valid(new Move(currentTurn == Color.WHITE ? firstMove.destStrip : 23 - firstMove.destStrip, currentTurn == Color.WHITE ? firstMove.destStrip - Board.die.getDice1() : (23 - firstMove.destStrip - Board.die.getDice1()), currentTurn), false, true)) {
                            copyAllMoves.add(new Move(currentTurn == Color.WHITE ? firstMove.destStrip : 23 - firstMove.destStrip, currentTurn == Color.WHITE ? firstMove.destStrip - Board.die.getDice1() : (23 - firstMove.destStrip - Board.die.getDice1()), currentTurn));
                        }
                        // i.e. assuming the first move is made and there is now a piece on destStrip where there wasn't before,
                        // does that produce any new valid moves that weren't available before? Checks both dice1 and dice2.

                        if((firstMove.orgStrip != -1 || Bar.piecesIn(currentTurn) < 2) && firstMove.destStrip > -1 && stripArray[firstMove.destStrip].pieceColor != currentTurn && valid(new Move(currentTurn == Color.WHITE ? firstMove.destStrip : 23 - firstMove.destStrip, currentTurn == Color.WHITE ? firstMove.destStrip - Board.die.getDice2() : (23 - firstMove.destStrip - Board.die.getDice2()), currentTurn), false, true)) {
                            copyAllMoves.add(new Move(currentTurn == Color.WHITE ? firstMove.destStrip : 23 - firstMove.destStrip, currentTurn == Color.WHITE ? firstMove.destStrip - Board.die.getDice2() : (23 - firstMove.destStrip - Board.die.getDice2()), currentTurn));
                        }
                        //Conversely, also need to check if any moves are no longer possible now. This would only happen if there are
                        //no more pieces left on orgStrip after the move is made, i.e. if there is currently only one piece on orgStrip

                        if((firstMove.orgStrip >= 0 && firstMove.orgStrip < 24 && firstMove.destStrip != -2 && stripArray[firstMove.orgStrip].quantity <= 1) || (firstMove.orgStrip == -1 && Bar.piecesIn(currentTurn) <= 1)) {  //<=1 to make testing easier, really could be ==1
                            copyAllMoves.removeIf(m -> m.orgStrip == firstMove.orgStrip);
                        }
                        // >=0 and <24 conditions included so that it doesn't try to dereference something like stripArray[-1] for bar moves

                        allCombos.add(new MoveCombo(1, firstMove));

                        // Finds all combinations of moves after last bar move
                        if(Bar.piecesIn(currentTurn) == 1 && firstMove.orgStrip == -1 && maxMoves - currentMoves == 2) {
                            Piece tempFreedom = new Piece(Bar.remove(currentTurn));
                            int barDist = getMoveDistFromBar(firstMove);
                            int diceReset = 0;
                            // Changing the corresponding dice value so that findAllValidMoves doesn't add moves with the same dice to the list
                            if(barDist == die.getDice1()) {
                                die.setDice1(0);
                                diceReset = 1;
                            } else if(barDist == die.getDice2()) {
                                die.setDice2(0);
                                diceReset = 2;
                            }
                            // Removed the Bar piece, allows findAllValidMoves() to find other normal valid moves
                            ArrayList<Move> temp = findAllValidMoves();
                            for (Move secondMove : temp) {
                                allCombos.add(new MoveCombo(2, firstMove, secondMove));
                            }

                            if(diceReset == 1) // Re-add the value back to the affected dice.
                                die.setDice1(barDist);
                            else if(diceReset == 2) {
                                die.setDice2(barDist);
                            }
                            Bar.insert(tempFreedom); // Re-insert the removed piece from the Bar
                        }
                        // Player White has 1 piece outside of the homeboard, and moves that piece into the homeboard,
                        // opens up bear-off combinations
                        else if((Main.players[currentTurn.getValue()].getPiecesLeft() - 1) == piecesInHomeBoard() && currentTurn == Color.WHITE && firstMove.orgStrip > 5 && firstMove.destStrip <= 5){
                            ArrayList<Move> afterMovetoHomeBoard = combinationAfterLastPieceHome(firstMove);
                            // Making the combos
                            if (maxMoves - currentMoves == 2) {
                                for(Move secondMove : afterMovetoHomeBoard){
                                    if(secondMove.orgStrip != firstMove.orgStrip){
                                        allCombos.add(new MoveCombo(2,firstMove,secondMove));
                                    }
                                }
                            }
                        }else if((Main.players[currentTurn.getValue()].getPiecesLeft() - 1) == piecesInHomeBoard() && currentTurn == Color.BLACK && firstMove.orgStrip < 18 && firstMove.destStrip >= 18){
                            ArrayList<Move> afterMovetoHomeBoard = combinationAfterLastPieceHome(firstMove);
                            // Making the combos
                            if(maxMoves - currentMoves == 2){
                                for(Move secondMove : afterMovetoHomeBoard){
                                    if(secondMove.orgStrip != firstMove.orgStrip){
                                        allCombos.add(new MoveCombo(2,firstMove,secondMove));
                                    }
                                }
                            }
                        }
                        else{
                            if(maxMoves - currentMoves == 2){ // Only adds the second move if a second move is allowed.
                                for (Move secondMove : copyAllMoves){
                                    if (secondMove.orgStrip == -1 || Bar.piecesIn(currentTurn) < 2){
                                        int firstDiff = (firstMove.orgStrip == -1) ? getMoveDistFromBar(firstMove) : (firstMove.destStrip == -2 ? getDiceMoveAtBearOff(firstMove) : getMoveDist(firstMove));
                                        int secondDiff = (secondMove.orgStrip == -1) ? getMoveDistFromBar(secondMove) : (secondMove.destStrip == -2 ? getDiceMoveAtBearOff(secondMove) : getMoveDist(secondMove));

                                        if((firstDiff + secondDiff == Board.die.getDice1() + Board.die.getDice2())) {
                                            allCombos.add(new MoveCombo(2, firstMove, secondMove));
                                        } else {
                                            System.out.println(firstDiff + " and " + secondDiff + " were " + (firstDiff + secondDiff) + " not " + (Board.die.getDice1() + Board.die.getDice2()));
                                            //System.out.println("firstMove " + firstMove + " has orgStrip " + firstMove.orgStrip + " and destStrip " + firstMove.destStrip);
                                            //System.out.println("secondMove " + secondMove + " has orgStrip " + secondMove.orgStrip + " and destStrip " + secondMove.destStrip);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            } else if(maxMoves == 4) { // Doubles
                for (Move firstMove : allMoves) {
                    copyAllMoves = new ArrayList<>(allMoves);
                    if(firstMove.destStrip == -2) {
                        allCombos.add(new MoveCombo(1, firstMove));
                    }
                    if(Bar.piecesIn(currentTurn) == 0 || firstMove.orgStrip == -1) {

                        if((firstMove.orgStrip != -1 || Bar.piecesIn(currentTurn) < 2) && firstMove.destStrip > -1 && stripArray[firstMove.destStrip].pieceColor != currentTurn && valid(new Move(currentTurn == Color.WHITE ? firstMove.destStrip : 23 - firstMove.destStrip, currentTurn == Color.WHITE ? firstMove.destStrip - Board.die.getDice1() : (23 - firstMove.destStrip - Board.die.getDice1()), currentTurn), false, true)) {
                            copyAllMoves.add(new Move(currentTurn == Color.WHITE ? firstMove.destStrip : 23 - firstMove.destStrip, currentTurn == Color.WHITE ? firstMove.destStrip - Board.die.getDice1() : (23 - firstMove.destStrip - Board.die.getDice1()), currentTurn));
                            //System.out.println("hi");
                        }  //Don't need to check again for dice2 since they're the same number

                        if((firstMove.orgStrip >= 0 && firstMove.orgStrip < 24 && firstMove.destStrip != -2 && stripArray[firstMove.orgStrip].quantity < 2) || (firstMove.orgStrip == -1 && Bar.piecesIn(currentTurn) <= 1)) {  //<=1 to make testing easier, really could be ==1
                            copyAllMoves.removeIf(m -> m.orgStrip == firstMove.orgStrip);
                        }

                        allCombos.add(new MoveCombo(1, firstMove));

                        if(maxMoves - currentMoves >= 2){ // Ensures that a combo greater than 1 can be made
                            for (Move secondMove : copyAllMoves) {
                                ArrayList<Move> copyAllMoves2 = new ArrayList<>(copyAllMoves);
                                if(secondMove.orgStrip == -1 || Bar.piecesIn(currentTurn) < 2) {
                                    allCombos.add(new MoveCombo(2, firstMove, secondMove));

                                    if((secondMove.orgStrip != -1 || Bar.piecesIn(currentTurn) < 2) && secondMove.destStrip > -1 && stripArray[secondMove.destStrip].pieceColor != currentTurn && valid(new Move(currentTurn == Color.WHITE ? secondMove.destStrip : 23 - secondMove.destStrip, currentTurn == Color.WHITE ? secondMove.destStrip - Board.die.getDice1() : (23 - secondMove.destStrip - Board.die.getDice1()), currentTurn), false, true)) {
                                        copyAllMoves2.add(new Move(currentTurn == Color.WHITE ? secondMove.destStrip : 23 - secondMove.destStrip, currentTurn == Color.WHITE ? secondMove.destStrip - Board.die.getDice1() : (23 - secondMove.destStrip - Board.die.getDice1()), currentTurn));
                                    }  //Don't need to check again for dice2 since they're the same number

                                    if((secondMove.orgStrip >= 0 && secondMove.orgStrip < 24 && secondMove.destStrip != -2 && stripArray[secondMove.orgStrip].quantity < 3) || (secondMove.orgStrip == -1 && Bar.piecesIn(currentTurn) <= 2)) {  //<=1 to make testing easier, really could be ==1
                                        copyAllMoves2.removeIf(m -> m.orgStrip == secondMove.orgStrip);
                                    }

                                    if(maxMoves - currentMoves >= 3){ // Ensures that a combo greater than 2 can be made
                                        for (Move thirdMove : copyAllMoves2) {
                                            ArrayList<Move> copyAllMoves3 = new ArrayList<>(copyAllMoves2);
                                            if(thirdMove.orgStrip == -1 || Bar.piecesIn(currentTurn) < 3) {
                                                if((thirdMove.orgStrip != -1 || Bar.piecesIn(currentTurn) < 2) && thirdMove.destStrip > -1 && stripArray[thirdMove.destStrip].pieceColor != currentTurn && valid(new Move(currentTurn == Color.WHITE ? thirdMove.destStrip : 23 - thirdMove.destStrip, currentTurn == Color.WHITE ? thirdMove.destStrip - Board.die.getDice1() : (23 - thirdMove.destStrip - Board.die.getDice1()), currentTurn), false, true)) {
                                                    copyAllMoves3.add(new Move(currentTurn == Color.WHITE ? thirdMove.destStrip : 23 - thirdMove.destStrip, currentTurn == Color.WHITE ? thirdMove.destStrip - Board.die.getDice1() : (23 - thirdMove.destStrip - Board.die.getDice1()), currentTurn));
                                                }  //Don't need to check again for dice2 since they're the same number

                                                if((thirdMove.orgStrip >= 0 && thirdMove.orgStrip < 24 && thirdMove.destStrip != -2 && stripArray[thirdMove.orgStrip].quantity < 4) || (thirdMove.orgStrip == -1 && Bar.piecesIn(currentTurn) <= 3)) {
                                                    copyAllMoves3.removeIf(m -> m.orgStrip == thirdMove.orgStrip);
                                                }

                                                allCombos.add(new MoveCombo(3, firstMove, secondMove, thirdMove));

                                                if(maxMoves - currentMoves == 4){ // Ensures that a combo = 4 can be made
                                                    for (Move fourthMove : copyAllMoves3) {
                                                        if(fourthMove.orgStrip == -1 || Bar.piecesIn(currentTurn) < 4) {
                                                            allCombos.add(new MoveCombo(4, firstMove, secondMove, thirdMove, fourthMove));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
            int max =0 ;
            for(MoveCombo mc: allCombos){
                if(mc.numMovesPerCombo > max){
                    max = mc.numMovesPerCombo;
                }
            }
            int finalMax = max;
            allCombos.removeIf(m -> m.numMovesPerCombo< finalMax);  //will uncomment when it first properly finds bar-moves as part of multi-move plays
                                                                      //because at the moment bar-moves are only found in plays shorter than the largest plays
                                                // Update: Needed to uncomment to use removeDuplicateCombos, which expects all combos to be of the same length
                                                // if you need to comment out this, make sure to also comment out that temporarily
            removeDuplicateCombos(allCombos);

            if(max == 1 && Board.die.getDice1() != Board.die.getDice2()){   // if only one-move long plays are found, the ones that use the bigger dice number must be used if possible,
                int smallerNum = min(Board.die.getDice1(), Board.die.getDice2());   // so we remove the ones using the smaller number (when they're not the same number)
                allCombos.removeIf(mc -> ((mc.moves[0].orgStrip == -1) ? getMoveDistFromBar(mc.moves[0]) : getMoveDist(mc.moves[0])) == smallerNum);
            }

            return allCombos;

        }

        static ArrayList<Move> combinationAfterLastPieceHome(Move firstMove){
            Main.players[currentTurn.getValue()].setPiecesLeft(Main.players[currentTurn.getValue()].getPiecesLeft() - 1);
            int diceValue = getMoveDist(firstMove);
            int diceUsed = 0;

            if(diceValue == die.getDice1()){
                diceUsed = 1;
                die.resetDice(1);
            }else if(diceValue == die.getDice2()){
                diceUsed = 2;
                die.resetDice(2);
            }
            ArrayList<Move> afterMovetoHomeBoard = findAllValidMoves();

            // Resetting the original pieces left.
            Main.players[currentTurn.getValue()].setPiecesLeft(Main.players[currentTurn.getValue()].getPiecesLeft() + 1);
            if(diceUsed == 1){
                die.setDice1(diceValue);
            }else if(diceUsed == 2){
                die.setDice2(diceValue);
            }

            return afterMovetoHomeBoard;
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

        static void cheat() {
            clearBoard();

            Piece[] black = new Piece[15];
            Piece[] white = new Piece[15];

            for (int i = 0; i < 15; i++) {
                black[i] = new Piece(Color.BLACK);
                white[i] = new Piece(Color.WHITE);
            }

            stripArray[0].insert(black, 0, 1);
            BearOff.insert(black, 2, 14);
            stripArray[23].insert(white,0,1);
            BearOff.insert(white, 2, 14);

            Main.players[0].setPiecesLeft(2);
            Main.players[1].setPiecesLeft(2);

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
        if(this.color == Color.BLACK) {
            this.orgStrip = 23 - orgStrip;
            this.destStrip = 23 - destStrip;

            if(orgStrip == -1) {
                this.orgStrip = -1;
            }
            if(destStrip == -2)
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

        if(Classes.Board.validMove(this, -1)) {
            return ((orgStrip == -1 || orgStrip == 24) ? "Bar" : ((color == Color.WHITE) ? (orgStrip + 1) : (23 - orgStrip + 1))) +   //might be able to remove || == 24 check
                    "-" +
                    (destStrip == -2 ? "Off" : ((color == Color.WHITE) ? (destStrip + 1) : (23 - destStrip + 1)) +
                            ((Classes.Board.stripArray[destStrip].quantity == 1 && Classes.Board.stripArray[destStrip].pieceColor != color) ? "*" : ""));
        } else
            return "Invalid move";
    }

    String isHitToString() {
        String m = "";

        //boolean tempAdded = false;
        //if (this.orgStrip != -1) {
        //    tempAdded = Classes.Board.addTempPiece(this);
        //}
        //if (!Classes.Board.validMove(this, 0))
        //  m = "this - ";

        //if (tempAdded)
        //    Classes.Board.removeTempPiece(this);
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

    boolean equals(MoveCombo mc) {
        HashMap<Move, Integer> theseMoves = new HashMap<>();
        HashMap<Move, Integer> otherMoves = new HashMap<>();

        //for(MoveCombo tmp1: combos) {
        for (int i = 0; i < numMovesPerCombo; i++) {
            if(theseMoves.containsKey(moves[i])) {
                theseMoves.put(moves[i], theseMoves.get(moves[i]) + 1);
            } else {
                theseMoves.put(moves[i], 1);
            }
        }
        for (int i = 0; i < mc.numMovesPerCombo; i++) {
            if(otherMoves.containsKey(mc.moves[i])) {
                otherMoves.put(mc.moves[i], otherMoves.get(mc.moves[i]) + 1);
            } else {
                otherMoves.put(mc.moves[i], 1);
            }
        }
        //System.out.println(theseMoves);
        //System.out.println(otherMoves);

        return theseMoves.equals(otherMoves);
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
    private int stripID;
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
        if(quantity == 0)
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
        if(this.stripID >= 0 && this.stripID <= 11) {
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
    boolean isBearoff = false;

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
        if(isBearoff)
            v = piece.toBearOff();
        boxes[color].getChildren().add(v);
    }

    Color remove(Color color) {
        int x = color.getValue();
        int len = pieces[x].size();
        if(len == 0)
            return null;
        pieces[x].remove(len - 1);
        boxes[x].getChildren().remove(len - 1);
        return color;
    }

    //Finds the number of pieces in the Bar for COLOR's turn
    int piecesIn(Color color) {
        if(color == Color.WHITE)
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

            if(dice1 > dice2) {
                Classes.Board.currentTurn = players[0].getColor();
            } else if(dice2 > dice1) {
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

    void setDice1(int integer) {
        this.dice1 = integer;
    }

    void setDice2(int integer) {
        this.dice2 = integer;
    }

    void resetDice(int diceNo) {
        if(diceNo == 1)
            this.dice1 = 0;
        else
            this.dice2 = 0;
    }
}