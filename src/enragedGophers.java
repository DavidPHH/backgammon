
public class enragedGophers implements BotAPI {

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

    enragedGophers(PlayerAPI me, PlayerAPI opponent, BoardAPI board, CubeAPI cube, MatchAPI match, InfoPanelAPI info) {
        this.me = me;
        this.opponent = opponent;
        this.board = board;
        this.cube = cube;
        this.match = match;
        this.info = info;
    }

    public String getName() {
        return "enragedGophers"; // must match the class name
    }

    public String getCommand(Plays possiblePlays) {
        // Add your code here
        if(match.canDouble(me.getId()) && (cube.isOwned() || cube.getValue() == 1)){ // Checks to see if the bot has access to double
            if(opponent.getScore() == match.getLength() - 1) // If the opponent is one game away from taking the match, always double. Nothing to lose.
                return "double";
            else if(getProbability(board.get()) >= 66) // If there is a greater than 66% chance of winning, double
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
        return Integer.toString(indexOfPlay+1); // Plays are listed starting from 1. So index 0 will be play 1.
    }

    // getScore function that will calculate the score of a board state.
    private double getProbability(int[][] board){
        // TODO Add all the score functions to be called here and return the resulting score
        double diffHomeBoard = diffInHomeBoard(board);
        double diffPips = relativePipDiff(board);

        // Coefficients
        double cBlocks = 0.35;
        double cBlots = 0.35;
        double cHBoard = 0.3;
        double cPips = 0.3;
        double cBornOff;

        return cBlocks*diffOfBlocks() + cBlots*diffOfBlots() + cHBoard*diffHomeBoard + cPips*diffPips + piecesBornOff(board);
    }

    private double diffOfBlots(){
        int myBlots =0, opponentsBlots = 0;

        for (int i = 1; i < 25; i++) {
            if(board.getNumCheckers(me.getId(), i) == 1){
                myBlots++;
            }
            if(board.getNumCheckers(opponent.getId(), i) == 1){
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

    private double diffOfBlocks(){
        int myBlocks =0, opponentsBlocks = 0;

        for (int i = 1; i < 25; i++) {
            if(board.getNumCheckers(me.getId(), i) >= 2){
                myBlocks++;
            }
            if(board.getNumCheckers(opponent.getId(), i) >= 2){
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

        int min = -167;
        int max = 167;
        double score = (oppPips - myPips);
        score = (score - min) / (max - min); // Normalizes the score
        return score * 100;
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
            if(getProbability(board.get())  > 25) // If bot has greater than 25% chance of winning, then allow the opposition to double.
                return "y";
        }
        return "n";
    }
}
