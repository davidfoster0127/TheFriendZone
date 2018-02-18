package team16.uw.tacoma.edu.thefriendzone.group;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import team16.uw.tacoma.edu.thefriendzone.R;
import team16.uw.tacoma.edu.thefriendzone.item.Group;
import team16.uw.tacoma.edu.thefriendzone.item.User;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnGroupMemberListInteractionListener}
 * interface.
 */
public class GroupFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    public final static String GROUP_ITEM_SELECTED = "group_selected";
    private static final String REMOVE_GROUP_URL =
            "http://cssgate.insttech.washington.edu/~_450bteam16/removeGroup.php?";


    private int mColumnCount = 1;
    private RecyclerView mRecyclerView;
    private Group mGroup;
    private OnGroupMemberListInteractionListener mListener;
    private SharedPreferences mPreferences;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupFragment() {
    }

    @SuppressWarnings("unused")
    public static GroupFragment newInstance(int columnCount) {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mGroup = (Group) getArguments().getSerializable(GROUP_ITEM_SELECTED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        Context context = view.getContext();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_group_details);
        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        mGroup.downloadMembersList();
        mRecyclerView.setAdapter(new MemberListRecyclerViewAdapter(mGroup.getMGroupMembers(),
                mListener, mGroup));
        mPreferences = getActivity().getSharedPreferences(getString(R.string.LOGIN_PREFS),
                Context.MODE_PRIVATE);

        boolean isFacilitator = mPreferences.getString("email", "UTF-8").equals(mGroup.getmFacilitator());

        Button addgroupbutton = (Button) view.findViewById(R.id.group_member_add_button);
        addgroupbutton.setEnabled(isFacilitator);
        addgroupbutton.setVisibility(isFacilitator ? View.VISIBLE : View.GONE);
        addgroupbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Create and show the dialog.
                DialogFragment newFragment = AddFriendToGroupFragment.newInstance(mGroup);
                if (newFragment != null)
                    newFragment.show(getActivity().getSupportFragmentManager(), "onClick");
            }
        });

        Button delete = (Button) view.findViewById(R.id.group_delete_button);
        delete.setEnabled(isFacilitator);
        delete.setVisibility(isFacilitator ? View.VISIBLE : View.GONE);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

// 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage("Would you like to delete this Group?")
                        .setTitle("Remove " + mGroup.getmGroupId());
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            StringBuilder sb = new StringBuilder(REMOVE_GROUP_URL);
                            sb.append("groupid=");
                            sb.append(URLEncoder.encode(mGroup.getmGroupId(), "UTF-8"));

                            Log.v("GroupFragment", sb.toString());

                            RemoveGroupTask task = new RemoveGroupTask();
                            task.execute(sb.toString());
                        } catch (Exception e) {
                            Log.e("MainActivity", "Error Building Remove Group URL!");
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
// 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGroupMemberListInteractionListener) {
            mListener = (OnGroupMemberListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnGroupMemberListInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnGroupMemberListInteractionListener {
        void onGroupMemberListInteraction(User item, Group group);
    }

    private class RemoveGroupTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url: urls){
                try{
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while((s=buffer.readLine())!=null){
                        response+=s;
                    }
                } catch (Exception e) {
                    response = "Unable to remove group, Reason: " + e.getMessage();
                } finally {
                    if(urlConnection != null) urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = (String) jsonObject.get("result");
                if(status.equals("success")) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Group removed successfully!", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Failed to remove: "+
                            jsonObject.get("error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong with the data "+
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
