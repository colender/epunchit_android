package com.epunchit.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.epunchit.Constants;
import com.epunchit.user.MainActivity.UserPlacesResultReceiver;
import com.epunchit.utils.LauncherUtils;
import com.epunchit.utils.Utils;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.json.JsonHttpParser;
import static com.epunchit.Constants.*;

public class AddPlacesView extends ListActivity {
	
	AddPlaceResultReceiver addPlaceResultReceiver	= null;

	// The different Places API endpoints.
	private static final String PLACES_SEARCH_URL =  "https://maps.googleapis.com/maps/api/place/search/json?";
	private static final String PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
	private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
	private static final HttpTransport transport = new NetHttpTransport();
    private ProgressDialog progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        Bundle extras = getIntent().getExtras();
        String nosearch = null;
        if(extras != null) {
            	nosearch = extras.getString("nosearch");
        }

		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		String searchKeywords = "restaurant|food|cafe|meal_delivery|meal_takeaway|bakery";
        setContentView(R.layout.addplacesview);
        if(nosearch == null)
        {
        	if (Intent.ACTION_SEARCH.equals(queryAction)) {
        		searchKeywords = queryIntent.getStringExtra(SearchManager.QUERY);
        	}
        }
//		List<Place> placeList = getPlaceList(searchKeywords, "3");
		progressBar = ProgressDialog.show(this, "Nearby places", "Please wait...");
		List<Place> placeList = getPlaceList(searchKeywords, null);
		if (progressBar.isShowing()) {
            progressBar.dismiss();
        }
		setListAdapter(new AddPlacesArrayAdapter(this, 
				R.layout.addplacesviewlistitem,
				placeList));
	}
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// Get the item that was clicked
		Place o = (Place) this.getListAdapter().getItem(position);
		showAddPlaceDialog(o.getName(), o.getReference());
	}
    
    private void showAddPlaceDialog(final String placeName, final String reference)
    {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		String title = "Would you like to add "+placeName+"?";
		alert.setTitle(title);
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				addPlaceRequest(reference);
			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		alert.show();
    }
    
    private void addPlaceRequest(String reference)
    {
	    try {
 	        Bundle params = new Bundle();
	    	params.putString("place_reference", reference);
	    	params.putString("action", "add");
 	    	params.putString("user", Utils.getAccountName(this));
	    	Intent intent = new Intent(this, RESTClientService.class);
	        intent.setData(Uri.parse(EP_USER_SUGGEST_PLACE_PATH));
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	    	addPlaceResultReceiver = new AddPlaceResultReceiver(new Handler());
	    	addPlaceResultReceiver.setParentContext(this);
	    	intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER, addPlaceResultReceiver);
	        this.startService(intent);  
		} catch (Exception e) {
			if(Constants.DEBUG)
				Log.e("addPlacesView","error adding place:"+e.getMessage());
		}	

    }

	public class AddPlaceResultReceiver extends ResultReceiver {

        private Context context = null;

        protected void setParentContext (Context context) {
            this.context = context;
        }

        public AddPlaceResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult (int resultCode, Bundle resultData) {
				if(resultCode == HttpStatusCodes.STATUS_CODE_OK)
					{
	  					Toast.makeText(context, "Add place request completed. Thank you.", Toast.LENGTH_LONG)
	  				.show();
					} 
				if(resultData != null)
				{
	   				String responseString = resultData.getString(RESTClientService.REST_RESPONSE);
	   				if(DEBUG)
	   				{
	   					Log.d("AddPlaceReceiver","result="+responseString);
	   				}
				}
        }
    }

    public void ScanButtonHandler(View view) {
 
    }
    

    public void SettingsButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.settingsview, this, null);    	
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
    
    protected List<Place> getPlaceList(String category, String radius) {
    	List<Place> list = null;
    	Location location = Utils.getLastKnownLocation(this);
    	if(location == null)
    		return null;
    	try {
			HttpRequestFactory httpRequestFactory = Utils.createRequestFactory(transport);
			HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
			request.getUrl().put("key", API_KEY);
			request.getUrl().put("location",location.getLatitude() + "," + location.getLongitude());
			if(radius != null) {
				radius = String.valueOf((Integer.parseInt(radius)) * 610); //miles to meters
				request.getUrl().put("radius", radius);
			} else {
				request.getUrl().put("rankby", "distance");
			}
			request.getUrl().put("sensor", "false");
			if(category != null)
				request.getUrl().put("types", category.toLowerCase());
			PlacesList places = request.execute().parseAs(PlacesList.class);
			return places.results;
		} catch (HttpResponseException e) {
		} catch (IOException e) {
		}    	
    	return list;
    }
 
  /*
  public void performAutoComplete() throws Exception {
		try {
			System.out.println("Perform Autocomplete ....");
			System.out.println("-------------------------");

			HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
			HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_AUTOCOMPLETE_URL));
			request.url.put("key", API_KEY);
			request.url.put("input", "mos");
			request.url.put("location", latitude + "," + longitude);
			request.url.put("radius", 500);
			request.url.put("sensor", "false");
			PlacesAutocompleteList places = request.execute().parseAs(PlacesAutocompleteList.class);
			if (PRINT_AS_STRING) {
				System.out.println(request.execute().parseAsString());
			} else {
				for (PlaceAutoComplete place : places.predictions) {
					System.out.println(place);
				}
			}

		} catch (HttpResponseException e) {
			System.err.println(e.response.parseAsString());
			throw e;
		}
	}
	*/	    

    
}