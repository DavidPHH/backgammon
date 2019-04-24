public class testOpponent implements BotAPI {

    // The public API of Bot must not change
    // This is ONLY class that you can edit in the program
    // Rename Bot to the name of your team. Use camel case.
    // Bot may not alter the state of the game objects
    // It may only inspect the state of the board and the player objects

    private PlayerAPI me, opponent;
    private BoardAPI board;
    private CubeAPI cube;
    private MatchAPI match;
    private InfoPanelAPI info;

    testOpponent(PlayerAPI me, PlayerAPI opponent, BoardAPI board, CubeAPI cube, MatchAPI match, InfoPanelAPI info) {
        this.me = me;
        this.opponent = opponent;
        this.board = board;
        this.cube = cube;
        this.match = match;
        this.info = info;
    }

    public String getName() {
        return "testOpponent"; // must match the class name
    }

    public String getCommand(Plays possiblePlays) {
        System.out.println("enragedGophers's probability: " + getProbability(board.get()));
        if(match.canDouble(me.getId()) && (cube.getValue() == 1 || cube.getOwnerId() == me.getId())){ // Checks to see if the bot has access to double
            if(opponent.getScore() == match.getLength() - 1) // If the opponent is one game away from taking the match, always double. Nothing to lose.
                return "double";
            else if(getProbability(board.get()) >= 55) // If there is a greater than 66% chance of winning, double
                return "double";
        }

        int[][] tempBoard = board.get();
        double probability = 0;
        int indexOfPlay = 0;
        for(int i = 0;i < possiblePlays.number();i++){
            for(int j = 0;j < possiblePlays.get(i).numberOfMoves();j++){ // Perform the play on the temp board.
                tempBoard[me.getId()][possiblePlays.get(i).getMove(j).getFromPip()]--;
                tempBoard[me.getId()][possiblePlays.get(i).getMove(j).getToPip()]++;
            }

            double tempScore = getProbability(tempBoard);
            if(tempScore > probability){
                probability = tempScore;
            }
            tempBoard = board.get();
            indexOfPlay = i;
        }
        return Integer.toString(indexOfPlay + 1); // Plays are listed starting from 1. So index 0 will be play 1.
    }

    // getScore function that will calculate the score of a board state.
    private double getProbability(int[][] board){
        double diffHomeBoard = diffInHomeBoard(board);
        double diffPips = relativePipDiff(board);
        double primeScore = scorePrime(board);

        // Coefficients
        double cBlocks = 0.17;
        double cBlots = 0.11;
        double cHBoard = 0.12;
        double cPips = 0.13;
        double cBornOff = 0.15;
        double cBar = 0.2;
        double cSpreadOfBlocksHB = 0.1;
        double cPrime = 0.02;

        if(pieceInFrontOfMyFurthest(board)) {
            cBlocks = 0;
            cBlots = 0;
            cBar = 0;
            cSpreadOfBlocksHB = 0;
            cPrime = 0;

            cHBoard = 0.4;
            cPips = 0.2;
            cBornOff = 0.4;
        }

        return cBlocks*diffOfBlocks(board) + cBlots*diffOfBlots(board) + cHBoard*diffHomeBoard + cPips*diffPips +
                cBornOff*piecesBornOff(board) + cBar*diffInBar(board) +
                cSpreadOfBlocksHB*diffSpreadOfBlocksInHomeBoard(board) + cPrime * primeScore;
    }

    private double diffOfBlots(int[][] board){
        int myBlots =0, opponentsBlots = 0;

        for (int i = 1; i < 25; i++) {
            if(board[me.getId()][i] == 1){
                myBlots++;
            }
            if(board[opponent.getId()][i] == 1){
                opponentsBlots++;
            }
        }

        // Because blots are a weakness, we start by saying that the more blots you have over your opponent,
        // the worse that is for you, meaning it should result in a lower numerical score

        double score = opponentsBlots - myBlots;

        // Now we need to normalise the returned range to 0-100;
        // in theory, the lowest that score could ever be is -15, when the opponent has the minimum number of blots, 0,
        // and you have the maximum, 15. Likewise the highest it could ever be is +15 for the reverse scenario.

        // So first we multiply score by 10/3 to stretch that max range of -15 to +15 to a new range of -50 to +50,
        // then add 50 to it so it's now 0-100;

        score = (10/3.0)*score + 50;

        return score;
    }

    private double diffOfBlocks(int[][] board){
        int myBlocks =0, opponentsBlocks = 0;

        for (int i = 1; i < 25; i++) {
            if(board[me.getId()][i] >= 2){
                myBlocks++;
            }
            if(board[opponent.getId()][i] >= 2){
                opponentsBlocks++;
            }
        }

        // opposite of diffOfBlots in that this time the more the better, so subtraction order is reversed

        double score = myBlocks - opponentsBlocks;

        // Now we need to normalise the returned range to 0-100;
        // in theory, the lowest that score could ever be is -7, when the opponent has the maximum number of blots, 7,
        // and you have the minimum, 0. Likewise the highest it could ever be is +7 for the reverse scenario.

        // So first we multiply score by 50/7 to stretch that max range of -7 to +7 to a new range of -50 to +50,
        // then add 50 to it so it's now 0-100;

        score = (50/7.0)*score + 50;

        return score;
    }

    private double diffInBar(int[][] board){
        int piecesInMyBar = board[me.getId()][25], piecesInOpponentsBar = board[opponent.getId()][25];

        return ((piecesInOpponentsBar - piecesInMyBar) * (10.0/3.0)) + 50;
    }

    private double diffInHomeBoard(int[][] board){
        int pipsInMyHomeBoard = 0, pipsInOpponentsHomeBoard = 0;

        for(int i = 0;i <= 6;i++){ // Get all the pieces in my home board.
            pipsInMyHomeBoard += board[me.getId()][i];
        }

        for(int i = 0;i <= 6;i++){ // Get all the pieces in the opponents home board.
            pipsInOpponentsHomeBoard += board[opponent.getId()][i];
        }

        /* Max amount of pieces possible in both home board is 15.
           Max score range is +/- 15. ( All pieces in one home board, none for the other player).
           Multiply by 10/3 then add 50 to create a normalised range from 0-100.
        */
        return ((pipsInMyHomeBoard - pipsInOpponentsHomeBoard)*(10/3.0)) + 50;
    }

    private double piecesBornOff(int[][] board){
        return (((board[me.getId()][0]) - ((board[opponent.getId()][0])) * (10/3.0) + 50));
    }

    private double diffSpreadOfBlocksInHomeBoard(int[][] board){
        int blocksInMyHomeBoard = 0;
        int blocksInOpponentsHomeBoard = 0;

        for(int i = 1;i <= 6;i++){
            if(board[me.getId()][i] > 1){
                blocksInMyHomeBoard++;
            }
        }

        for(int i = 1;i <= 6;i++){
            if(board[opponent.getId()][i] > 1){
                blocksInOpponentsHomeBoard++;
            }
        }

        int score = blocksInMyHomeBoard - blocksInOpponentsHomeBoard;

        return ((50.0/6.0) * score) + 50;
    }

    private int countPips(int[][] board, int id){
        int count = 0;
        for (int i = 24; i > 0; i--) {
            count += board[id][i] * i;
        }
        return count;
    }
    private double relativePipDiff(int[][] board){
        int myPips = countPips(board, me.getId());
        int oppPips = countPips(board, opponent.getId());

        int min = -375;
        int max = 375;
        double score = (oppPips - myPips);
        score = (score - min) / (max - min); // Normalizes the score
        return score * 100;
    }

    private double scorePrime(int[][] board){
        int count = 0;
        int max_prime = 0;
        for (int i = 1; i <= 24; i++) {
            if(board[me.getId()][i] > 1)
                count += 1;
            else{
                if (count > max_prime)
                    max_prime = count;
                count = 0;
            }
        }
        if (max_prime >= 6)
            return 100;
        else{
            return (max_prime / 6.0 ) * 100;
        }
    }

    private boolean pieceInFrontOfMyFurthest(int[][] board){
        int indexOfFurthestPiece = 0;

        for(int i = board[me.getId()].length - 1;i >= 0;i--){
            if(board[me.getId()][i] > 0){
                indexOfFurthestPiece = i;
                break;
            }
        }

        if(indexOfFurthestPiece != 0){ // Ensures that the furthest piece found wasn't in bear-off. (It should never be)
            for(int i = board[opponent.getId()].length - 1;i >= indexOfFurthestPiece;i--){
                if(board[opponent.getId()][i] > 0){
                    return true;
                }
            }
        }

        return false;
    }

    public String getDoubleDecision() {
        // Add your code here
        if(me.getScore() == match.getLength() - 1){ // Post Crawford rule, the opponent should in theory be doubling, this bot should always accept it.
            return "y";
        }else if(me.getScore() == match.getLength() - 2 && opponent.getScore() == match.getLength() - 2){ // If both players are 2 points away, different risk assessment then usual
            if(getProbability(board.get()) > 25)
                return "y";
            else
                return "n";
        }else{
            if(getProbability(board.get())  > 40) // If bot has greater than 40% chance of winning, then accept the double offer.
                return "y";
        }
        return "n";
    }
}
