package team16.uw.tacoma.edu.thefriendzone.item;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * A holder class for the User, that contains the given user's email and password for easy access
 * and use within the application.
 *
 * @author Matthew Subido | subidomd@uw.edu
 * @version May 2, 2017
 */

public class User implements Serializable {
    private String myUsername;
    private String myEmail;
    private Boolean myAvailable;

    /**
     * The Constructor of the User class.
     *
     * @param theUsername is the User's password.
     * @param theEmail is the User's email.
     */
    public User(final String theEmail, final String theUsername, final Boolean theAvailable) {
        myUsername = theUsername;
        myEmail = theEmail;
        myAvailable = theAvailable;
    }

    public String getMyUsername() {
        return myUsername;
    }

    public void setMyUsername(final String myPassword) {
        this.myUsername = myPassword;
    }

    public String getMyEmail() {
        return myEmail;
    }

    public void setMyEmail(final String myEmail) {
        this.myEmail = myEmail;
    }

    public Boolean getMyAvailable() { return myAvailable; }

    public void toggleMyAvailable() { myAvailable = !myAvailable; }


    /**
     * Parses the json string, returns an error message if unsuccessful
     * Returns group list if success
     * @param friendJSON
     * @return reason or null if successful
     */
    public static String parseFriendJSON(String friendJSON, List<User> friendList) {
        String reason = null;
        if(friendJSON!=null){
            try{
                JSONArray arr = new JSONArray(friendJSON);
                for (int i =0; i<arr.length();i++){
                    JSONObject obj = arr.getJSONObject(i);
                    Boolean available;
                    available = !obj.get("available").equals("0");
                    Log.v("User", obj.get("available") + " : " + available.toString());
                    User friend = new User(obj.getString("email"), obj.getString("username"),available);
                    friendList.add(friend);
                }
            }catch(JSONException e){
                reason = "Unable to parse data, Reason:"+e.getMessage();
            }
        }
        return reason;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (!obj.getClass().getSimpleName().equals("User")) {
            return false;
        }
        User user = (User) obj;

        return (myEmail.equals(user.getMyEmail()));
    }
}
