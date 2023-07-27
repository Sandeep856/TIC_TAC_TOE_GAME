package androidsamples.java.tictactoe;

public class OngoingGame {
    public User host;
    public User player;
    public String gameID;

    public OngoingGame() {

    }

    public OngoingGame(User host,User player,String gameID) {
        this.host = host;
        this.player = player;
        this.gameID = gameID;
    }
}
