package team16.uw.tacoma.edu.thefriendzone.group;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import team16.uw.tacoma.edu.thefriendzone.R;
import team16.uw.tacoma.edu.thefriendzone.group.GroupListFragment.OnGroupListInteractionListener;
import team16.uw.tacoma.edu.thefriendzone.item.Group;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Group} and makes a call to the
 * specified {@link GroupListFragment.OnGroupListInteractionListener}.
 */
public class GroupListRecyclerViewAdapter extends RecyclerView.Adapter<GroupListRecyclerViewAdapter.ViewHolder> {

    private final List<Group> mValues;
    private final OnGroupListInteractionListener mListener;

    public GroupListRecyclerViewAdapter(List<Group> items, GroupListFragment.OnGroupListInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_group_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getmGroupId());
        holder.mContentView.setText(mValues.get(position).getmFacilitator());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onGroupListInteraction(holder.mItem);
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
        public final TextView mContentView;
        public Group mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id_group);
            mContentView = (TextView) view.findViewById(R.id.content_group);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
