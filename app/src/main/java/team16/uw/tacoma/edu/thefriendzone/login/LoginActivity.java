package team16.uw.tacoma.edu.thefriendzone.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import team16.uw.tacoma.edu.thefriendzone.MainActivity;
import team16.uw.tacoma.edu.thefriendzone.R;
import team16.uw.tacoma.edu.thefriendzone.login.RegisterFragment.AddUserListener;

/**
 * The activity responsible for all login and register actions.
 * Implements child fragment interfaces:
 * {@link AddUserListener}
 * {@link LoginFragment.SignInListener}
 */
public class LoginActivity extends AppCompatActivity
    implements AddUserListener, LoginFragment.SignInListener {

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSharedPreferences = getSharedPreferences(getString(R.string.LOGIN_PREFS)
                , Context.MODE_PRIVATE);

        if (!mSharedPreferences.getBoolean(getString(R.string.LOGGEDIN), false)) {
            if (savedInstanceState != null) {
                return;
            }
            LoginFragment firstFragment = new LoginFragment();
            firstFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        } else {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    /**
     * Opens a new Register Fragment when corresponding button is pressed.
     *
     * @param view is the current view.
     */
    public void onRegister(View view) {
        RegisterFragment newFragment = new RegisterFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, newFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Attempts to open an {@link LoginActivity.RegisterTask}
     * and add a user, and displays any errors that occur.
     *
     * @param url is the given url that the RegisterTask will execute.
     */
    @Override
    public void addUser(String url) {
        RegisterTask task = new RegisterTask();
        task.execute(url.toString());
        getSupportFragmentManager().popBackStackImmediate();
    }

    /**
     * Attempts to open an {@link LoginActivity.LoginTask}
     * and add a user, and displays any errors that occur.
     *
     * @param url is the given url that the LoginTask will execute.
     */
    @Override
    public void signIn(String url) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            LoginTask task = new LoginTask();
            task.execute(url.toString());
        } else {
            Toast.makeText(this, "No network connection available. Cannot authenticate user",
                    Toast.LENGTH_SHORT) .show();
            return;
        }
    }

    /**
     * Called when a {@link LoginActivity.LoginTask} is successful.
     * Opens a new {@link MainActivity}
     */
    private void signInSuccessful() {
        String email = ((EditText) findViewById(R.id.login_email)).getText().toString();

        mSharedPreferences
                .edit()
                .putBoolean(getString(R.string.LOGGEDIN), true)
                .putString("email", email)
                .commit();

        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    /**
     *
     */
    private class RegisterTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();
                    InputStream content = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    response = "Unable to register, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // Something wrong with the network or the URL.
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = (String) jsonObject.get("result");
                if (status.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Successfully created account!",
                            Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to create account: "
                                    + jsonObject.get("error"),
                            Toast.LENGTH_LONG)
                            .show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something wrong with the data" +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();
                    InputStream content = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    Log.v("LoginTask", "Unable to Login, Reason: "
                            + e.getMessage());
                    response = "Unable to login, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = (String) jsonObject.get("result");
                if (status.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Login Successful!"
                            , Toast.LENGTH_SHORT)
                            .show();

                    Boolean available;
                    available = jsonObject.get("available").equals("1");

                    mSharedPreferences.edit().putString("username",
                            (String) jsonObject.get("username"))
                            .putBoolean("available", available)
                            .commit();
                    signInSuccessful();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to Login: "
                                    + jsonObject.get("error")
                            , Toast.LENGTH_LONG)
                            .show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something wrong with the data " +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
