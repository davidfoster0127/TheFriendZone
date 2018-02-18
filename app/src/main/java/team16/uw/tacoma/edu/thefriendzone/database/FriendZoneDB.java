package team16.uw.tacoma.edu.thefriendzone.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import team16.uw.tacoma.edu.thefriendzone.friend.FriendListFragment;
import team16.uw.tacoma.edu.thefriendzone.friend.FriendListRecyclerViewAdapter;
import team16.uw.tacoma.edu.thefriendzone.group.GroupListFragment;
import team16.uw.tacoma.edu.thefriendzone.group.GroupListRecyclerViewAdapter;
import team16.uw.tacoma.edu.thefriendzone.item.Group;
import team16.uw.tacoma.edu.thefriendzone.item.User;

/**
 * Created by David on 5/26/2017.
 */

public class FriendZoneDB {
    private FriendZoneDBHelper mFriendZoneDBHelper;
    private SQLiteDatabase mSQLiteDatabase;
    private static final String FRIEND_TABLE = "friends";
    private static final String GROUP_TABLE = "groups";
    private static final String FRIEND_URL = "http://cssgate.insttech.washington.edu/~_450bteam16/list_friends.php?";
    private static final String GROUP_URL = "http://cssgate.insttech.washington.edu/~_450bteam16/list_groups.php?";


    public FriendZoneDB(Context context){
        mFriendZoneDBHelper = FriendZoneDBHelper.getInstance(context);
        mSQLiteDatabase = mFriendZoneDBHelper.getWritableDatabase();
    }

    public void eraseAndInitialize(){
        mFriendZoneDBHelper.eraseAndInitialize(mSQLiteDatabase);
    }

    public boolean insertFriend(String email, String username, Boolean available){
        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("username", username);
        cv.put("available", available.toString());

        long rowId = mSQLiteDatabase.insert(FRIEND_TABLE, null, cv);
        return rowId != -1;
    }

    public boolean insertGroup(String groupid, String facilitator){
        ContentValues cv = new ContentValues();
        cv.put("groupid", groupid);
        cv.put("facilitator", facilitator);

        long rowId = mSQLiteDatabase.insert(GROUP_TABLE, null, cv);
        return rowId != -1;
    }

    public List<User> getFriends() {
        String [] columns = {
                "email", "username", "available"
        };
        Cursor c = mSQLiteDatabase.query(
                FRIEND_TABLE,
                columns, null, null, null, null, null
        );
        c.moveToFirst();
        List<User> list = new ArrayList<User>();
        for (int i = 0; i <c.getCount(); i++){
            String email = c.getString(0);
            String username = c.getString(1);
            String available = c.getString(2);
            User user = new User(email, username, Boolean.parseBoolean(available));
            list.add(user);
            c.moveToNext();
        }
        return list;
    }

    public List<Group> getGroups() {
        String [] columns = {
                "groupid", "facilitator"
        };
        Cursor c = mSQLiteDatabase.query(
                GROUP_TABLE,
                columns, null, null, null, null, null
        );
        c.moveToFirst();
        List<Group> list = new ArrayList<Group>();
        for (int i = 0; i<c.getCount();i++){
            String groupid = c.getString(0);
            String facilitator = c.getString(1);
            Group group = new Group(groupid, facilitator);
            group.downloadMembersList();
            list.add(group);
            c.moveToNext();
        }
        return list;
    }

    public void downloadFriends(String email, RecyclerView recyclerView,
                                FriendListFragment.OnFriendListFragmentInteractionListener mListener) {
        DownloadFriendsTask task = new DownloadFriendsTask();
        StringBuilder sb = new StringBuilder(FRIEND_URL);
        sb.append("user=");
        try {
            sb.append(URLEncoder.encode(email, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = sb.toString();
        task.passThings(recyclerView, mListener);
        task.execute(url);
    }

    public void downloadGroups(String email, RecyclerView recyclerView,
                                GroupListFragment.OnGroupListInteractionListener mListener) {
        DownloadGroupsTask task = new DownloadGroupsTask();
        StringBuilder sb = new StringBuilder(GROUP_URL);
        sb.append("user=");
        try {
            sb.append(URLEncoder.encode(email, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = sb.toString();
        task.passThings(recyclerView, mListener);
        task.execute(url);
    }

    /**
     * A private inner class for performing asynchronus fetching of group data
     */
    private class DownloadFriendsTask extends AsyncTask<String, Void, String> {

        private RecyclerView recyclerView;
        private FriendListFragment.OnFriendListFragmentInteractionListener mListener;

        public void passThings(RecyclerView recyclerView,
                               FriendListFragment.OnFriendListFragmentInteractionListener mListener) {
            this.recyclerView = recyclerView;
            this.mListener = mListener;
        }
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param urls The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            HttpURLConnection urlConnection = null;
            for(String url:urls){
                try{
                    URL urlObject = new URL(url);
                    urlConnection=(HttpURLConnection) urlObject.openConnection();
                    InputStream content = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s="";
                    while((s=buffer.readLine())!=null){
                        result+=s;
                    }
                }catch(Exception e){
                    result = "Unable to download the list of course, Reason: "+e.getMessage();
                }finally{
                    if(urlConnection!=null) urlConnection.disconnect();
                }
            }
            return result;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param result The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(String result) {

            List<User> friendList = new ArrayList<User>();
            User.parseFriendJSON(result, friendList);

            for (User u : friendList){
                insertFriend(u.getMyEmail(), u.getMyUsername(), u.getMyAvailable());
                Log.d("in hackydbinsertfriend", u.getMyEmail() + "  " +u.getMyUsername());
            }

            List<User> list = getFriends();
            for (User u : list) {
                Log.d("testing sql get friends",u.getMyEmail() +" "+u.getMyUsername()+" ");
            }

            if(!friendList.isEmpty()){
                recyclerView.setAdapter(new FriendListRecyclerViewAdapter(friendList, mListener));
            }
        }

    }

    /**
     * A private inner class for performing asynchronus fetching of group data
     */
    private class DownloadGroupsTask extends AsyncTask<String, Void, String> {

        private RecyclerView recyclerView;
        private GroupListFragment.OnGroupListInteractionListener mListener;

        public void passThings(RecyclerView recyclerView,
                              GroupListFragment.OnGroupListInteractionListener mListener) {
            this.recyclerView = recyclerView;
            this.mListener = mListener;
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param urls The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            HttpURLConnection urlConnection = null;
            for(String url:urls){
                try{
                    URL urlObject = new URL(url);
                    urlConnection=(HttpURLConnection) urlObject.openConnection();
                    InputStream content = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s="";
                    while((s=buffer.readLine())!=null){
                        result+=s;
                    }
                }catch(Exception e){
                    result = "Unable to download the list of groups, Reason: "+e.getMessage();
                }finally{
                    if(urlConnection!=null) urlConnection.disconnect();
                }
            }
            return result;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param result The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(String result) {

            List<Group> mGroupList = new ArrayList<Group>();
            Group.parseGroupJSON(result, mGroupList);

            if(!mGroupList.isEmpty()){
                for (Group g: mGroupList) {
                    Log.d("download groups task", "found group: "+g.getmGroupId());
                    insertGroup(g.getmGroupId(), g.getmFacilitator());
                    g.downloadMembersList();
                }
                recyclerView.setAdapter(new GroupListRecyclerViewAdapter(mGroupList, mListener));
            }
        }
    }
}
