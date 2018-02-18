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


/**
 * A simple {@link Fragment} subclass that contains a field and button for adding a new group.
 * Activities that contain this fragment must implement the
 * {@link AddGroupFragment.GroupAddListener} interface
 * to handle interaction events.
 * Use the {@link AddGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddGroupFragment extends DialogFragment {
    private static final String GROUP_ADD_URL = "http://cssgate.insttech.washington.edu/~_450bteam16/addgroup.php?";

    private EditText mAddGroupIdEditText;
    private GroupAddListener mListener;

    public interface GroupAddListener{
        void addGroup(String url);
    }

    public AddGroupFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment AddGroupFragment.
     */
    public static AddGroupFragment newInstance() {
        AddGroupFragment fragment = new AddGroupFragment();
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
        View v = inflater.inflate(R.layout.fragment_add_group, container, false);
        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_add_group, null))
                .setPositiveButton("Add Group", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAddGroupIdEditText = (EditText) getDialog().findViewById(R.id.add_group_id_text);
                        String url = buildGroupURL(getActivity().findViewById(R.id.view_pager));
                        Log.i("url", url.toString());
                        mListener.addGroup(url);
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
    private String buildGroupURL(View v){
        StringBuilder sb = new StringBuilder(GROUP_ADD_URL);
        try{
            String groupId = mAddGroupIdEditText.getText().toString();
            sb.append("id=");
            sb.append(groupId);

            String groupFac = getActivity()
                    .getSharedPreferences(getString(R.string.LOGIN_PREFS), Context.MODE_PRIVATE)
                    .getString("email", "");
            sb.append("&facilitator=");
            sb.append(URLEncoder.encode(groupFac, "UTF-8"));
        } catch (Exception e){
            Toast.makeText(v.getContext(), "Something went wrong with the url" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return sb.toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GroupAddListener) {
            mListener = (GroupAddListener) context;
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
