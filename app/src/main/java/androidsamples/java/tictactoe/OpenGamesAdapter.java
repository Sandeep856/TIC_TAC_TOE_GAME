package androidsamples.java.tictactoe;

import android.content.Context;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {
  private static List<Lobby> lobbies;
  private LayoutInflater mInflator;

  public OpenGamesAdapter(Context context) {
    // FIXME if needed
    mInflator = LayoutInflater.from(context);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
    // TODO bind the item at the given position to the holder
    if (lobbies != null) {
      Lobby lobby  = lobbies.get(position);
      holder.mIdView.setText(lobby.gameID);
      holder.mContentView.setText("Host : "+lobby.host.username);
    }
  }

  @Override
  public int getItemCount() {
    return (lobbies == null) ? 0 : lobbies.size(); // FIXME
  }

  public void setAccounts(List<Lobby> lobbyList) {
    lobbies = lobbyList;
    notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mView.setOnClickListener(this::lobbyLaunch);
      mIdView = view.findViewById(R.id.item_number);
      mContentView = view.findViewById(R.id.content);
    }

    public void lobbyLaunch(View v) {
      NavDirections action = DashboardFragmentDirections.actionGame(lobbies.get(getAdapterPosition()).gameID+" "+lobbies.get(getAdapterPosition()).host.username);
      Navigation.findNavController(v).navigate(action);
    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}