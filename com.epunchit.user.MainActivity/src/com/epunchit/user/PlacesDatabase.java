package com.epunchit.user;

import com.epunchit.Constants;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;

/**
 * Database to store and retrieve places of received links.
 */
public class PlacesDatabase {
    public static final int ID_INDEX = 0;
    public static final int EP_PLACE_INDEX = 1;
    public static final int EP_PLACE_ICON = 2;
    public static final int EP_PLACE_LAT = 3;
    public static final int EP_PLACE_LNG = 4;

    public static final String DATABASE_NAME = "epunchit.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "places";
    private static final String _ID = "_id";
    private static final String COL_PLACE = "placeURL";
    private static final String COL_ICON = "icon";
    private static final String COL_LAT = "lat";
    private static final String COL_LNG = "lng";

    private static final String[] ALL_COLUMNS =
        new String[] { _ID, COL_PLACE, COL_ICON, COL_LAT, COL_LNG};

    private final SQLiteDatabase mDb;
    private SQLiteStatement mInsertStatement;
    private SQLiteStatement mDeleteStatement;
    private SQLiteStatement mDeleteAllStatement;

    private static PlacesDatabase mSingleton;

    private PlacesDatabase(Context context) {
        mDb = new DatabaseHelper(context).getWritableDatabase();
    }

    public synchronized static PlacesDatabase get(Context context) {
        if (mSingleton == null) {
            mSingleton = new PlacesDatabase(context);
        }
        return mSingleton;
    }

    public int insertPlaces(String placeURL, byte[]icon, String lat, String lng) 
    {
    	int ret = -1;
    	try {
    		if (mInsertStatement == null) {
    			mInsertStatement = mDb.compileStatement("INSERT INTO "+ TABLE_NAME +
                    "(" +            		
                    COL_PLACE + ", " +
                    COL_ICON + ", " +
                    COL_LAT + ", " +
                    COL_LNG +
                    ") VALUES (?, ?, ?, ?)");
    		}
    		mInsertStatement.bindString(1, placeURL);
    		mInsertStatement.bindBlob(2, icon);
    		mInsertStatement.bindString(3, lat);
    		mInsertStatement.bindString(4, lng);
       	    mInsertStatement.execute();
    		ret = 1;
    	} catch(Exception ex) {
    		if(Constants.DEBUG)
    			Log.e("PlacesDatabase", "Insert Alert Places error"+ex.getMessage());
    	}
    	return ret;
    }

    public Cursor lookupPlaces() {
        return mDb.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null,null);
    }

    public byte[] lookupIcon(String placeURL) {
    	String[] args = new String[]{placeURL};
    	byte[] icon = null;
    	try {
    		Cursor cursor = mDb.query(TABLE_NAME, ALL_COLUMNS, COL_PLACE, args, null, null, null);
    		icon = cursor.getBlob(EP_PLACE_ICON);
    	} catch(Exception ex)
    	{
    		if(Constants.DEBUG)
    			ex.printStackTrace();
    	}
    	return icon;
    }

    
    public void deletePlaces(String place) {
        if (mDeleteStatement == null) {
            mDeleteStatement = mDb.compileStatement("DELETE FROM "+ TABLE_NAME +
                                                    " WHERE " + COL_PLACE + " == ?");
        }
        mDeleteStatement.bindString(1, place);
        mDeleteStatement.execute();
    }

    public void deleteAll() {
        if (mDeleteAllStatement == null) {
            mDeleteAllStatement = mDb.compileStatement("DELETE FROM "+ TABLE_NAME);
        }
        mDeleteAllStatement.execute();
    }

    /**
     * Database helper that creates and maintains the SQLite database.
     */
    static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createPlacesTable(db);
        }
        
        private void createPlacesTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "+ TABLE_NAME + "(" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PLACE + " TEXT NOT NULL, " +
                    COL_ICON + " BLOB NOT NULL, " +
                    COL_LAT + " TEXT NOT NULL, " +
                    COL_LNG + " TEXT NOT NULL)");
        }        

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Implement the upgrade path to databases with DATABASE_VERSION > 1 here
        }
    }
	public static void insertPlace(Context context, String placeURL, byte[] icon, String lat, String lng)
	{
        try {
            Bundle params = new Bundle();
        	params.putString("placeURL",placeURL);
            params.putString("lat",lat);
            params.putString("lng",lng);
        	params.putByteArray("icon", icon);
        	final Intent intent = new Intent(context, DatabaseIntentService.class);
            intent.putExtra(DatabaseIntentService.EXTRA_PARAMS, params);
            context.startService(intent);            
		} catch (Exception e) {
			if(Constants.DEBUG)
				Log.e("PlacesDatabase:insert",e.getMessage());
		}
	}

}
