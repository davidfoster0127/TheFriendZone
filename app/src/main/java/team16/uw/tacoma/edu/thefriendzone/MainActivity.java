package team16.uw.tacoma.edu.thefriendzone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import team16.uw.tacoma.edu.thefriendzone.database.FriendZoneDB;
import team16.uw.tacoma.edu.thefriendzone.friend.AddFriendFragment;
import team16.uw.tacoma.edu.thefriendzone.friend.FriendListFragment;
import team16.uw.tacoma.edu.thefriendzone.group.AddGroupFragment;
import team16.uw.tacoma.edu.thefriendzone.group.GroupFragment;
import team16.uw.tacoma.edu.thefriendzone.group.GroupListFragment;
import team16.uw.tacoma.edu.thefriendzone.item.Group;
import team16.uw.tacoma.edu.thefriendzone.item.User;
import team16.uw.tacoma.edu.thefriendzone.login.LoginActivity;

/**
 * The primary activity started after a successful login that acts as a container and listener for the primary fragments.
 */
public class MainActivity extends AppCompatActivity implements
        GroupListFragment.OnGroupListInteractionListener,
        FriendListFragment.OnFriendListFragmentInteractionListener,
        AddGroupFragment.GroupAddListener,
        AddFriendFragment.FriendAddListener {

    private FriendZoneDB mFZDB;

    private static final String AVAILABLE_URL = "http://cssgate.insttech.washington.edu/~_450bteam16/setAvailable.php?";
    private static final String REMOVE_FRIEND_URL = "http://cssgate.insttech.washington.edu/~_450bteam16/removeFriend.php?";

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private FloatingActionButton mFab;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private MainActivityPagerAdapater mPageAdapter;
    private SharedPreferences mSharedPreferences;

    private User myUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
             // DownloadUserInfoTask task = new DownloadUserInfoTask();
            // task.execute(new String[]{COURSE_URL});
        } else {
            Toast.makeText(this.getApplicationContext(),
                    "No network connection available. Logging out...",
                    Toast.LENGTH_SHORT)
                    .show();
            logout();
        }

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPageAdapter = new MainActivityPagerAdapater(getSupportFragmentManager());
        mViewPager.setAdapter(mPageAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);

        mSharedPreferences = getSharedPreferences(getString(R.string.LOGIN_PREFS),
                Context.MODE_PRIVATE);

        myUser = new User(mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("username", null),
                mSharedPreferences.getBoolean("available", false));

        mFZDB = new FriendZoneDB(getApplicationContext());


        mFab = (FloatingActionButton) findViewById(R.id.fab);
        setFAB(myUser.getMyAvailable());

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    StringBuilder sb = new StringBuilder(AVAILABLE_URL);
                    sb.append("user=");
                    sb.append(URLEncoder.encode(myUser.getMyEmail(), "UTF-8"));
                    sb.append("&available=");
                    sb.append(!myUser.getMyAvailable() ? "TRUE" : "FALSE");

                    Log.v("MainActivity", sb.toString());
                    SetAvailableTask task = new SetAvailableTask();
                    task.execute(sb.toString());
                } catch (Exception e) {
                    Log.e("MainActivity", "Error Building URL!");
                }

//                myUser.toggleMyAvailable();
//
//                setFAB(myUser.getMyAvailable());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_bar) {
            return logout();
        }
        return true;
    }

    private void setFAB(final boolean theState) {
        Log.v("MainActivity", theState ? "TRUE" : "FALSE");
        if (theState)
            mFab.setImageResource(R.drawable.ic_filled_star);
        else
            mFab.setImageResource(R.drawable.ic_empty_star);
    }

    private boolean logout() {
        mSharedPreferences.edit().putBoolean(getString(R.string.LOGGEDIN), false)
                .putString("email", null)
                .putString("username", null)
                .commit();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            //decide what to do instead of going back to the login
            super.onBackPressed();
        }
    }

    @Override
    public void onGroupListInteraction(Group item) {
        Intent intent = new Intent(this, GroupActivity.class);
        intent.putExtra(GroupFragment.GROUP_ITEM_SELECTED, item);
        intent.putExtra("user", myUser);
        startActivity(intent);
    }

    /**
     * Starts an async task to add group to the database
     * @param url contains the information about the group to be created
     */
    @Override
    public void addGroup(String url) {
        AddGroupTask task = new AddGroupTask();
        task.execute(url.toString());
        mPageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFriendListFragmentInteraction(final User item) {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Would you like to remove this friend?")
                .setTitle("Remove " + item.getMyUsername() + " from Friend List");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    StringBuilder sb = new StringBuilder(REMOVE_FRIEND_URL);
                    sb.append("user1=");
                    sb.append(URLEncoder.encode(myUser.getMyEmail(), "UTF-8"));
                    sb.append("&user2=");
                    sb.append(URLEncoder.encode(item.getMyEmail(), "UTF-8"));

                    Log.v("MainActivity", sb.toString());

                    RemoveFriendTask task = new RemoveFriendTask();
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
    }

    @Override
    public void addFriend(String url) {
        AddFriendTask task = new AddFriendTask();
        task.execute(url);
        mPageAdapter.notifyDataSetChanged();
    }

    /**
     * A private inner class for performing asynchronus adding of new groups
     */
    private class AddFriendTask extends AsyncTask<String, Void, String> {
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

    private class RemoveFriendTask extends AsyncTask<String, Void, String> {
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
                    response = "Unable to remove friend, Reason: " + e.getMessage();
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
                    Toast.makeText(getApplicationContext(), "Friend removed successfully!", Toast.LENGTH_LONG).show();
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

    private class SetAvailableTask extends AsyncTask<String, Void, String> {
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
                    response = "Unable to set availability, Reason: " + e.getMessage();
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
                    myUser.toggleMyAvailable();
                    mSharedPreferences.edit().putBoolean("available", myUser.getMyAvailable())
                        .commit();
                    setFAB(myUser.getMyAvailable());
                    mPageAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to set Availability: "+
                            jsonObject.get("error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong with the data "+
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * A private inner class for performing asynchronus adding of new groups
     */
    private class AddGroupTask extends AsyncTask<String, Void, String> {
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
                    response = "Unable to add group, Reason: " + e.getMessage();
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
                    Toast.makeText(getApplicationContext(), "Group added successfully!", Toast.LENGTH_LONG).show();
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

    /**
     *This class extends Fragment Pager adapter which helps control the tab layout contents
     */
    private class MainActivityPagerAdapater extends FragmentPagerAdapter {

        public MainActivityPagerAdapater(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return new GroupListFragment();
                case 1:
                    return new FriendListFragment();
//                case 2:
//                    return new BlankFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getItemPosition(Object item){
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0:
                    return "Groups";
                case 1:
                    return "Friends";
//                case 2:
//                    return "Tab 2";
                default:
                    return null;
            }
        }

    }

}
