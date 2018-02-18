package team16.uw.tacoma.edu.thefriendzone.item;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Group is a model class for a group
 * and for parsing JSONdata that contains an array of groups
 */

public class Group implements Serializable{
    private String mGroupId;
    private String mFacilitator;
    private ArrayList<User> myGroupMembers;
    public static final String ID = "id", FACILITATOR="facilitator";
    private static final String GROUPMEMBERS_URL = "http://cssgate.insttech.washington.edu/~_450bteam16/list_group_members.php?";

    public Group(String mGroupId, String mFacilitator) {
        this.mGroupId = mGroupId;
        this.mFacilitator = mFacilitator;
    }

    public String getmGroupId() {
        return mGroupId;
    }

    public void setmGroupId(String mGroupId) {
        this.mGroupId = mGroupId;
    }

    public String getmFacilitator() {
        return mFacilitator;
    }

    public void setmFacilitator(String mFacilitator) {
        this.mFacilitator = mFacilitator;
    }

    public void setMGroupMembers(ArrayList<User> memberlist) {myGroupMembers = memberlist; }

    public ArrayList<User> getMGroupMembers() {return myGroupMembers; }

    public boolean isAvailable() {
        boolean b = true;
        for (User u : myGroupMembers) {
            b = (b && u.getMyAvailable());
        }
        return b;
    }


    /**
     * Parses the json string, returns an error message if unsuccessful
     * Returns group list if success
     * @param groupJSON
     * @return reason or null if successful
     */
    public static String parseGroupJSON(String groupJSON, List<Group> groupList) {
        String reason = null;
        if(groupJSON!=null){
            try{
                JSONArray arr = new JSONArray(groupJSON);
                for (int i =0; i<arr.length();i++){
                    JSONObject obj = arr.getJSONObject(i);
                    Group group = new Group(obj.getString(Group.ID), obj.getString(Group.FACILITATOR));
                    groupList.add(group);
                }
            }catch(JSONException e){
                reason = "Unable to parse data, Reason:"+e.getMessage();
            }
        }
        return reason;
    }

    public static String parseGroupMemberJSON(String groupMemberJSON, List<User> memberList) {
        String reason = null;
        if(groupMemberJSON!=null) {
            try {
                JSONArray arr = new JSONArray(groupMemberJSON);
                for (int i = 0; i <arr.length();i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Boolean available;
                    available = !obj.get("available").equals("0");
                    User member = new User(obj.getString("email"), obj.getString("username"), available);
                    memberList.add(member);
                }
            } catch (JSONException e) {
                reason = "Unable to parse member data, Reason: "+e.getMessage();
            }
        }
        return reason;
    }

    public void downloadMembersList() {
        try {
            DownloadGroupMembersTask task = new DownloadGroupMembersTask();
            StringBuilder sb = new StringBuilder(GROUPMEMBERS_URL.toString());
            sb.append("groupid=");
            sb.append(URLEncoder.encode(mGroupId, "UTF-8"));
            String url = sb.toString();
            task.execute(url);
        } catch (Exception e){
            Log.d("download memberslist", e.toString());
        }
    }

    /**
     * A private inner class for performing asynchronus fetching of group member data
     */
    private class DownloadGroupMembersTask extends AsyncTask<String, Void, String> {
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
                    urlConnection = (HttpURLConnection) urlObject.openConnection();
                    InputStream content = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s="";
                    while((s=buffer.readLine())!=null){
                        result+=s;
                    }
                }catch(Exception e){
                    result = "Unable to download the group member data, Reason: "+e.getMessage();
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
            if(result.startsWith("Unable to")){
                Log.d("group post execute", result);
                myGroupMembers = new ArrayList<>();
                //SystemClock.sleep(1000);
                //downloadMembersList();
                //Toast.makeText(getActivity().getApplicationContext(), "Unable to error in fetching group member info", Toast.LENGTH_LONG).show();
                return;
            }

            ArrayList<User> memberList = new ArrayList<>();
            Log.d("group post execute", result);
            result = Group.parseGroupMemberJSON(result, memberList);
            myGroupMembers = memberList;
            if(result!=null){
                Log.d("post exec group", "Unable to do something twice"+result);
                return;
            }
        }
    }
}
