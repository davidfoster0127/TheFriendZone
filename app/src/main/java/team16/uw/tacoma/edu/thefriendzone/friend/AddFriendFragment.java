package team16.uw.tacoma.edu.thefriendzone.friend;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URLEncoder;

import team16.uw.tacoma.edu.thefriendzone.R;
import team16.uw.tacoma.edu.thefriendzone.group.AddGroupFragment;


/**
 * A simple {@link Fragment} subclass that contains a field and button for adding a new group.
 * Activities that contain this fragment must implement the
 * {@link AddFriendFragment.FriendAddListener} interface
 * to handle interaction events.
 * Use the {@link AddGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFriendFragment extends DialogFragment {
    private static final String FRIEND_ADD_URL = "http://cssgate.insttech.washington.edu/~_450bteam16/addfriend.php?";

    private EditText mAddFriendIdEditText;
    private FriendAddListener mListener;

    public interface FriendAddListener{
        void addFriend(String url);
    }

    public AddFriendFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment AddGroupFragment.
     */
    public static AddFriendFragment newInstance() {
        AddFriendFragment fragment = new AddFriendFragment();
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
        View v = inflater.inflate(R.layout.fragment_add_friend, container, false);
        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_add_friend, null))
                .setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAddFriendIdEditText = (EditText) getDialog().findViewById(R.id.add_friend_id_text);
                        String url = buildFriendURL(getActivity().findViewById(R.id.view_pager));
                        Log.v("AddFriendFragment", url);
                        mListener.addFriend(url);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    /**
     * A method to build a url string with the appropriate fields for id and facilitator
     * @param v the view
     * @return the url string
     */
    private String buildFriendURL(View v){
        StringBuilder sb = new StringBuilder(FRIEND_ADD_URL);
        try{
            String friendEmail = mAddFriendIdEditText.getText().toString();
            sb.append("user1=");
            sb.append(friendEmail);

            String email = getActivity()
                    .getSharedPreferences(getString(R.string.LOGIN_PREFS), Context.MODE_PRIVATE)
                    .getString("email", "");
            sb.append("&user2=");
            sb.append(URLEncoder.encode(email, "UTF-8"));
        } catch (Exception e){
            Toast.makeText(v.getContext(), "Something went wrong with the url" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return sb.toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FriendAddListener) {
            mListener = (FriendAddListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement GroupAddListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}