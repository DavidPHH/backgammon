package Backgammon;
/*
Player class not important for sprint 1, will be used later for additional functionality such as saving game states
*/
public class Player{
    private String playerName;
    private int pipsLeft;

    public Player(String player){
        this.playerName = player;
        pipsLeft = 167;
    }

    public void setPlayerName(String name){
        this.playerName = name;
    }
    public String getPlayerName(){
        return this.playerName;
    }
    public void setPipsLeft(int newPipNumber){
        pipsLeft = newPipNumber;
    }
    public int getPipsLeft(){
        return this.pipsLeft;
    }
}
