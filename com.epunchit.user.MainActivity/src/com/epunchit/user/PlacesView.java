package com.epunchit.user;

import static com.epunchit.Constants.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.epunchit.Constants;
import com.epunchit.user.AddPlacesView.AddPlaceResultReceiver;
import com.epunchit.utils.LauncherUtils;
import com.epunchit.utils.Utils;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlacesView extends ListActivity  {
	
	PlacesSearchResultReceiver placesSearchResultReceiver	= null;
    private ProgressDialog progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.placesview);
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
        getEPunchItPlaces();
    }

    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// Get the item that was clicked
		EPPlace o = (EPPlace) this.getListAdapter().getItem(position);
		String keyword = o.getUrl();
    	List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
      	NameValuePair nvPair = new BasicNameValuePair("placeURL",keyword);
    	nvPairs.add(nvPair);
    	LauncherUtils.launchActivity(R.layout.epplacedetailsview, this, nvPairs);    	
	}
     
    public void AddPlaceButtonHandler(View view) {
    	//onSearchRequested();
    	List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
      	NameValuePair nvPair = new BasicNameValuePair("nosearch","true");
    	nvPairs.add(nvPair);
    	LauncherUtils.launchActivity(R.layout.addplacesview, this, nvPairs);    	
    	
    }
    
    public void PlacesButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.placesview, this, null);    	
    }
    
    public void HomeButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.main, this, null);    	
    }
    
    public void FavoritesButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.favoriteplacesview, this, null);    	
    }

    
    public void followPlaceRequest(String placeURL, String action)
    {
	    try {
 	        Bundle params = new Bundle();
	    	params.putString("place_url", placeURL);
	    	params.putString("action", action);
 	    	params.putString("user", Utils.getAccountName(this));
	    	Intent intent = new Intent(this, RESTClientService.class);
	        intent.setData(Uri.parse(EP_USER_FOLLOW_PLACE_PATH));
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	        intent.putExtra(RESTClientService.EXTRA_HTTP_VERB, RESTClientService.GET);
	        startService(intent);  
		} catch (Exception e) {
			if(Constants.DEBUG)
				Log.e("PlacesView","error un/following place:"+e.getMessage());
		}	

    }

    
	public void getEPunchItPlaces() {
	    try {
			progressBar = ProgressDialog.show(this, "Nearby places", "Please wait...");

 	        Bundle params = new Bundle();
	    	params.putString("user", Utils.getAccountName(this));
	    	Intent intent = new Intent(this, RESTClientService.class);
 	       	Location curLoc = Utils.getLastKnownLocation(this);
 	       	double dLat = 0, dLng = 0;
 	       	if(curLoc != null)
 	       	{
 	       		dLat = curLoc.getLatitude();
 	       		dLng = curLoc.getLongitude();
	       	} 
 	       	if(DEBUG)
 	       		Log.d("getEPlaces","lat,lng="+dLat+","+dLng);
	       	params.putString("lat", String.valueOf(dLat));
 	       	params.putString("lng", String.valueOf(dLng));
		    intent.setData(Uri.parse(EP_PLACES_NEARBY_PATH));
	    	
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	    	placesSearchResultReceiver = new PlacesSearchResultReceiver(new Handler());
	    	placesSearchResultReceiver.setParentContext(this);
	    	intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER, placesSearchResultReceiver);
	        intent.putExtra(RESTClientService.EXTRA_HTTP_VERB, RESTClientService.GET);
	        this.startService(intent);            
		} catch (Exception e) {
			if(Constants.DEBUG)
				Log.e("PlacesView",e.getMessage());
		}	
	}

	public class PlacesSearchResultReceiver extends ResultReceiver {

        private Context context = null;

        protected void setParentContext (Context context) {
            this.context = context;
        }

        public PlacesSearchResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult (int resultCode, Bundle resultData) {
   			String responseString = null;
   			if (progressBar.isShowing()) {
   	            progressBar.dismiss();
   	        }

   			if(resultData != null)
   			{
   				responseString = resultData.getString(RESTClientService.REST_RESPONSE); 
   				try {
  					JSONObject placesObj = new JSONObject(responseString);
   					JSONArray places = (JSONArray) placesObj.get("placesNearby");
   					List<EPPlace> epPlaceList = Utils.JSONArrayToPlaceList(places);
   					if(epPlaceList != null)
   					{
   						PlacesView view = (PlacesView)context;
   						view.setListAdapter(new EPPlaceArrayAdapter(context, 
   							R.layout.epplacesviewlistitem,
   							epPlaceList));
   					} else {
  						if(DEBUG)
   							Log.d("getNearbyPlaces"," null  list");
   						
   					}

   				} catch(Exception ex)
   				{
   					if(Constants.DEBUG)
   					{
   						ex.printStackTrace();
   					}
   				}
   			}	
        }
    }

}
