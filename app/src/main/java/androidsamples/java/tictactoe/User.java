package androidsamples.java.tictactoe;

public class User {
    public String username;
    public int wins;
    public int losses;
    public int draws;

    public User() {

    }

    public User(String username) {
        this.username = username;
        wins = 0;
        losses = 0;
        draws = 0;
    }
}
