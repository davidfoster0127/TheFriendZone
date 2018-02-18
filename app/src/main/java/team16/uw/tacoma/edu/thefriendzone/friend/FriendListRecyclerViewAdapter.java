package team16.uw.tacoma.edu.thefriendzone.friend;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import team16.uw.tacoma.edu.thefriendzone.R;
import team16.uw.tacoma.edu.thefriendzone.friend.FriendListFragment.OnFriendListFragmentInteractionListener;
import team16.uw.tacoma.edu.thefriendzone.item.Group;
import team16.uw.tacoma.edu.thefriendzone.item.User;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Group} and makes a call to the
 * specified {@link OnFriendListFragmentInteractionListener}.
 */
public class FriendListRecyclerViewAdapter extends RecyclerView.Adapter<FriendListRecyclerViewAdapter.ViewHolder> {

    private final List<User> mValues;
    private final FriendListFragment.OnFriendListFragmentInteractionListener mListener;

    public FriendListRecyclerViewAdapter(List<User> items, OnFriendListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friend_list_item, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getMyUsername());
        holder.mContentView.setImageResource(mValues.get(position).getMyAvailable() ?
                R.drawable.ic_filled_star :
                R.drawable.ic_empty_star);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onFriendListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * An inner class that determines the content to be displayed in the group list
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final ImageView mContentView;
        public User mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id_friend);
            mContentView = (ImageView) view.findViewById(R.id.content_friend);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.toString() + "'";
        }
    }
}
