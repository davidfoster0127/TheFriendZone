package team16.uw.tacoma.edu.thefriendzone;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import team16.uw.tacoma.edu.thefriendzone.group.AddFriendToGroupFragment;
import team16.uw.tacoma.edu.thefriendzone.group.GroupFragment;
import team16.uw.tacoma.edu.thefriendzone.item.Group;
import team16.uw.tacoma.edu.thefriendzone.item.User;

public class GroupActivity extends AppCompatActivity implements GroupFragment.OnGroupMemberListInteractionListener,
        AddFriendToGroupFragment.GroupFriendAddListener {

    private static final String REMOVE_FROM_GROUP_URL =
            "http://cssgate.insttech.washington.edu/~_450bteam16/removeFromGroup.php?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        GroupFragment firstFragment = new GroupFragment();
        firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.group_detail_fragment_container,
                firstFragment).commit();
    }

    @Override
    public void onGroupMemberListInteraction(final User item, final Group group) {
        User user = (User) getIntent().getSerializableExtra("user");
        if ((item.getMyEmail().equals(user.getMyEmail()) &&
                !group.getmFacilitator().equals(user.getMyEmail())) ||
                (group.getmFacilitator().equals(user.getMyEmail()) &&
                        !item.getMyEmail().equals(user.getMyEmail()))) {
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

// 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage(item.getMyEmail().equals(user.getMyEmail()) ?
                    "Would you like to remove yourself from the Group?" :
                    "Would you like to remove this friend?")
                    .setTitle("Remove " + item.getMyUsername() + " from Group");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        StringBuilder sb = new StringBuilder(REMOVE_FROM_GROUP_URL);
                        sb.append("groupid=");
                        sb.append(URLEncoder.encode(group.getmGroupId(), "UTF-8"));
                        sb.append("&user=");
                        sb.append(URLEncoder.encode(item.getMyEmail(), "UTF-8"));

                        Log.v("GroupActivity", sb.toString());

                        RemoveFromGroupTask task = new RemoveFromGroupTask();
                        task.execute(sb.toString());
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error Building Remove Friend URL!");
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
        } else {
            Toast.makeText(getApplicationContext(),
                    item.getMyEmail().equals(user.getMyEmail()) ?
                            "Cannot remove Facilitator from Group" :
                    "You are not the Facilitator", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void addFriendToGroup(String url) {
        AddFriendToGroupTask task = new AddFriendToGroupTask();
        task.execute(url);

        getSupportFragmentManager().popBackStackImmediate();
        finish();
    }

    private class AddFriendToGroupTask extends AsyncTask<String, Void, String> {
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
                    response = "Unable to add friend, Reason: " + e.getMessage();
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

                    Toast.makeText(getApplicationContext(), "Friend added successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to add: "+
                            jsonObject.get("error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong with the data "+
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class RemoveFromGroupTask extends AsyncTask<String, Void, String> {
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
                    response = "Unable to remove from group, Reason: " + e.getMessage();
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
                    Toast.makeText(getApplicationContext(), "Removed successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to remove: "+
                            jsonObject.get("error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong with the data "+
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

}
