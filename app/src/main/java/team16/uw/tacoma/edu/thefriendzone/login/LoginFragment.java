package team16.uw.tacoma.edu.thefriendzone.login;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URLEncoder;

import team16.uw.tacoma.edu.thefriendzone.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.SignInListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author Matthew Subido | subidomd@uw.edu
 * @version May 5, 2017
 */
public class LoginFragment extends Fragment {

    /** The location of login.php for use with logging in to user accounts. */
    public static final String SIGN_IN_URL = "http://cssgate.insttech.washington.edu/~_450bteam16/login.php?";

    /** Member variables for the listener and text fields of the fragment. */
    private SignInListener mListener;
    private EditText mEmail;
    private EditText mPassword;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        // Inflate the layout for this fragment
        mEmail = (EditText) v.findViewById(R.id.login_email);
        mPassword = (EditText) v.findViewById(R.id.login_password);

        Button button = (Button) v.findViewById(R.id.sign_in_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = buildLoginURL(v);
                mListener.signIn(url);
            }
        });

        return v;
    }

    /**
     * Pulls the string from the member variables and creates a URL with them.
     *
     * @param v is the current View.
     * @return the completed URL.
     */
    private String buildLoginURL(View v) {
        StringBuilder sb = new StringBuilder(SIGN_IN_URL);
        try {
            String email = mEmail.getText().toString();
            sb.append("&email=");
            sb.append(URLEncoder.encode(email, "UTF-8"));
            String password = mPassword.getText().toString();
            sb.append("&password=");
            sb.append(URLEncoder.encode(password, "UTF-8"));
            Log.i("LoginFragment", sb.toString());
        }
        catch(Exception e) {
            Toast.makeText(v.getContext(), "Something wrong with the url" + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
        return sb.toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SignInListener) {
            mListener = (SignInListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SignInListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * The Interface to be held by parent activity. Handles the signIn action.
     */
    public interface SignInListener {
        void signIn(String url);
    }
}
