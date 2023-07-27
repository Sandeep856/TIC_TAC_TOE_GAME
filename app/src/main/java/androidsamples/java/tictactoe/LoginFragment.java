package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginStatus";
    private DatabaseReference dbr;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        dbr = FirebaseDatabase.getInstance().getReference();
        // TODO if a user is logged in, go to Dashboard
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
            Navigation.findNavController(requireActivity(),R.id.loginFragment).navigate(action);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        EditText email = view.findViewById(R.id.edit_email);
        EditText password = view.findViewById(R.id.edit_password);
        view.findViewById(R.id.btn_log_in)
            .setOnClickListener(v -> {
                // TODO implement sign in logic
                String e = email.getText().toString();
                String p = password.getText().toString();
                mAuth.fetchSignInMethodsForEmail(e).addOnCompleteListener(t -> {
                    boolean b = t.getResult().getSignInMethods().isEmpty();
                    if (b) {
                        mAuth.createUserWithEmailAndPassword(e,p).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG,"accountCreationAndSignInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                                Navigation.findNavController(v).navigate(action);
                            }
                        });
                    }
                    else {
                        mAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG,"signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                                Navigation.findNavController(v).navigate(action);
                            }
                        });
                    }
                });
            });

        return view;
    }

    // No options menu in login fragment.
}