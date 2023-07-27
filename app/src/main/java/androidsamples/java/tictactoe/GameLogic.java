package androidsamples.java.tictactoe;

import android.util.Log;

import androidx.core.util.Pair;

import java.util.Random;
import java.util.Vector;

public class GameLogic {
    private String[][] board;
    private String compPiece;

    public GameLogic() {
        board = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";
            }
        }
    }

    public void setCompPiece(String s) {
        compPiece = s;
    }

    public void setBoard(int x,String piece) {
        switch(x) {
            case 0:
                board[0][0] = piece;
                break;
            case 1:
                board[0][1] = piece;
                break;
            case 2:
                board[0][2] = piece;
                break;
            case 3:
                board[1][0] = piece;
                break;
            case 4:
                board[1][1] = piece;
                break;
            case 5:
                board[1][2] = piece;
                break;
            case 6:
                board[2][0] = piece;
                break;
            case 7:
                board[2][1] = piece;
                break;
            case 8:
                board[2][2] = piece;
                break;
        }
    }

    public int playGame() {
        Vector<Pair<Integer,Integer>> v = new Vector<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == "") {
                    Pair<Integer,Integer> p = new Pair<>(i,j);
                    v.add(p);
                }
            }
        }
        Random random = new Random();
        int size = v.size();
        int x = 9;
        if (size != 0) {
            int index = random.nextInt(v.size());
            Pair<Integer, Integer> pair = v.get(index);
            int a = pair.first;
            int b = pair.second;
            board[a][b] = compPiece;
            if (a == 0 && b == 0) {
                x = 0;
            } else if (a == 0 && b == 1) {
                x = 1;
            } else if (a == 0 && b == 2) {
                x = 2;
            } else if (a == 1 && b == 0) {
                x = 3;
            } else if (a == 1 && b == 1) {
                x = 4;
            } else if (a == 1 && b == 2) {
                x = 5;
            } else if (a == 2 && b == 0) {
                x = 6;
            } else if (a == 2 && b == 1) {
                x = 7;
            } else if (a == 2 && b == 2) {
                x = 8;
            }
        }
        return x;
    }

    public boolean checkXWin() {
        boolean a = (board[0][0] == "X" && board[0][1] == "X" && board[0][2] == "X");
        boolean b = (board[1][0] == "X" && board[1][1] == "X" && board[1][2] == "X");
        boolean c = (board[2][0] == "X" && board[2][1] == "X" && board[2][2] == "X");
        boolean d = (board[0][0] == "X" && board[1][0] == "X" && board[2][0] == "X");
        boolean e = (board[0][1] == "X" && board[1][1] == "X" && board[2][1] == "X");
        boolean f = (board[0][2] == "X" && board[1][2] == "X" && board[2][2] == "X");
        boolean g = (board[0][0] == "X" && board[1][1] == "X" && board[2][2] == "X");
        boolean h = (board[0][2] == "X" && board[1][1] == "X" && board[2][0] == "X");
        if (a || b || c || d || e || f || g || h) {
            return true;
        }
        return false;
    }

    public boolean checkOWin() {
        boolean a = (board[0][0] == "O" && board[0][1] == "O" && board[0][2] == "O");
        boolean b = (board[1][0] == "O" && board[1][1] == "O" && board[1][2] == "O");
        boolean c = (board[2][0] == "O" && board[2][1] == "O" && board[2][2] == "O");
        boolean d = (board[0][0] == "O" && board[1][0] == "O" && board[2][0] == "O");
        boolean e = (board[0][1] == "O" && board[1][1] == "O" && board[2][1] == "O");
        boolean f = (board[0][2] == "O" && board[1][2] == "O" && board[2][2] == "O");
        boolean g = (board[0][0] == "O" && board[1][1] == "O" && board[2][2] == "O");
        boolean h = (board[0][2] == "O" && board[1][1] == "O" && board[2][0] == "O");
        if (a || b || c || d || e || f || g || h) {
            return true;
        }
        return false;
    }
}
