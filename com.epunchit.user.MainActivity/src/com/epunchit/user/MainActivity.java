package com.epunchit.user;

import static com.epunchit.Constants.DEBUG;
import static com.epunchit.Constants.EP_QRCODE_REDEEM_PATH;
import static com.epunchit.Constants.EP_QRCODE_UPDATE_PATH;
import static com.epunchit.Constants.EP_USER_PROFILE_PATH;
import static com.epunchit.Constants.EP_USER_REDEEM_PLACES_PATH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.epunchit.Constants;
import com.epunchit.utils.LauncherUtils;
import com.epunchit.utils.Utils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;
import com.urbanairship.Logger;

public class MainActivity extends ListActivity {
	UserPlacesResultReceiver userPlacesResultReceiver	= null;
	UserProfileResultReceiver userProfileResultReceiver	= null;
	UserUpdateResultReceiver userUpdateResultReceiver = null;
    private ProgressDialog progressBar;
    private final String TAG = "MainActivity";
	private boolean TOGGLEON = false;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        TextView codeButton = (TextView)findViewById(R.id.redeemCode);
        codeButton.setText("Redeem Code");
        codeButton.setTextColor(getResources().getColor(R.color.epunchit_blue));
    	if(!Utils.isUserSignedIn(this) || !Utils.isThereInternetConnection(getApplicationContext()))
    	{
        	LauncherUtils.launchActivity(R.layout.login, this, null);
        	return;
        }
    	//Utils.init(this);
    	getUserData();
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
		progressBar = ProgressDialog.show(this, "Retrieving data", "Please wait...");
    	getUserData();
    }
    
    @Override
    protected void onDestroy() {
    super.onDestroy();

    unbindDrawables(findViewById(R.id.main));
    System.gc();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
        view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
        ((ViewGroup) view).removeAllViews();
        }
    }
    
    public void ScanButtonHandler(View view) {
    	IntentIntegrator qrScanner = new IntentIntegrator(this);
    	qrScanner.setTitle("Installing QR code reader");
    	qrScanner.setMessage("Please select yes to get the QR code reader...");
    	qrScanner.initiateScan();
    }
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
            	 IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            	 if (scanResult != null) {
            		 String content = scanResult.getContents();
            		 
            		 String format = scanResult.getFormatName();
            		 
            		 Map<String,String> contents = parseContent(content);
            		 confirmActionDialog("Content Being Sent",contents,content);
            	 }            	
            } else if (resultCode == RESULT_CANCELED) {
                //handle cancel
            }
        }
    }
    
    
    
    public HashMap<String,String> parseContent(String content){ 
    	String[] strPairs = content.split("&");
    	HashMap<String,String> contents = new HashMap<String,String>();
    	
    	
    	for(String kPair: strPairs){ 
    		
    		
    		
    		String[] kv = kPair.split("=");
 		   String key = kv[0];
 		   String value = kv[1];
 		   if(key.equals("place"))
 			   contents.put("place", value);
 		   if(key.equalsIgnoreCase("point"))
 			   contents.put("point", value);
 		   if(key.equalsIgnoreCase("type"))
 			   contents.put("type",value);
 		   if(key.equalsIgnoreCase("places"))
 			   contents.put("places", value);
    		
    		
    	}
    	
    	return contents;
    	
    	
    }
    
    
    public void confirmActionDialog(final String title,final Map<String,String> contents,final String content)
	{
    	
    	
    	StringBuilder strB = new StringBuilder();
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		//String text = "Please confirm:"+content;
		String place = "Place: " + contents.get("place") + "\n";
		String points;
		String msg;
		if(contents.get("type") != null && contents.get("type").equalsIgnoreCase("redeem")){ 
			msg = "You are going to use a Redeem would you like to continue?";
			points = "<br /><font color='#90EE90'>*Please make sure to show cashier that Redeem was successful.</font>\n";
		}else { 
			msg = "Do you want to add point";
			points = "Points: " + contents.get("point") + "\n";
		}
		
		
		
		strB.append(msg + points);
		alert.setTitle(title);
		alert.setMessage(Html.fromHtml(strB.toString()));
		Log.d(TAG,strB.toString());
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
       		 updateUserInfo(content);			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		alert.show();
	}
    
    public void confirmActionDialog(final String title,final String content)
	{
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		String text = "Please confirm:"+content;
		alert.setTitle(title);
		alert.setMessage(text);
		Log.d(TAG,content);
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
       		 updateUserInfo(content);			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		alert.show();
	}

    // updates the user infromation 
    public void updateUserInfo(String data)
    {
	    try {
	    	String place = "";
	    	String points = "";
	    	String redeem = "";
	    	String[] kvPairs = data.split("&");
	    	for(String kvPair: kvPairs) 
	    	{
	    		   String[] kv = kvPair.split("=");
	    		   String key = kv[0];
	    		   String value = kv[1];
	    		   if(key.equals("place"))
	    			   place = value;
	    		   if(key.equalsIgnoreCase("point"))
	    			   points = value;
	    		   if(key.equalsIgnoreCase("type"))
	    			   redeem = value;
	    		   if(key.equalsIgnoreCase("places"))
	    			   place=value;
	    	}
	    	
 	        Bundle params = new Bundle();
	    	
	    	
	    	params.putString("type", redeem);
 	    	//TODO After code is being used delete code from SharedPreference a look for a new code.
	    	Intent intent = new Intent(this, RESTClientService.class);
	    	Uri updatepath = null;
	    	if(redeem.equalsIgnoreCase("redeem")){ 
	    		updatepath = Uri.parse(EP_QRCODE_REDEEM_PATH); 
	    		params.putString("code", Utils.getRedeemCode
	    				(this));
	    		params.putString("userid", Utils.getAccountName(this));
	    		params.putString("place", place);
	    		Log.d("REDEEM:","Inside the redeem");
	    		Log.d("Code:",Utils.getRedeemCode(this));
	    	}else{ 
	    		updatepath = Uri.parse(EP_QRCODE_UPDATE_PATH);
	    		params.putString("uid", Utils.getAccountName(this));
	    		params.putString("point", points);
	    		params.putString("place", place);
	    	}
	        intent.setData(updatepath);
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	    	userUpdateResultReceiver = new UserUpdateResultReceiver(new Handler());
	    	userUpdateResultReceiver.setParentContext(this);
	    	intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER, userUpdateResultReceiver);
	        intent.putExtra(RESTClientService.EXTRA_HTTP_VERB, RESTClientService.GET);
	        startService(intent);  
		} catch (Exception e) {
			if(Constants.DEBUG)
				Log.e("MainActivity","error updating qr input:"+e.getMessage());
		}	

    }

    
    public void PlacesButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.placesview, this, null);    	
    }
    
    public void HomeButtonHandler(View view) {
    }
    
    public void SettingsButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.settingsview, this, null);    	
    }
    
    public void onSignOutHandler(View view)
    {
    	Utils.signOutUser(this);
    	LauncherUtils.launchActivity(R.layout.login, this, null);    	
    }
    
    
    public void getRedeemCodeHandler(View view) {
    	String redeemCode = Utils.getRedeemCode(this);
    	Button txtView = (Button) view;
    	txtView.setClickable(true);
    	if(TOGGLEON){ 
    		txtView.setText(redeemCode);
    		txtView.setTextColor(Color.RED);
    		
    		TOGGLEON = false;
    	}else { 
    		
    		txtView.setText("Redeem Code");
    		txtView.setTextColor(getResources().getColor(R.color.epunchit_blue));
    		TOGGLEON = true;
    	}
    	
    	
    	
		
    }
    
    private void getUserData()
    {
	    try {
	    	getUserProfile();
	    	getRedeemList();
 		} catch (Exception e) {
			if(Constants.DEBUG)
				Log.e("MainActivity:getUserData",e.getMessage());
		}	

    }
    
    private void getUserProfile()
    {
	    try {
 	        Bundle params = new Bundle();
	    	params.putString("user", Utils.getAccountName(this));
	    	Intent intent = new Intent(this, RESTClientService.class);
	        intent.setData(Uri.parse(EP_USER_PROFILE_PATH));
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	    	userProfileResultReceiver = new UserProfileResultReceiver(new Handler());
	    	userProfileResultReceiver.setParentContext(this);
	    	intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER, userProfileResultReceiver);
	        intent.putExtra(RESTClientService.EXTRA_HTTP_VERB, RESTClientService.GET);
	        this.startService(intent);            
		} catch (Exception e) {
			if(Constants.DEBUG)
				Log.e("MainActivity:getUserProfile",e.getMessage());
		}	
    }

    private void getRedeemList()
    {
	    try {
 	        Bundle params = new Bundle();
	    	params.putString("user", Utils.getAccountName(this));
	    	Intent intent = new Intent(this, RESTClientService.class);
	        intent.setData(Uri.parse(EP_USER_REDEEM_PLACES_PATH));
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	    	userPlacesResultReceiver = new UserPlacesResultReceiver(new Handler());
	    	userPlacesResultReceiver.setParentContext(this);
	    	intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER, userPlacesResultReceiver);
	        intent.putExtra(RESTClientService.EXTRA_HTTP_VERB, RESTClientService.GET);
	        this.startService(intent);            
		} catch (Exception e) {
			if(Constants.DEBUG)
				Log.e("MainActivity:getRedeemList",e.getMessage());
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
		
	}

	public class UserUpdateResultReceiver extends ResultReceiver {

        private Context context = null;

        protected void setParentContext (Context context) {
            this.context = context;
        }

        public UserUpdateResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult (int resultCode, Bundle resultData) {
        	
   			if(resultData != null)
   			{
   				Log.d("MaintActivtiy:","Inside receive");
   				String responseString = resultData.getString(RESTClientService.REST_RESPONSE); 
   				Log.d("MainActivity:",responseString);
   	        	try {
					JSONObject json = new JSONObject(responseString);
					String success = json.getString("Success");
					if(success.equalsIgnoreCase("true")){
						
						String type = json.getString("type");
						if(type.equalsIgnoreCase("redeem")){ 
							String body = getString(R.string.redeem_success);
							
							getUserData();
							confirmationRedeemDialog("Redeem Success", body);
						}else { 
							getUserData();
							confirmationDialog("Success","Your points have been added");
						}
						
   	        	}else {
						String errormsg = json.getString("Error");
						 Log.d("UserUpdateResultReciver:error msg:", errormsg);
						confirmationDialog("Error- Program Error",errormsg);
						 
					}
				} catch (JSONException e) {
					Log.d(TAG, e.getMessage());
					confirmationDialog("Error","Field Does not Exist");
				}
   			}
        }
    }

	
	
	
	public void confirmationRedeemDialog(String title,String bodymsg){ 
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(title);
		alert.setIcon(android.R.drawable.ic_dialog_info);
		alert.setMessage(Html.fromHtml(bodymsg));
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
	        	onStart();
       		 }
		});
		alert.show();
		
		
		
	}
	
	
    public void confirmationDialog(final String text,final String message)
	{
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		String title = text;
		alert.setTitle(title);
		alert.setIcon(android.R.drawable.ic_dialog_info);
		alert.setMessage(message);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
	        	onStart();
       		 }
		});
		alert.show();
	}

	
    public class UserPlacesResultReceiver extends ResultReceiver {

        private Context context = null;

        protected void setParentContext (Context context) {
            this.context = context;
        }

        public UserPlacesResultReceiver(Handler handler) {
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
   					JSONArray places = (JSONArray) placesObj.get("redeemPlaces");
   					List<EPPlace> epPlaceList = Utils.JSONArrayToPlaceList(places);
   					if(epPlaceList != null)
   					{
   						MainActivity view = (MainActivity)context;
   						view.setListAdapter(new EPPlaceArrayAdapter(context, 
   							R.layout.epredeemplacelistitem,
   							epPlaceList));
   					} else {
  						if(DEBUG)
   							Log.d("userRedeemPlaces"," null  list");
   						
   					}

   				} catch(Exception ex)
   				{
   					if(Constants.DEBUG)
   						ex.printStackTrace();
   				}
   			}	
        }
    }

	public class UserProfileResultReceiver extends ResultReceiver {

        private Context context = null;

        protected void setParentContext (Context context) {
            this.context = context;
        }

        public UserProfileResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult (int resultCode, Bundle resultData) {
   			String responseString = null;
   			if(resultData != null)
   			{
   				responseString = resultData.getString(RESTClientService.REST_RESPONSE); 
   				try {
   					JSONObject userProfile = new JSONObject(responseString);
   					int points = Integer.valueOf(userProfile.getString("points"));
   		        	initPoints(points);
   		        	TextView txtView = (TextView) findViewById(R.id.userName);
  					txtView.setText(userProfile.getString("first")+" "+userProfile.getString("last"));  					
   		        	
  					txtView = (TextView) findViewById(R.id.availableRedeems);
  					txtView.setText("Available Redeems:"+userProfile.getString("rewards"));  					
  					txtView = (TextView) findViewById(R.id.mostVisited);
  					txtView.setText("Most Visited:"+userProfile.getString("mostVisited"));
  					String redeemCode = userProfile.getString("redeemCode");
  					txtView = (TextView) findViewById(R.id.lastRedeem);
  					txtView.setText("Last Redeem: " + userProfile.getString("lastRedeem") );
  					Button btnView = (Button) findViewById(R.id.redeemCode);
  					if(!redeemCode.equalsIgnoreCase("0"))
  					{
  						
  						btnView.setVisibility(Button.VISIBLE);
  						btnView.setClickable(true);
  						Utils.setRedeemCode(context, redeemCode);
  					}else{ 
  						btnView.setVisibility(Button.VISIBLE);
  						btnView.setClickable(true);
  						Utils.setRedeemCode(context, redeemCode);
  					}
  					    		        	
    			} catch(Exception ex)
   				{
   					if(Constants.DEBUG)
   						Log.d("MainActivity","getUserPlaces JSON exception:"+ex.getMessage());
   				}
   			}	
        }
        
        protected void initPoints(int points)
        {
        	if(points > 10)
        		points = 10;
        	ImageView imageView = null;
        	for(int i = 0; i < points; i++)
        	{
        		imageView = (ImageView) findViewById(getImageResourceId(i));
        		imageView.setVisibility(ImageView.VISIBLE);
        	}
        }
        
        protected int getImageResourceId(int index)
        {
        	switch(index)
        	{
        		case 0: return R.id.punch1;
        		case 1: return R.id.punch2;
        		case 2: return R.id.punch3;
        		case 3: return R.id.punch4;
        		case 4: return R.id.punch5;
        		case 5: return R.id.punch6;
        		case 6: return R.id.punch7;
        		case 7: return R.id.punch8;
        		case 8: return R.id.punch9;
        		case 9: return R.id.punch10;
        	}
        	return 0;
        }
    }
	
	 
}