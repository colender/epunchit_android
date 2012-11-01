package com.epunchit.user;

import java.util.Random;

import com.epunchit.Constants;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class DatabaseIntentService extends IntentService {
    public static final String EXTRA_PARAMS          = "com.epunchit.user.EXTRA_PARAMS";
    public static final String EXTRA_RESULT_RECEIVER = "com.epunchit.user.EXTRA_RESULT_RECEIVER";
	public static final String TAG = "DatabaseIntentService";

	public DatabaseIntentService(String name) {
		super(TAG);
	}

	public DatabaseIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        Bundle params = extras.getParcelable(RESTClientService.EXTRA_PARAMS);
        
        if(params == null || params.size() <= 0)
        	return;
    	long notifyID = new Random().nextLong();
		String placeURL = params.getString("placeURL");
		byte[] icon = params.getByteArray("icon");
		String lat = params.getString("lat");
		String lng = params.getString("lng");

		try {
				int ret = PlacesDatabase.get(this).insertPlaces(placeURL, icon, lat, lng);
				if(ret == -1)
				{
					PlacesDatabase.get(this).deletePlaces(placeURL);
					ret = PlacesDatabase.get(this).insertPlaces(placeURL, icon, lat, lng);
				}
			ResultReceiver receiver = extras.getParcelable(RESTClientService.EXTRA_RESULT_RECEIVER);
			if(receiver != null)
				receiver.send(0,null);
			
		} catch(Exception ex) {
			if(Constants.DEBUG)
			  Log.e("DatabaseIntentService",ex.getMessage());
		}
	}

}
