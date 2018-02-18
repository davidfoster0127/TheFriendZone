package team16.uw.tacoma.edu.thefriendzone.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import team16.uw.tacoma.edu.thefriendzone.R;

/**
 * Created by David on 5/26/2017.
 */

public class FriendZoneDBHelper extends SQLiteOpenHelper {

    private static FriendZoneDBHelper sInstance;

    private static final String DATABASE_NAME = "_450bteam16";
    private static final int DATABASE_VERSION = 1;
    private final String CREATE_FRIENDS_SQL;
    private final String DROP_FRIENDS_SQL;
    private final String CREATE_GROUPS_SQL;
    private final String DROP_GROUPS_SQL;

    public static synchronized FriendZoneDBHelper getInstance(Context context) {
        if(sInstance == null){
            sInstance = new FriendZoneDBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private FriendZoneDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        CREATE_FRIENDS_SQL = context.getString(R.string.CREATE_FRIENDS_SQL);
        DROP_FRIENDS_SQL = context.getString(R.string.DROP_FRIENDS_SQL);
        CREATE_GROUPS_SQL = context.getString(R.string.CREATE_GROUPS_SQL);
        DROP_GROUPS_SQL = context.getString(R.string.DROP_GROUPS_SQL);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //initialize the database with the sql string
        db.execSQL(CREATE_FRIENDS_SQL);
        db.execSQL(CREATE_GROUPS_SQL);
    }


    public void eraseAndInitialize(SQLiteDatabase db){
        db.execSQL(DROP_FRIENDS_SQL);
        db.execSQL(DROP_GROUPS_SQL);
        onCreate(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //for refershing the db essentially
        //first execute the delete entries sql string
        db.execSQL(DROP_FRIENDS_SQL);
        db.execSQL(DROP_GROUPS_SQL);
        //then recreate the db
        onCreate(db);
    }
}
