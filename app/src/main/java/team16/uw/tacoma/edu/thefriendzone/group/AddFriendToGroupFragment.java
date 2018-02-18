package team16.uw.tacoma.edu.thefriendzone.group;

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
import team16.uw.tacoma.edu.thefriendzone.item.Group;


/**
 * A simple {@link Fragment} subclass that contains a field and button for adding a new group.
 * Activities that contain this fragment must implement the
 * {@link AddGroupFragment.GroupAddListener} interface
 * to handle interaction events.
 * Use the {@link AddGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFriendToGroupFragment extends DialogFragment {
    private static final String GROUP_ADD_URL = "http://cssgate.insttech.washington.edu/~_450bteam16/addfriendtogroup.php?";

    private EditText mAddIdEditText;
    private GroupFriendAddListener mListener;
    private Group mGroup;

    public interface GroupFriendAddListener{
        void addFriendToGroup(String url);
    }

    public AddFriendToGroupFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment AddGroupFragment.
     */
    public static AddFriendToGroupFragment newInstance(Group group) {
        AddFriendToGroupFragment fragment = new AddFriendToGroupFragment();
        Bundle args = new Bundle();
        args.putSerializable("group", group);
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
        View v = inflater.inflate(R.layout.fragment_add_friend_group, container, false);
        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_add_friend_group, null))
                .setPositiveButton("Add Friend to Group", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAddIdEditText = (EditText) getDialog().findViewById(R.id.add_friend_group_id_text);
                        String url = buildURL(getActivity().findViewById(R.id.group_detail_fragment_container));
                        Log.v("url", url.toString());
                        mListener.addFriendToGroup(url);
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
    private String buildURL(View v){
        StringBuilder sb = new StringBuilder(GROUP_ADD_URL);
        try{
            String userId = mAddIdEditText.getText().toString();
            sb.append("user=");
            sb.append(URLEncoder.encode(userId, "UTF-8"));

            mGroup = (Group) getArguments().getSerializable("group");

            String groupID = mGroup.getmGroupId();
            sb.append("&groupid=");
            sb.append(URLEncoder.encode(groupID, "UTF-8"));
        } catch (Exception e){
            Toast.makeText(v.getContext(), "Something went wrong with the url" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return sb.toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GroupFriendAddListener) {
            mListener = (GroupFriendAddListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement GroupFriendAddListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
