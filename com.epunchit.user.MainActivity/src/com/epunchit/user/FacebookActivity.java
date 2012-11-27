/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epunchit.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.epunchit.user.SessionEvents.AuthListener;
import com.epunchit.user.SessionEvents.LogoutListener;
import com.epunchit.utils.LauncherUtils;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

import com.facebook.android.Util;


public class FacebookActivity extends Activity {

    public static final String APP_ID = "222561211100812";
    public static final String[] FACEBOOK_PERMISSIONS = {"user_events","create_event","publish_stream"};
    protected static String GRAPH_BASE_URL =
        "https://graph.facebook.com/";
    
    private LoginButton mLoginButton;
    private TextView mPlaceTxt;
    private TextView mDateTxt;
    private TextView mStartTimeTxt;
    private TextView mEndTimeTxt;
    private TextView mLocTxt;
    private Button mRequestButton;
    private Button mPostButton;
    private Button mDeleteButton;
    private Button mUploadButton;
	private String eventStartTime;
	private String eventEndTime;
	private String unixStartTime;
	private String unixEndTime;
	private String eventDate;
	private String eventPlace;
	private String eventLocation;
	private String placeImageUrl;
	private static Activity mActivity;
    private Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APP_ID == null) {
            Util.showAlert(this, "Warning", "Facebook Applicaton ID Error!");
            return;
        }
        mActivity = this;
        setContentView(R.layout.facebookactivity);
        mLoginButton = (LoginButton) findViewById(R.id.login);
        mPlaceTxt = (TextView) FacebookActivity.this.findViewById(R.id.eventPlace);
        mDateTxt = (TextView) FacebookActivity.this.findViewById(R.id.eventDate);
        mStartTimeTxt = (TextView) FacebookActivity.this.findViewById(R.id.eventStartTime);
        mEndTimeTxt = (TextView) FacebookActivity.this.findViewById(R.id.eventEndTime);
        mLocTxt = (TextView) FacebookActivity.this.findViewById(R.id.eventLocation);
        mPostButton = (Button) findViewById(R.id.postButton);
        
		eventStartTime = getIntent().getStringExtra("eventStartTime");
		eventEndTime = getIntent().getStringExtra("eventEndTime");
		eventDate = getIntent().getStringExtra("eventDate");
		eventLocation = getIntent().getStringExtra("eventLocation");
		eventPlace = getIntent().getStringExtra("eventPlace");
		placeImageUrl = getIntent().getStringExtra("eventPlaceImgURL");
		unixStartTime = getIntent().getStringExtra("unixStartTime");
		unixEndTime = getIntent().getStringExtra("unixEndTime");
		
       	mFacebook = new Facebook(APP_ID);
       	mAsyncRunner = new AsyncFacebookRunner(mFacebook);
        SessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());
        mLoginButton.init(this, mFacebook, FACEBOOK_PERMISSIONS);
        mPostButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	inviteFriendsConfirmDialog();
            	//mFacebook.dialog(FacebookActivity.this, "feed",
                //       new SampleDialogListener());
            }
        });
        if(mFacebook.isSessionValid())
        {
        	mPostButton.setVisibility(View.VISIBLE);
            mPlaceTxt.setVisibility(TextView.VISIBLE);
            mDateTxt.setVisibility(TextView.VISIBLE);
            mStartTimeTxt.setVisibility(TextView.VISIBLE);
            mLocTxt.setVisibility(TextView.VISIBLE);
            mEndTimeTxt.setVisibility(TextView.VISIBLE);
            mPlaceTxt.setText("Place:"+eventPlace);
            mDateTxt.setText("Date:"+eventDate);
            mStartTimeTxt.setText("Start Time:"+eventStartTime);
            mLocTxt.setText("Location:"+eventLocation);
            mEndTimeTxt.setText("End Time:"+eventEndTime);
        } else {
        	mPostButton.setVisibility(View.INVISIBLE);
            mPlaceTxt.setVisibility(TextView.INVISIBLE);
            mDateTxt.setVisibility(TextView.INVISIBLE);
            mStartTimeTxt.setVisibility(TextView.INVISIBLE);
            mLocTxt.setVisibility(TextView.INVISIBLE);
            mEndTimeTxt.setVisibility(TextView.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mFacebook.authorizeCallback(requestCode, resultCode, data);
    }

    public class SampleAuthListener implements AuthListener {
        public void onAuthSucceed() {
            mPostButton.setVisibility(View.VISIBLE);
            mPlaceTxt.setVisibility(TextView.VISIBLE);
            mDateTxt.setVisibility(TextView.VISIBLE);
            mStartTimeTxt.setVisibility(TextView.VISIBLE);
            mLocTxt.setVisibility(TextView.VISIBLE);
            mEndTimeTxt.setVisibility(TextView.VISIBLE);
            mPlaceTxt.setText("Place:"+eventPlace);
            mDateTxt.setText("Date:"+eventDate);
            mStartTimeTxt.setText("Start Time:"+eventStartTime);
            mLocTxt.setText("Location:"+eventLocation);
            mEndTimeTxt.setText("End Time:"+eventEndTime);
        }

        public void onAuthFail(String error) {
        	mActivity.finish();
        }
    }

    public class SampleLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
        }

        public void onLogoutFinish() {
        	mActivity.finish();
        }
    }
    
    public class WallPostResponseListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            Log.d("WallPostResponseListener", "response: " + response);
            String postId = null;
            try {
                JSONObject json = Util.parseJson(response);
                postId = json.getString("id");
//                mAsyncRunner.request(postId, new WallPostRequestListener());
            } catch (JSONException e) {
                Log.w("WallPostResponseListener"," JSON Error in response"+e.getMessage());
            } catch (FacebookError e) {
                Log.w("WallPostResponseListener", "Facebook Error: " + e.getMessage());
            }
            if(postId != null) {
                FacebookActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                    	setResult(RESULT_OK);
                        mActivity.finish();
                    }
                });
            }
        }
    }

    
    public class WallPostRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            Log.d("FacebookActivity", "Got response: " + response);
            String message = "<empty>";
            try {
                JSONObject json = Util.parseJson(response);
                message = json.getString("message");
            } catch (JSONException e) {
                Log.w("WallPostRequestListener"," JSON Error in response"+e.getMessage());
            } catch (FacebookError e) {
                Log.w("WallPostRequestListener", "Facebook Error: " + e.getMessage());
            }
            final String text = "Your Wall Post: " + message;
            FacebookActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                }
            });
        }
    }

    public class WallPostDeleteListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            if (response.equals("true")) {
                Log.d("Facebook-Example", "Successfully deleted wall post");
                FacebookActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mDeleteButton.setVisibility(View.INVISIBLE);
//                        mText.setText("Deleted Wall Post");
                    }
                });
            } else {
                Log.d("Facebook-Example", "Could not delete wall post");
            }
        }
    }

    public class SampleDialogListener extends BaseDialogListener {
        public void onComplete(Bundle values) {
            final String postId = values.getString("post_id");
            if (postId != null) {
                Log.d("Facebook-Example", "Dialog Success! post_id=" + postId);
                mAsyncRunner.request(postId, new WallPostRequestListener());
            } else {
                Log.d("Facebook-Example", "No wall post made");
            }
        }
    }

    public void postOnWall() {
         try {
        	 SessionStore.restore(mFacebook, mActivity);
             Bundle params = new Bundle();
             String message = "Join me at "+eventPlace+" Date:"+eventDate+" Time:"+eventStartTime+" Location:"+eventLocation;
             params.putString("message", message);
             params.putString("description", message);
             params.putString("link","https://epunchit.com/");
             //params.putString("picture", placeImageUrl);
             //params.putString("place", eventPlace);
             params.putString("app_id", APP_ID);
             mAsyncRunner.request("me/feed", params, "POST", new WallPostResponseListener(), null);
         } catch(Exception e) {
             e.printStackTrace();
         }
    }

    public void postUserEvent() {
        try {
       	 SessionStore.restore(mFacebook, mActivity);
            Bundle params = new Bundle();
            params.putString("app_id", APP_ID);
            params.putString("start_time", unixStartTime);
            params.putString("name", "You're invited to "+eventPlace);
            params.putString("location", eventLocation);
            params.putString("end_time", unixEndTime);
//            ImageLoader imageLoader = new ImageLoader(mActivity);
  //          params.putByteArray("picture", imageLoader.getImageByteArray(placeImageUrl));
            String message = "Join me at "+eventPlace+" Date:"+eventDate+" Time:"+eventStartTime+" Location:"+eventLocation;
            params.putString("description", message);
            params.putString("privacy", "OPEN");
            mAsyncRunner.request("me/events", params, "POST", new WallPostResponseListener(), null);
        } catch(Exception e) {
            e.printStackTrace();
        }
   }

    public void inviteFriendsConfirmDialog()
	{
		final AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
		String title = "Are you sure you like to send this invite?";
		alert.setTitle(title);
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
            	postOnWall();
            	postUserEvent();
			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		alert.show();
	}

}

/*
        mUploadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Bundle params = new Bundle();
                params.putString("method", "photos.upload");

                URL uploadFileUrl = null;
                try {
                    uploadFileUrl = new URL(
                        "http://www.facebook.com/images/devsite/iphone_connect_btn.jpg");
                } catch (MalformedURLException e) {
                	e.printStackTrace();
                }
                try {
                    HttpURLConnection conn= (HttpURLConnection)uploadFileUrl.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    int length = conn.getContentLength();

                    byte[] imgData =new byte[length];
                    InputStream is = conn.getInputStream();
                    is.read(imgData);
                    params.putByteArray("picture", imgData);

                } catch  (IOException e) {
                    e.printStackTrace();
                }

                mAsyncRunner.request(null, params, "POST",
                        new SampleUploadListener(), null);
            }
        });
*/
/*
mRequestButton.setOnClickListener(new OnClickListener() {
    public void onClick(View v) {
    	mAsyncRunner.request("me", new SampleRequestListener());
    }
});

mRequestButton.setVisibility(mFacebook.isSessionValid() ?
        View.VISIBLE :
        View.INVISIBLE);
        

mUploadButton.setVisibility(mFacebook.isSessionValid() ?
        View.VISIBLE :
        View.INVISIBLE);
*/

/* 
    public class SampleRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            try {
                // process the response here: executed in background thread
                Log.d("FacebookActivity", "Response: " + response.toString());
                JSONObject json = Util.parseJson(response);
                final String name = json.getString("name");

                // then post the processed result back to the UI thread
                // if we do not do this, an runtime exception will be generated
                // e.g. "CalledFromWrongThreadException: Only the original
                // thread that created a view hierarchy can touch its views."
                FacebookActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mText.setText("Hello there, " + name + "!");
                    }
                });
            } catch (JSONException e) {
                Log.w("FacebookActivity", "JSON Error in response");
            } catch (FacebookError e) {
                Log.w("FacebookActivity", "Facebook Error: " + e.getMessage());
            }
        }
    }

    public class SampleUploadListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            try {
                // process the response here: (executed in background thread)
                Log.d("FacebookActivity", "Response: " + response.toString());
                JSONObject json = Util.parseJson(response);
                final String src = json.getString("src");

                // then post the processed result back to the UI thread
                // if we do not do this, an runtime exception will be generated
                // e.g. "CalledFromWrongThreadException: Only the original
                // thread that created a view hierarchy can touch its views."
                FacebookActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mText.setText("Hello there, photo has been uploaded at \n" + src);
                    }
                });
            } catch (JSONException e) {
                Log.w("FacebookActivity", "JSON Error in response");
            } catch (FacebookError e) {
                Log.w("FacebookActivity", "Facebook Error: " + e.getMessage());
            }
        }
    }

 *
 */
