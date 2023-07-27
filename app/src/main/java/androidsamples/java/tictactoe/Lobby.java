package androidsamples.java.tictactoe;

import java.util.UUID;

public class Lobby {
    public User host;
    public String gameID;

    public Lobby() {

    }

    public Lobby(User host) {
        this.host = host;
        gameID = UUID.randomUUID().toString();
    }

    public OngoingGame ongoingConvert(User player) {
        return new OngoingGame(host,player,gameID);
    }
}
