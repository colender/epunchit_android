package com.epunchit.user;

import static com.epunchit.Constants.BASE_IMAGE_PATH;
import static com.epunchit.Constants.EP_PLACE_DETAILS_PATH;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.epunchit.Constants;
import com.epunchit.utils.LauncherUtils;
import com.epunchit.utils.TargetLocationOverlay;
import com.epunchit.utils.Utils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class EPPlaceDetailsView extends FragmentActivity {
	static final int FB_POST_REQ_CODE = 999;
	MapView mapView = null;
	PlaceDetailsResultReceiver placeDetailsResultReceiver	= null;
	String placeURL = null;
	static String eventStartTime = null;
	static String eventEndTime = null;
	static String eventDate = null;
	static FragmentActivity mActivity = null;
	static String placeName = null;
	static String placeFace = null;
	static String placeAddress = null;
	static String placeFaceUrl = null;
	static String placeLat = null;
	static String placeLng = null;
	static String placePhone = null;
	static Calendar cal = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.epplacedetailsview);
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	/*
       	Location curLoc = Utils.getLastKnownLocation(this);
       	double dLat = curLoc.getLatitude();
       	double dLng = curLoc.getLongitude();
        if(mapView == null)
           mapView = (MapView) findViewById(R.id.mapview);
        GeoPoint p = new GeoPoint((int)(dLat * 1E6),(int)(dLng * 1E6));
        MapController mapControl = mapView.getController();
        mapControl.setCenter(p);
        mapControl.setZoom(15);
        mapView.setBuiltInZoomControls(true);
		mapView = setTargetMarker(mapView, p);
		*/
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            	placeURL = extras.getString("placeURL");
        }
 		getPlaceDetails();
    }
    
    public void getPlaceDetails()
    {
	    try {
 	        Bundle params = new Bundle();
	    	params.putString("search", placeURL);
	    	params.putString("user", Utils.getAccountName(this));
	    	Intent intent = new Intent(this, RESTClientService.class);
	        intent.setData(Uri.parse(EP_PLACE_DETAILS_PATH));
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	    	placeDetailsResultReceiver = new PlaceDetailsResultReceiver(new Handler());
	    	placeDetailsResultReceiver.setParentContext(this);
	    	intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER, placeDetailsResultReceiver);
	        intent.putExtra(RESTClientService.EXTRA_HTTP_VERB, RESTClientService.GET);
	        this.startService(intent);            
		} catch (Exception e) {
			if(Constants.DEBUG)
				Log.e("EPPlaceDetails:getPlaceDetails",e.getMessage());
		}	

    }
    
	public class PlaceDetailsResultReceiver extends ResultReceiver {

        private Context context = null;

        protected void setParentContext (Context context) {
            this.context = context;
        }

        public PlaceDetailsResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult (int resultCode, Bundle resultData) {
   			String responseString = null;
   			if(resultData != null)
   			{
   				responseString = resultData.getString(RESTClientService.REST_RESPONSE); 
   				
   				try {
   					JSONObject placeProfile = new JSONObject(responseString);
   					String placeURL = placeProfile.getString("placeURL");
   	   				placeName = 	placeProfile.getString("placeName");
   	   				String placeDesc = placeProfile.getString(	"placeDescription");
   	   				//String placeCity = placeProfile.getString("placeCity");
   	   				//String placeState = placeProfile.getString("placeState");
   	   				//String placeZip = placeProfile.getString("placeZip");
   	   				placeFace = placeProfile.getString("placeFace");
   	   				placeAddress = placeProfile.getString("placeFormatted");
   	   				placeLat = placeProfile.getString("placeLat");
   	   				placeLng = placeProfile.getString(	"placeLng");
   	   				placePhone = placeProfile.getString("placePhone");
   	   				String placeFollowing = placeProfile.getString("placeFollowing");
   					TextView txtView = (TextView) findViewById(R.id.placeName);
   		        	txtView.setText(placeName);
  					txtView = (TextView) findViewById(R.id.placeDesc);
  					txtView.setText(placeDesc);  					
  					txtView = (TextView) findViewById(R.id.placeAddress);
  					txtView.setText(placeAddress);  					
  					txtView = (TextView) findViewById(R.id.placePhone);
  					txtView.setText(placePhone);  					
 					txtView = (TextView) findViewById(R.id.placeFollowing);
  					txtView.setText(placeFollowing + " following");  	
   	   				txtView = (TextView) findViewById(R.id.placeReviews);
   	   				if(placeProfile.has("placeYelp"))
   	   				{
   	   	   				String placeYelpDesc = placeProfile.getString("placeYelp");
   	   	   				if(placeYelpDesc != null && !(placeYelpDesc == ""))
   	   	   				{
   	   	   					LinearLayout layout = (LinearLayout) findViewById(R.id.placeReviewsLayout);
   	   	   					layout.setVisibility(LinearLayout.VISIBLE);
   	   	   					txtView = (TextView) findViewById(R.id.placeReviews);
   	   	   					txtView.setText(placeYelpDesc);  
   	   	   				} 
   	   				}
   	   				if(placeProfile.has("placeSpecials"))
   	   				{
   	   	   				String placeSpecials = placeProfile.getString("placeSpecials");
   	   	   				if(placeSpecials != null && !(placeSpecials == ""))
   	   	   				{
   	   	   					LinearLayout layout = (LinearLayout) findViewById(R.id.placeSpecialsLayout);
   	   	   					layout.setVisibility(LinearLayout.VISIBLE);
   	   	   					txtView = (TextView) findViewById(R.id.placeSpecials);
   							txtView.setText(placeSpecials);  
   	   	   				}
   	   				}
  					EPPlaceDetailsView view = (EPPlaceDetailsView)context;
  					
  					ImageView logoView = (ImageView) findViewById(R.id.placeLogo);
  		        	placeFaceUrl = BASE_IMAGE_PATH + placeFace;
  		        	if(Constants.DEBUG)
  		        		Log.d("PlaceDetailsView", "logo = "+placeFaceUrl);
  		        	Bitmap bitmap = Utils.getBitmapFromURL(placeFaceUrl, context);
  		        	if(bitmap != null)
  		        		logoView.setImageBitmap(bitmap);
/*  					
  			       GeoPoint p = new GeoPoint((int)(Double.parseDouble(placeLat) * 1E6),
  			    		   (int)(Double.parseDouble(placeLng) * 1E6));
  					view.setTargetMarker(mapView, p);
*/  					
  					    		        	
    			} catch(Exception ex)
   				{
   					if(Constants.DEBUG)
   						Log.d("MainActivity","getUserPlaces JSON exception:"+ex.getMessage());
   				}
   			}	
        }
	}

	public MapView setTargetMarker(MapView mapView, GeoPoint p)
	{
        MyLocationOverlay myLocOverlay = new MyLocationOverlay(this, mapView);
        myLocOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocOverlay);
        
        Drawable marker=getResources().getDrawable(android.R.drawable.star_big_on);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);
    
        TargetLocationOverlay targetOverlay = new TargetLocationOverlay (marker);
        mapView.getOverlays().add(targetOverlay);
      targetOverlay.addItem(p, "Target", "Target");
        mapView.getOverlays().add(targetOverlay);
        return mapView;
	}

    public void ShowDirectionsHandler(View view) {
    	if(placeLng != null && placeLat != null)
    	{
    		/*
    		List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
    		NameValuePair nvPair = new BasicNameValuePair("lat",placeLat);
    		nvPairs.add(nvPair);
    		nvPair = new BasicNameValuePair("lng",placeLng);
    		nvPairs.add(nvPair);  		
    		LauncherUtils.launchActivity(GoogleMapActivity.class, this, nvPairs); 
    		*/
    		showDrivingDirections();
    		
    	}
    }
    
    public void showNavigationView()
    {
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse("google.navigation:q="+placeAddress));		
		startActivity(intent);
    }
    
    public void showDrivingDirections() {
    	Location myLoc = Utils.getLastKnownLocation(this);
    	
    	String urlStr = "http://maps.google.com/maps?" +
		"saddr=" +
		myLoc.getLatitude()+"," + myLoc.getLongitude()+
		"&daddr=" +
		placeLat+","+placeLng +
		"&view=map";
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,Uri.parse(urlStr));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");		
		startActivity(intent);
    }
    
    public void PhoneCallHandler(View view)
    {
    	if(placePhone != null)
    		LauncherUtils.launchPhoneDialer(this, placePhone);
    }
	
    public void MenuButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.placeordermenu1, this, null);    	
    }
	
	
    public void FollowPlaceHandler(View view) {
    	Toast.makeText(this, "Coming soon!", Toast.LENGTH_LONG).show();
    }
    
    public void PlacesButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.placesview, this, null);    	
    }
    
    public void HomeButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.main, this, null);    	
    }
     
    public void InviteButtonHandler(View view) {
    	DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

	public static class TimePickerFragment extends DialogFragment
    implements TimePickerDialog.OnTimeSetListener {

		@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
				final Calendar c = Calendar.getInstance();
				int hour = c.get(Calendar.HOUR_OF_DAY);
				int minute = c.get(Calendar.MINUTE);

				// Create a new instance of TimePickerDialog and return it
				return new TimePickerDialog(getActivity(), this, hour, minute,
						DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			String unixStartTime = null;
			String unixEndTime = null;

			
			if(cal != null) {
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
						cal.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
			}
			unixStartTime = String.valueOf(cal.getTimeInMillis() / 1000L);
			if(hourOfDay>12)
		        eventStartTime = String.valueOf(hourOfDay-12)+ ":"+(String.valueOf(minute)+"pm");
		    if(hourOfDay==12)
		        eventStartTime = "12"+ ":"+(String.valueOf(minute)+"pm");
		    if(hourOfDay<12)
		        eventStartTime = String.valueOf(hourOfDay)+ ":"+(String.valueOf(minute)+"am");

		    if(cal != null)
		    {
		    	cal.add(Calendar.HOUR, 1);
				unixEndTime = String.valueOf(cal.getTimeInMillis() / 1000L);
		    	hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
		    	if(hourOfDay>12)
		    		eventEndTime = String.valueOf(hourOfDay-12)+ ":"+(String.valueOf(minute)+"pm");
		    	if(hourOfDay==12)
		    		eventEndTime = "12"+ ":"+(String.valueOf(minute)+"pm");
		    	if(hourOfDay<12)
		    		eventEndTime = String.valueOf(hourOfDay)+ ":"+(String.valueOf(minute)+"am");
		    }
		    
		    List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
			String facebookMessage = "Invites you to "+placeName + " Date:"+eventDate+" Time:"+eventStartTime;
			nvPairs.add(new BasicNameValuePair("eventPlace",placeName));
			nvPairs.add(new BasicNameValuePair("eventDate",eventDate));
			nvPairs.add(new BasicNameValuePair("eventStartTime",eventStartTime));
			nvPairs.add(new BasicNameValuePair("eventEndTime",eventEndTime));
			nvPairs.add(new BasicNameValuePair("unixStartTime",unixStartTime));
			nvPairs.add(new BasicNameValuePair("unixEndTime",unixEndTime));
			nvPairs.add(new BasicNameValuePair("eventLocation",placeAddress));
			nvPairs.add(new BasicNameValuePair("eventPlaceImgURL",placeFaceUrl));
	    	LauncherUtils.launchActivityForResult(FacebookActivity.class, mActivity, nvPairs, FB_POST_REQ_CODE);    	
		}
	}

	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == FB_POST_REQ_CODE) {
            if (resultCode == RESULT_OK) {
            	Toast.makeText(this, "Invitation sent", Toast.LENGTH_LONG);
            } else {
            	Toast.makeText(this, "Invitation not sent", Toast.LENGTH_LONG);
            }
        }
    }
	public static class DatePickerFragment extends DialogFragment
    implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// 	Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// 	Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			eventDate = month+1 + "/" + day + "/" + year;
			cal = Calendar.getInstance();
			cal.set(year, month, day);
	    	DialogFragment newFragment = new TimePickerFragment();
	        newFragment.show(mActivity.getSupportFragmentManager(), "timePicker");
		}
	}


}