package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class DashboardFragment extends Fragment {

  private FirebaseAuth mAuth;
  private static final String TAG = "DashboardFragment";
  private DatabaseReference lobbies;
  private NavController mNavController;
  private Vector<Lobby> lobbyList = new Vector<>();
  private List<Lobby> lobbyList1;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DashboardFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");
    mAuth = FirebaseAuth.getInstance();
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
  }

  @Override
  public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
    lobbies = FirebaseDatabase.getInstance().getReference("Lobbies");
    OpenGamesAdapter adapter = new OpenGamesAdapter(requireContext());
    lobbies.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
          lobbyList.add(dataSnapshot.getValue(Lobby.class));
        }
        lobbyList1 = Collections.list(lobbyList.elements());
        adapter.setAccounts(lobbyList1);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.d("Error",error.getMessage());
      }
    });
    RecyclerView recyclerView = view.findViewById(R.id.list);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mNavController = Navigation.findNavController(view);
    TextView placeholder = view.findViewById(R.id.txt_score);
    // TODO if a user is not logged in, go to LoginFragment
    FirebaseUser user = mAuth.getCurrentUser();
    if (user == null) {
      NavDirections action = DashboardFragmentDirections.actionNeedAuth();
      mNavController.navigate(action);
    }
    // Show a dialog when the user clicks the "new game" button
    view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {

      // A listener for the positive and negative buttons of the dialog
      DialogInterface.OnClickListener listener = (dialog, which) -> {
        String gameType = "No type";
        if (which == DialogInterface.BUTTON_POSITIVE) {
          gameType = getString(R.string.two_player);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
          gameType = getString(R.string.one_player);
        }
        Log.d(TAG, "New Game: " + gameType);

        // Passing the game type as a parameter to the action
        // extract it in GameFragment in a type safe way
        NavDirections action = DashboardFragmentDirections.actionGame(gameType);
        mNavController.navigate(action);
      };

      // create the dialog
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
          .setTitle(R.string.new_game)
          .setMessage(R.string.new_game_dialog_message)
          .setPositiveButton(R.string.two_player, listener)
          .setNegativeButton(R.string.one_player, listener)
          .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
          .create();
      dialog.show();
    });
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }
}