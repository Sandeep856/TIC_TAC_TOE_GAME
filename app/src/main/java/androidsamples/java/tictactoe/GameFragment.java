package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;

public class GameFragment extends Fragment {
  private static final String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;
  private String gameType;
  private String pieceType1;
  private String pieceType2;
  private Button[] mButtons = new Button[GRID_SIZE];
  private NavController mNavController;
  private FirebaseDatabase firebaseDatabase;
  private FirebaseAuth mAuth;
  private FirebaseUser user;
  private DatabaseReference mDatabase;
  private DatabaseReference lobbies;
  private DatabaseReference runningGame;
  private DatabaseReference board;
  private DatabaseReference players;
  private DatabaseReference choice;
  private DatabaseReference turn;
  private DatabaseReference win;
  private DatabaseReference lose;
  private DatabaseReference draw;
  private String playerID;
  private String s;
  private Lobby lobby;
  private OngoingGame game;
  private String gameID;
  private GameLogic gl;
  private int position;
  private TextView pTurn;
  private String userName;
  private User player;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
    gl = new GameLogic();
    firebaseDatabase = FirebaseDatabase.getInstance("https://tic-tac-toe-bfe07-default-rtdb.firebaseio.com/");
    players = firebaseDatabase.getReference("OnlinePlayers");
    mDatabase = firebaseDatabase.getReference("Players");
    lobbies = firebaseDatabase.getReference("Lobbies");
    runningGame = firebaseDatabase.getReference("RunningGame");
    choice = firebaseDatabase.getReference("Choice");
    board = firebaseDatabase.getReference("Board");
    turn = firebaseDatabase.getReference("Turn");
    win = firebaseDatabase.getReference("Win");
    lose = firebaseDatabase.getReference("Lose");
    draw = firebaseDatabase.getReference("Draw");
    mAuth = FirebaseAuth.getInstance();
    user = mAuth.getCurrentUser();
    String email = user.getEmail();
    int j = 0;
    for (; j < email.length(); j++) {
      if (email.charAt(j) == '@') {
        break;
      }
    }
    userName = email.substring(0,j);
    MainActivity.host = new User(userName);
    players.child(userName).setValue(MainActivity.host);
    // Extract the argument passed with the action in a type-safe way
    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
    if (args.getGameType().equals("One-Player") || args.getGameType().equals("Two-Player")) {
      Log.d(TAG, "New game type = " + args.getGameType());
      gameType = args.getGameType();
    }
    else {
      String s = args.getGameType();
      int i = 0;
      for (; i < s.length(); i++) {
        if (s.charAt(i) == ' ') {
          break;
        }
      }
      gameID = s.substring(0,i);
      gameType = s.substring(0,i);
      playerID = s.substring(i+1,s.length());
    }

    // Handle the back press by adding a confirmation dialog
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        Log.d(TAG, "Back pressed");

        // TODO show dialog only when the game is still in progress
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.confirm)
                .setMessage(R.string.forfeit_game_dialog_message)
                .setPositiveButton(R.string.yes, (d, which) -> {
                  // TODO update loss count
                  MainActivity.host.losses++;
                  players.child(MainActivity.host.username).setValue(MainActivity.host);
                  mDatabase.removeValue();
                  lobbies.removeValue();
                  runningGame.removeValue();
                  choice.removeValue();
                  board.removeValue();
                  turn.removeValue();
                  win.removeValue();
                  lose.removeValue();
                  draw.removeValue();
                  d.dismiss();
                })
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();
        dialog.show();
        NavDirections action = GameFragmentDirections.actionGameFragmentToDashboardFragment();
        mNavController.navigate(action);
      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mNavController = Navigation.findNavController(view);
    pTurn = view.findViewById(R.id.playerTurn);
    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);
    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);
    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);
    if (gameType.equals("One-Player")) {
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
              .setTitle("Piece Type")
              .setMessage("Please choose X or O")
              .setPositiveButton("Choose X", (d, which) -> {
                pieceType1 = "X";
                pieceType2 = "O";
                gl.setCompPiece(pieceType2);
              })
              .setNegativeButton("Choose O", (d, which) -> {
                pieceType1 = "O";
                pieceType2 = "X";
                gl.setCompPiece(pieceType2);
              })
              .create();
      dialog.show();
      for (int i = 0; i < mButtons.length; i++) {
        final int finalI = i;
        mButtons[i].setOnClickListener(v -> {
          Log.d(TAG, "Button " + finalI + " clicked");
          // TODO implement listeners
          if (gameType.equals("One-Player")) {
            mButtons[finalI].setText(pieceType1);
            mButtons[finalI].setEnabled(false);
            gl.setBoard(finalI, pieceType1);
            if (checkWin() == 0) {
              NavDirections action = GameFragmentDirections.actionGameFragmentToDashboardFragment();
              mNavController.navigate(action);
              return;
            }
            try {
              position = gl.playGame();
              mButtons[position].setText(pieceType2);
              mButtons[position].setEnabled(false);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            if (checkWin() == 0) {
              NavDirections action = GameFragmentDirections.actionGameFragmentToDashboardFragment();
              mNavController.navigate(action);
            }
          }
        });
      }
    }
    else if (gameType.equals("Two-Player")) {
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
              .setTitle("Two Player Game")
              .setMessage("Please choose to create or join")
              .setPositiveButton("Create", (d, which) -> {
                choice.child(MainActivity.host.username).setValue("c");
              })
              .setNegativeButton("Join", (d, which) -> {
                choice.child(MainActivity.host.username).setValue("j");
              })
              .create();
      dialog.show();

      choice.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
          if (snapshot.getKey().equals(MainActivity.host.username)) {
            s = snapshot.getValue(String.class);
            if (s.equals("c")) {
              ProgressDialog pd = new ProgressDialog(requireContext());
              pd.setMessage("Getting Opponent...");
              pd.show();
              lobby = new Lobby(MainActivity.host);
              lobbies.child(lobby.gameID).setValue(lobby);
              mDatabase.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot1, @Nullable String previousChildName) {
                  player = snapshot1.getValue(User.class);
                  game = lobby.ongoingConvert(player);
                  lobbies.child(lobby.gameID).removeValue();
                  runningGame.child(game.gameID).setValue(game);
                  turn.child(game.gameID).setValue("");
                  board.child(game.gameID).child("0,0").setValue("");
                  board.child(game.gameID).child("0,1").setValue("");
                  board.child(game.gameID).child("0,2").setValue("");
                  board.child(game.gameID).child("1,0").setValue("");
                  board.child(game.gameID).child("1,1").setValue("");
                  board.child(game.gameID).child("1,2").setValue("");
                  board.child(game.gameID).child("2,0").setValue("");
                  board.child(game.gameID).child("2,1").setValue("");
                  board.child(game.gameID).child("2,2").setValue("");
                  turn.child(game.gameID).setValue(MainActivity.host.username);
                  pTurn.setText(MainActivity.host.username+"'s turn");
                  pd.dismiss();
                  for (int i = 0; i < 9; i++) {
                    final int pos = i;
                    mButtons[i].setOnClickListener(v -> {
                      Log.d(TAG, "Button " + pos + " clicked");
                      turn.child(game.gameID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot3) {
                          int z = 0;
                          if (snapshot3.getValue(String.class).equals(MainActivity.host.username)) {
                            if (gameType.equals("Two-Player")) {
                              mButtons[pos].setText("X");
                              mButtons[pos].setEnabled(false);
                              switch (pos) {
                                case 0:
                                  board.child(game.gameID).child("0,0").setValue("X");
                                  turn.child(game.gameID).setValue(player.username);
                                  break;
                                case 1:
                                  board.child(game.gameID).child("0,1").setValue("X");
                                  turn.child(game.gameID).setValue(player.username);
                                  break;
                                case 2:
                                  board.child(game.gameID).child("0,2").setValue("X");
                                  turn.child(game.gameID).setValue(player.username);
                                  break;
                                case 3:
                                  board.child(game.gameID).child("1,0").setValue("X");
                                  turn.child(game.gameID).setValue(player.username);
                                  break;
                                case 4:
                                  board.child(game.gameID).child("1,1").setValue("X");
                                  turn.child(game.gameID).setValue(player.username);
                                  break;
                                case 5:
                                  board.child(game.gameID).child("1,2").setValue("X");
                                  turn.child(game.gameID).setValue(player.username);
                                  break;
                                case 6:
                                  board.child(game.gameID).child("2,0").setValue("X");
                                  turn.child(game.gameID).setValue(player.username);
                                  break;
                                case 7:
                                  board.child(game.gameID).child("2,1").setValue("X");
                                  turn.child(game.gameID).setValue(player.username);
                                  break;
                                case 8:
                                  board.child(game.gameID).child("2,2").setValue("X");
                                  turn.child(game.gameID).setValue(player.username);
                                  break;
                              }
                              String s1 = mButtons[0].getText().toString();
                              String s2 = mButtons[1].getText().toString();
                              String s3 = mButtons[2].getText().toString();
                              String s4 = mButtons[3].getText().toString();
                              String s5 = mButtons[4].getText().toString();
                              String s6 = mButtons[5].getText().toString();
                              String s7 = mButtons[6].getText().toString();
                              String s8 = mButtons[7].getText().toString();
                              String s9 = mButtons[8].getText().toString();
                              boolean b1 = (s1.equals("X") && s2.equals("X") && s3.equals("X"));
                              boolean b2 = (s4.equals("X") && s5.equals("X") && s6.equals("X"));
                              boolean b3 = (s7.equals("X") && s8.equals("X") && s9.equals("X"));
                              boolean b4 = (s1.equals("X") && s4.equals("X") && s7.equals("X"));
                              boolean b5 = (s2.equals("X") && s5.equals("X") && s8.equals("X"));
                              boolean b6 = (s3.equals("X") && s6.equals("X") && s9.equals("X"));
                              boolean b7 = (s1.equals("X") && s5.equals("X") && s9.equals("X"));
                              boolean b8 = (s3.equals("X") && s5.equals("X") && s7.equals("X"));
                              boolean a1 = (s1.equals("O") && s2.equals("O") && s3.equals("O"));
                              boolean a2 = (s4.equals("O") && s5.equals("O") && s6.equals("O"));
                              boolean a3 = (s7.equals("O") && s8.equals("O") && s9.equals("O"));
                              boolean a4 = (s1.equals("O") && s4.equals("O") && s7.equals("O"));
                              boolean a5 = (s2.equals("O") && s5.equals("O") && s8.equals("O"));
                              boolean a6 = (s3.equals("O") && s6.equals("O") && s9.equals("O"));
                              boolean a7 = (s1.equals("O") && s5.equals("O") && s9.equals("O"));
                              boolean a8 = (s3.equals("O") && s5.equals("O") && s7.equals("O"));
                              boolean d1 = (!s1.equals("") && !s2.equals("") && !s3.equals("") && !s4.equals("") && !s5.equals("") && !s6.equals("") && !s7.equals("") && !s8.equals("") && !s9.equals(""));
                              if (b1 || b2 || b3 || b4 || b5 || b6 || b7 || b8) {
                                win.child(game.gameID).setValue(MainActivity.host.username);
                                lose.child(game.gameID).setValue(player.username);
                                MainActivity.host.wins++;
                                board.child(game.gameID).removeValue();
                                choice.child(MainActivity.host.username).removeValue();
                                mDatabase.child(game.gameID).removeValue();
                                runningGame.child(game.gameID).removeValue();
                                turn.child(game.gameID).removeValue();
                                players.child(MainActivity.host.username).setValue(MainActivity.host);
                                z = displaywin();
                              }
                              else if (a1 || a2 || a3 || a4 || a5 || a6 || a7 || a8) {
                                win.child(game.gameID).setValue(player.username);
                                lose.child(game.gameID).setValue(MainActivity.host.username);
                                MainActivity.host.losses++;
                                board.child(game.gameID).removeValue();
                                choice.child(MainActivity.host.username).removeValue();
                                mDatabase.child(game.gameID).removeValue();
                                runningGame.child(game.gameID).removeValue();
                                turn.child(game.gameID).removeValue();
                                players.child(MainActivity.host.username).setValue(MainActivity.host);
                                z = displayloss();
                              }
                              else if (d1) {
                                draw.child(game.gameID).setValue(game.gameID);
                                MainActivity.host.draws++;
                                board.child(game.gameID).removeValue();
                                choice.child(MainActivity.host.username).removeValue();
                                mDatabase.child(game.gameID).removeValue();
                                runningGame.child(game.gameID).removeValue();
                                turn.child(game.gameID).removeValue();
                                players.child(MainActivity.host.username).setValue(MainActivity.host);
                                z = displaydraw();
                              }
                            }
                          }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                          Log.d("Error",error.getMessage());
                        }
                      });
                    });
                  }
                  board.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot2, @Nullable String previousChildName) {
                      int z = 0;
                      for (DataSnapshot dataSnapshot : snapshot2.getChildren()) {
                        if (dataSnapshot.getValue(String.class).equals("O")) {
                          switch (dataSnapshot.getKey()) {
                            case "0,0":
                              mButtons[0].setText("O");
                              mButtons[0].setEnabled(false);
                              break;
                            case "0,1":
                              mButtons[1].setText("O");
                              mButtons[1].setEnabled(false);
                              break;
                            case "0,2":
                              mButtons[2].setText("O");
                              mButtons[2].setEnabled(false);
                              break;
                            case "1,0":
                              mButtons[3].setText("O");
                              mButtons[3].setEnabled(false);
                              break;
                            case "1,1":
                              mButtons[4].setText("O");
                              mButtons[4].setEnabled(false);
                              break;
                            case "1,2":
                              mButtons[5].setText("O");
                              mButtons[5].setEnabled(false);
                              break;
                            case "2,0":
                              mButtons[6].setText("O");
                              mButtons[6].setEnabled(false);
                              break;
                            case "2,1":
                              mButtons[7].setText("O");
                              mButtons[7].setEnabled(false);
                              break;
                            case "2,2":
                              mButtons[8].setText("O");
                              mButtons[8].setEnabled(false);
                              break;
                          }
                          String s1 = mButtons[0].getText().toString();
                          String s2 = mButtons[1].getText().toString();
                          String s3 = mButtons[2].getText().toString();
                          String s4 = mButtons[3].getText().toString();
                          String s5 = mButtons[4].getText().toString();
                          String s6 = mButtons[5].getText().toString();
                          String s7 = mButtons[6].getText().toString();
                          String s8 = mButtons[7].getText().toString();
                          String s9 = mButtons[8].getText().toString();
                          boolean b1 = (s1.equals("X") && s2.equals("X") && s3.equals("X"));
                          boolean b2 = (s4.equals("X") && s5.equals("X") && s6.equals("X"));
                          boolean b3 = (s7.equals("X") && s8.equals("X") && s9.equals("X"));
                          boolean b4 = (s1.equals("X") && s4.equals("X") && s7.equals("X"));
                          boolean b5 = (s2.equals("X") && s5.equals("X") && s8.equals("X"));
                          boolean b6 = (s3.equals("X") && s6.equals("X") && s9.equals("X"));
                          boolean b7 = (s1.equals("X") && s5.equals("X") && s9.equals("X"));
                          boolean b8 = (s3.equals("X") && s5.equals("X") && s7.equals("X"));
                          boolean a1 = (s1.equals("O") && s2.equals("O") && s3.equals("O"));
                          boolean a2 = (s4.equals("O") && s5.equals("O") && s6.equals("O"));
                          boolean a3 = (s7.equals("O") && s8.equals("O") && s9.equals("O"));
                          boolean a4 = (s1.equals("O") && s4.equals("O") && s7.equals("O"));
                          boolean a5 = (s2.equals("O") && s5.equals("O") && s8.equals("O"));
                          boolean a6 = (s3.equals("O") && s6.equals("O") && s9.equals("O"));
                          boolean a7 = (s1.equals("O") && s5.equals("O") && s9.equals("O"));
                          boolean a8 = (s3.equals("O") && s5.equals("O") && s7.equals("O"));
                          boolean d1 = (!s1.equals("") && !s2.equals("") && !s3.equals("") && !s4.equals("") && !s5.equals("") && !s6.equals("") && !s7.equals("") && !s8.equals("") && !s9.equals(""));
                          if (b1 || b2 || b3 || b4 || b5 || b6 || b7 || b8) {
                            win.child(game.gameID).setValue(MainActivity.host.username);
                            lose.child(game.gameID).setValue(player.username);
                            MainActivity.host.wins++;
                            board.child(game.gameID).removeValue();
                            choice.child(MainActivity.host.username).removeValue();
                            mDatabase.child(game.gameID).removeValue();
                            runningGame.child(game.gameID).removeValue();
                            turn.child(game.gameID).removeValue();
                            players.child(MainActivity.host.username).setValue(MainActivity.host);
                            z = displaywin();
                          }
                          else if (a1 || a2 || a3 || a4 || a5 || a6 || a7 || a8) {
                            win.child(game.gameID).setValue(player.username);
                            lose.child(game.gameID).setValue(MainActivity.host.username);
                            MainActivity.host.losses++;
                            board.child(game.gameID).removeValue();
                            choice.child(MainActivity.host.username).removeValue();
                            mDatabase.child(game.gameID).removeValue();
                            runningGame.child(game.gameID).removeValue();
                            turn.child(game.gameID).removeValue();
                            players.child(MainActivity.host.username).setValue(MainActivity.host);
                            z = displayloss();
                          }
                          else if (d1) {
                            draw.child(game.gameID).setValue(game.gameID);
                            MainActivity.host.draws++;
                            board.child(game.gameID).removeValue();
                            choice.child(MainActivity.host.username).removeValue();
                            mDatabase.child(game.gameID).removeValue();
                            runningGame.child(game.gameID).removeValue();
                            turn.child(game.gameID).removeValue();
                            players.child(MainActivity.host.username).setValue(MainActivity.host);
                            z = displaydraw();
                          }
                        }
                      }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                      Log.d("Error",error.getMessage());
                    }
                  });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                  Log.d("Error",error.getMessage());
                }
              });
            }
            else if (s.equals("j")) {
              NavDirections action = GameFragmentDirections.actionGameFragmentToDashboardFragment();
              Navigation.findNavController(view).navigate(action);
              return;
            }
          }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
          Log.d("Error",error.getMessage());
        }
      });
    }
    else {
      mDatabase.child(gameID).setValue(MainActivity.host);
      pTurn.setText(playerID+"'s turn");
      board.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
          for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            if (dataSnapshot.getValue(String.class).equals("X")) {
              pTurn.setText(MainActivity.host.username+"'s turn");
              switch (dataSnapshot.getKey()) {
                case "0,0":
                  mButtons[0].setText("X");
                  mButtons[0].setEnabled(false);
                  break;
                case "0,1":
                  mButtons[1].setText("X");
                  mButtons[1].setEnabled(false);
                  break;
                case "0,2":
                  mButtons[2].setText("X");
                  mButtons[2].setEnabled(false);
                  break;
                case "1,0":
                  mButtons[3].setText("X");
                  mButtons[3].setEnabled(false);
                  break;
                case "1,1":
                  mButtons[4].setText("X");
                  mButtons[4].setEnabled(false);
                  break;
                case "1,2":
                  mButtons[5].setText("X");
                  mButtons[5].setEnabled(false);
                  break;
                case "2,0":
                  mButtons[6].setText("X");
                  mButtons[6].setEnabled(false);
                  break;
                case "2,1":
                  mButtons[7].setText("X");
                  mButtons[7].setEnabled(false);
                  break;
                case "2,2":
                  mButtons[8].setText("X");
                  mButtons[8].setEnabled(false);
                  break;
              }
              win.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot5, @Nullable String previousChildName) {
                  if (snapshot5.getValue(String.class).equals(MainActivity.host.username)) {
                    win.child(MainActivity.host.username).removeValue();
                    MainActivity.host.wins++;
                    players.child(MainActivity.host.username).setValue(MainActivity.host);
                    int z = displaywin();
                  }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                  Log.d("Error",error.getMessage());
                }
              });
              lose.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot6, @Nullable String previousChildName) {
                  if (snapshot6.getValue(String.class).equals(MainActivity.host.username)) {
                    lose.child(MainActivity.host.username).removeValue();
                    MainActivity.host.losses++;
                    players.child(MainActivity.host.username).setValue(MainActivity.host);
                    int z = displayloss();
                  }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                  Log.d("Error",error.getMessage());
                }
              });
              draw.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot7, @Nullable String previousChildName) {
                  if (snapshot7.getValue(String.class).equals(gameID)) {
                    draw.child(gameID).removeValue();
                    MainActivity.host.draws++;
                    players.child(MainActivity.host.username).setValue(MainActivity.host);
                    int z = displaydraw();
                  }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                  Log.d("Error",error.getMessage());
                }
              });
            }
          }
          for (int i = 0; i < 9; i++) {
            final int pos = i;
            mButtons[i].setOnClickListener(v -> {
              Log.d(TAG, "Button " + pos + " clicked");
              turn.child(gameID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot3) {
                  if (snapshot3.getValue(String.class).equals(MainActivity.host.username)) {
                    if (gameType.equals(gameID)) {
                      mButtons[pos].setText("O");
                      mButtons[pos].setEnabled(false);
                      pTurn.setText(playerID+"'s turn");
                      switch (pos) {
                        case 0:
                          board.child(gameID).child("0,0").setValue("O");
                          turn.child(gameID).setValue(playerID);
                          break;
                        case 1:
                          board.child(gameID).child("0,1").setValue("O");
                          turn.child(gameID).setValue(playerID);
                          break;
                        case 2:
                          board.child(gameID).child("0,2").setValue("O");
                          turn.child(gameID).setValue(playerID);
                          break;
                        case 3:
                          board.child(gameID).child("1,0").setValue("O");
                          turn.child(gameID).setValue(playerID);
                          break;
                        case 4:
                          board.child(gameID).child("1,1").setValue("O");
                          turn.child(gameID).setValue(playerID);
                          break;
                        case 5:
                          board.child(gameID).child("1,2").setValue("O");
                          turn.child(gameID).setValue(playerID);
                          break;
                        case 6:
                          board.child(gameID).child("2,0").setValue("O");
                          turn.child(gameID).setValue(playerID);
                          break;
                        case 7:
                          board.child(gameID).child("2,1").setValue("O");
                          turn.child(gameID).setValue(playerID);
                          break;
                        case 8:
                          board.child(gameID).child("2,2").setValue("O");
                          turn.child(gameID).setValue(playerID);
                          break;
                      }
                      win.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot8, @Nullable String previousChildName) {
                          if (snapshot8.getValue(String.class).equals(MainActivity.host.username)) {
                            win.child(MainActivity.host.username).removeValue();
                            MainActivity.host.wins++;
                            players.child(MainActivity.host.username).setValue(MainActivity.host);
                            int z = displaywin();
                          }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                          Log.d("Error",error.getMessage());
                        }
                      });
                      lose.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot9, @Nullable String previousChildName) {
                          if (snapshot9.getValue(String.class).equals(MainActivity.host.username)) {
                            lose.child(MainActivity.host.username).removeValue();
                            MainActivity.host.losses++;
                            players.child(MainActivity.host.username).setValue(MainActivity.host);
                            int z = displayloss();
                          }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                          Log.d("Error",error.getMessage());
                        }
                      });
                      draw.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot0, @Nullable String previousChildName) {
                          if (snapshot0.getValue(String.class).equals(gameID)) {
                            draw.child(gameID).removeValue();
                            MainActivity.host.draws++;
                            players.child(MainActivity.host.username).setValue(MainActivity.host);
                            int z = displaydraw();
                          }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                          Log.d("Error",error.getMessage());
                        }
                      });
                    }
                  }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                  Log.d("Error",error.getMessage());
                }
              });
            });
          }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
          Log.d("Error",error.getMessage());
        }
      });
    }
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

  public int checkWin() {
    boolean b1 = (!mButtons[0].getText().toString().equals(""));
    boolean b2 = (!mButtons[1].getText().toString().equals(""));
    boolean b3 = (!mButtons[2].getText().toString().equals(""));
    boolean b4 = (!mButtons[3].getText().toString().equals(""));
    boolean b5 = (!mButtons[4].getText().toString().equals(""));
    boolean b6 = (!mButtons[5].getText().toString().equals(""));
    boolean b7 = (!mButtons[6].getText().toString().equals(""));
    boolean b8 = (!mButtons[7].getText().toString().equals(""));
    boolean b9 = (!mButtons[8].getText().toString().equals(""));
    boolean b = b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8 && b9;
    boolean win = false,lose = false,draw = false;
    if (pieceType1.equals("X")) {
      win = gl.checkXWin();
      lose = gl.checkOWin();
    }
    else if (pieceType1.equals("O")){
      win = gl.checkOWin();
      lose = gl.checkXWin();
    }
    if (win) {
      MainActivity.host.wins++;
      players.child(MainActivity.host.username).setValue(MainActivity.host);
      AlertDialog.Builder dialog1 = new AlertDialog.Builder(requireActivity())
              .setTitle("Congratulations!")
              .setMessage("You have won the game!")
              .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss());
      dialog1.show();
      return 0;
    }
    else if (lose) {
      MainActivity.host.losses++;
      players.child(MainActivity.host.username).setValue(MainActivity.host);
      AlertDialog.Builder dialog1 = new AlertDialog.Builder(requireActivity())
              .setTitle("Oh no!")
              .setMessage("You have lost the game!")
              .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss());
      dialog1.show();
      return 0;
    }
    else if (b) {
      MainActivity.host.draws++;
      players.child(MainActivity.host.username).setValue(MainActivity.host);
      AlertDialog.Builder dialog1 = new AlertDialog.Builder(requireActivity())
              .setTitle("Draw!")
              .setMessage("You have drawn the game!")
              .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss());
      dialog1.show();
      return 0;
    }
    return 1;
  }

  public int displaywin() {
    AlertDialog.Builder dialog1 = new AlertDialog.Builder(requireActivity())
            .setTitle("Congratulations!")
            .setMessage("You have won the game!")
            .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss());
    dialog1.show();
    return 1;
  }

  public int displayloss() {
    AlertDialog.Builder dialog1 = new AlertDialog.Builder(requireActivity())
            .setTitle("Oh no!")
            .setMessage("You have lost the game!")
            .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss());
    dialog1.show();
    return 1;
  }

  public int displaydraw() {
    AlertDialog.Builder dialog1 = new AlertDialog.Builder(requireActivity())
            .setTitle("Draw!")
            .setMessage("You have drawn the game!")
            .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss());
    dialog1.show();
    return 1;
  }
}