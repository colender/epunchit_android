package com.epunchit.user;

import static com.epunchit.Constants.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;


import com.epunchit.Constants;
import com.epunchit.utils.LauncherUtils;
import com.epunchit.utils.Utils;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsView extends Activity {
	LoginResultReceiver loginResultReceiver	= null;
	public static String userName;
	public static String password;
	public static String oldPwd;
	public static String errorText;
	public static String strState;
	public static String strCity;
	
	EditText opw,pw, state, city;
	TextView error;
    Button ok;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsview);
        opw=(EditText)findViewById(R.id.txtOldPassword);
        pw=(EditText)findViewById(R.id.txtPassword);
        ok=(Button)findViewById(R.id.btn_save);
        error=(TextView)findViewById(R.id.txtError);
        city=(EditText)findViewById(R.id.txtCity);
        state=(EditText)findViewById(R.id.txtState);

        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	ok.setEnabled(false);
            	oldPwd = opw.getText().toString();
            	password = pw.getText().toString();
            	strState = state.getText().toString();
            	strCity = city.getText().toString();
            	saveSettings();
            }
        });
    }
    
	public void saveSettings() {
	    try {
 	        Bundle params = new Bundle();
	    	params.putString("user", userName);
	    	params.putString("password", oldPwd);
	    	params.putString("newpass", password);
	    	params.putString("city", strCity);
	    	params.putString("state", strState);
	    	final Intent intent = new Intent(this, RESTClientService.class);
	        intent.setData(Uri.parse(CHANGE_PASSWORD_PATH));
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	    	loginResultReceiver = new LoginResultReceiver(new Handler());
	    	loginResultReceiver.setParentContext(this);
	    	intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER, loginResultReceiver);
	        intent.putExtra(RESTClientService.EXTRA_HTTP_VERB, RESTClientService.GET);
	        startService(intent);            
		} catch (Exception e) {
			if(Constants.DEBUG)
				Log.e("LoginActivity",e.getMessage());
		}	
	}
	
	public void processSettings()
	{
		Utils.setAccountInfo(this, userName, password, false);
		LauncherUtils.launchActivity(R.layout.main, this, null);
	}

	public class LoginResultReceiver extends ResultReceiver {

        private Context context = null;
        private SettingsView activity = null;

        protected void setParentContext (Context context) {
            this.context = context;
            this.activity = (SettingsView) context;
        }

        public LoginResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult (int resultCode, Bundle resultData) {
        	ok.setEnabled(true);
        	String responseString = null;
   			if(resultData != null)
   			{
   				responseString = resultData.getString(RESTClientService.REST_RESPONSE); 
   				if(responseString != null)
   				{
            	    if(responseString.equalsIgnoreCase("yes"))
            	    {
            	    	error.setText("Success!");
            	    	activity.processSettings();
            	    }
            	    else
            	    {
            	    	error.setText("Incorrect password. Password not changed");
            	    }
   				} else {
        	    	error.setText("Internal error. Password not changed");
   				}
   			} else {
    	    	error.setText("Internal error. Password not changed");
   			}
        }
    }

}

