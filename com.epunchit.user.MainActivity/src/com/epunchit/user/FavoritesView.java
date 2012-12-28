package com.epunchit.user;

import static com.epunchit.Constants.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.epunchit.Constants;
import com.epunchit.utils.LauncherUtils;
import com.epunchit.utils.Utils;
import com.google.zxing.integration.android.IntentIntegrator;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

public class FavoritesView extends BaseListActivity  {
	
	PlacesSearchResultReceiver placesSearchResultReceiver	= null;
    private ProgressDialog progressBar;
    private final String TAG = "FavoriteView";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.favoriteplacesview);
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
        getEPunchItPlaces();
    }
    
    
    @Override
    protected void onDestroy() {
    super.onDestroy();

    unbindDrawables(findViewById(R.id.epfavoritesview));
    System.gc();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
        view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && view instanceof AdapterView) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
        ((ViewGroup) view).removeAllViews();
        }
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
    	if(DEBUG)
    		Log.d(TAG,"Item Clicked");
	}
     
    public void AddPlaceButtonHandler(View view) {
    	//onSearchRequested();
    	List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
      	NameValuePair nvPair = new BasicNameValuePair("nosearch","true");
    	nvPairs.add(nvPair);
    	LauncherUtils.launchActivity(R.layout.addplacesview, this, nvPairs);    	
    	
    }
    
    public void ScanButtonHandler(View view) {
    	IntentIntegrator qrScanner = new IntentIntegrator(this);
    	qrScanner.setTitle("Installing QR code reader");
    	qrScanner.setMessage("Please select yes to get the QR code reader...");
    	qrScanner.initiateScan();
    }
    
    
    public void PlacesButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.placesview, this, null);    	
    }
    
    public void HomeButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.main, this, null);    	
    }
    
    public void BackButtonHandler(View view) {
    	finish();
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
				Log.e("FavoritesView","error un/following place:"+e.getMessage());
		}	

    }

    
	public void getEPunchItPlaces() {
	    try {
			progressBar = ProgressDialog.show(this, "Retrieving your favorites.", "Please wait...");
 	        Bundle params = new Bundle();
	    	params.putString("user", Utils.getAccountName(this));
	    	Intent intent = new Intent(this, RESTClientService.class);
	        intent.setData(Uri.parse(EP_USER_FAVORITE_PLACES_PATH));
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	    	placesSearchResultReceiver = new PlacesSearchResultReceiver(new Handler());
	    	placesSearchResultReceiver.setParentContext(this);
	    	intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER, placesSearchResultReceiver);
	        intent.putExtra(RESTClientService.EXTRA_HTTP_VERB, RESTClientService.GET);
	        this.startService(intent);            
		} catch (Exception e) {
			if(Constants.DEBUG)
				Log.e("FavoritesView",e.getMessage());
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
   					JSONArray places = (JSONArray) placesObj.get("placesFollowing");
   					List<EPPlace> epPlaceList = Utils.JSONArrayToPlaceList(places);
   					if(epPlaceList != null)
   					{
   						FavoritesView view = (FavoritesView)context;
   						view.setListAdapter(new EPPlaceArrayAdapter(context, 
   							R.layout.epplacesviewlistitem,
   							epPlaceList));
   					} else {
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
