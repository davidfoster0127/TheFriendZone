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
 * {@link RegisterFragment.AddUserListener} interface
 * to handle interaction events.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author Matthew Subido | subidomd@uw.edu
 * @version May 5, 2017
 */
public class RegisterFragment extends Fragment {

    public static final String ADD_USER_URL = "http://cssgate.insttech.washington.edu/~_450bteam16/adduser.php?";
    public static final String FRAG = "RegisterFragment";

    /** Member variables for the listener and text fields of the fragment. */
    private EditText mEmail;
    private EditText mUsername;
    private EditText mPassword;
    private EditText mRePassword;

    private AddUserListener mListener;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment RegisterFragment.
     */
    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
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
        View v = inflater.inflate(R.layout.fragment_register, container, false);

        // Inflate the layout for this fragment
        mEmail = (EditText) v.findViewById(R.id.reg_email);
        mUsername = (EditText) v.findViewById(R.id.reg_username);
        mPassword = (EditText) v.findViewById(R.id.reg_password);
        mRePassword = (EditText) v.findViewById(R.id.reg_re_password);

        Button button = (Button) v.findViewById(R.id.register_submit_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRePassword.getText().toString().equals(mPassword.getText().toString())) {
                    String url = buildRegisterURL(v);
                    mListener.addUser(url);
                } else {
                    Toast.makeText(getContext(), "Error Adding User: Passwords did not Match",
                            Toast.LENGTH_LONG)
                            .show();
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                }
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddUserListener) {
            mListener = (AddUserListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Pulls the string from the member variables and creates a URL with them.
     *
     * @param v is the current View.
     * @return the completed URL.
     */
    private String buildRegisterURL(View v) {
        StringBuilder sb = new StringBuilder(ADD_USER_URL);
        try {
            String email = mEmail.getText().toString();
            sb.append("&email=");
            sb.append(URLEncoder.encode(email, "UTF-8"));
            String password = mPassword.getText().toString();
            sb.append("&password=");
            sb.append(URLEncoder.encode(password, "UTF-8"));
            String username = mUsername.getText().toString();
            sb.append("&username=");
            sb.append(URLEncoder.encode(username, "UTF-8"));
            Log.i("RegisterFragment", sb.toString());
        }
        catch(Exception e) {
            Toast.makeText(v.getContext(), "Something wrong with the url" + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
        return sb.toString();
    }

    /**
     * The Interface to be held by parent activity. Handles the addUser action.
     */
    public interface AddUserListener {
        void addUser(String url);
    }
}
