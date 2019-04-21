
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
        //System.out.println(diffOfBlots());
        return "1";
    }

    // getScore function that will calculate the score of a board state.
    public double getScore(){
        // TODO Add all the score functions to be called here and return the resulting score

        return 0;
    }

    public double diffOfBlots(){
        int myBlots =0, opponentsBlots = 0;

        for (int i = 0; i < 24; i++) {
            if(board.getNumCheckers(me.getId(), i) == 1){
                myBlots++;
            }else if(board.getNumCheckers(opponent.getId(), i) == 1){
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

    public String getDoubleDecision() {
        // Add your code here
        return "n";
    }
}
