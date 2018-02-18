package team16.uw.tacoma.edu.thefriendzone.friend;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import team16.uw.tacoma.edu.thefriendzone.R;
import team16.uw.tacoma.edu.thefriendzone.database.FriendZoneDB;
import team16.uw.tacoma.edu.thefriendzone.item.User;

/**
 * A fragment representing a list of Groups.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFriendListFragmentInteractionListener}
 * interface.
 */
public class FriendListFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    private RecyclerView mRecyclerView;
    private OnFriendListFragmentInteractionListener mListener;
    private SharedPreferences mPreferences;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendListFragment() {
    }

    public static FriendListFragment newInstance(int columnCount) {
        FriendListFragment fragment = new FriendListFragment();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);
        mPreferences = getActivity().getSharedPreferences(getString(R.string.LOGIN_PREFS), Context.MODE_PRIVATE);
        Context context = view.getContext();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_friend);
        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        FriendZoneDB friendZoneDB = new FriendZoneDB(getContext());
        friendZoneDB.eraseAndInitialize();
        friendZoneDB.downloadFriends(mPreferences.getString("email", ""), mRecyclerView, mListener);

        Button fab = (Button) view.findViewById(R.id.friend_add_button);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new AddFriendFragment();
                if (newFragment != null)
                    newFragment.show(getActivity().getSupportFragmentManager(), "onClick");
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFriendListFragmentInteractionListener) {
            mListener = (OnFriendListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFriendListFragmentInteractionListener");
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
    public interface OnFriendListFragmentInteractionListener {
        void onFriendListFragmentInteraction(User item);
    }
}
