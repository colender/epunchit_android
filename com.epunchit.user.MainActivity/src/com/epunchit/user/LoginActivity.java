package com.epunchit.user;

import static com.epunchit.Constants.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.epunchit.Constants;
import com.epunchit.utils.LauncherUtils;
import com.epunchit.utils.Prefs;
import com.epunchit.utils.Utils;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	LoginResultReceiver loginResultReceiver	= null;
	EmailPasswordResultReceiver emailPasswordResultReceiver = null;
	public static String userName;
	public static String password;
	public static String errorText;
	public static boolean remember;
	
	EditText un,pw;
	TextView error;
    Button ok;
    CheckBox chk;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        un=(EditText)findViewById(R.id.txtUserName);
        pw=(EditText)findViewById(R.id.txtPassword);
        ok=(Button)findViewById(R.id.btn_login);
        error=(TextView)findViewById(R.id.txtError);
        chk = (CheckBox)findViewById(R.id.chkRememberMe);
        chk.setChecked(true);
        if(Utils.isRememberMe(this))
        {
        	un.setText(Utils.getAccountName(this));
        	pw.setText(Utils.getPassword(this));
        }

        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	ok.setEnabled(false);
            	userName = un.getText().toString();
            	password = pw.getText().toString();
            	remember = chk.isChecked();
            	
            	validateUser();
            }
        });
    }
    
    public void createNewUserHandler(View view)
    {
    	LauncherUtils.launchActivity(R.layout.createnewuser, this, null);
    }

    public void forgotPasswordHandler(View view)
    {
    	userName = un.getText().toString();
    	if(userName == null || userName == "")
    	{
        	error.setText("Please enter your email address. Thank you.");
        	return;
    	}
    	emailUserPassword();
    }
    
	public void emailUserPassword() {
	    try {
 	        Bundle params = new Bundle();
	    	params.putString("user", userName);
	    	final Intent intent = new Intent(this, RESTClientService.class);
	        intent.setData(Uri.parse(PASSWORD_RESET_EMAIL_PATH));
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	    	emailPasswordResultReceiver = new EmailPasswordResultReceiver(new Handler());
	    	emailPasswordResultReceiver.setParentContext(this);
	    	intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER, emailPasswordResultReceiver);
	        intent.putExtra(RESTClientService.EXTRA_HTTP_VERB, RESTClientService.GET);
	        startService(intent);            
		} catch (Exception e) {
	    	error.setText("Could not connect. Please try again.");
		}	
	}
    

	public void validateUser() {
	    try {
 	        Bundle params = new Bundle();
	    	params.putString("user", userName);
	    	params.putString("password", password);
	    	final Intent intent = new Intent(this, RESTClientService.class);
	        intent.setData(Uri.parse(LOGIN_PATH));
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	    	loginResultReceiver = new LoginResultReceiver(new Handler());
	    	loginResultReceiver.setParentContext(this);
	    	intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER, loginResultReceiver);
	        intent.putExtra(RESTClientService.EXTRA_HTTP_VERB, RESTClientService.GET);
	        startService(intent);            
		} catch (Exception e) {
	    	error.setText("Could not connect. Please try again.");
        	ok.setEnabled(true);
		}	
	}
	
	public void processValidUser()
	{
		Utils.setAccountInfo(this, userName, password, remember);
		LauncherUtils.launchActivity(R.layout.main, this, null);
	}

	public void processInvalidUser()
	{
		Utils.signOutUser(this);
	}

	
	public class LoginResultReceiver extends ResultReceiver {

        private Context context = null;
        private LoginActivity activity = null;

        protected void setParentContext (Context context) {
            this.context = context;
            this.activity = (LoginActivity) context;
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
            	    	activity.processValidUser();
            	    	error.setText("Success!");
            	    }
            	    else
            	    {
            	    	activity.processInvalidUser();
            	    	error.setText("Sorry!! Incorrect Username or Password");
            	    }
   				} else {
   			    	error.setText("Could not connect. Please try again.");
   				}
   			} else {
   		    	error.setText("Could not connect. Please try again.");
   			}
       }
    }

	public class EmailPasswordResultReceiver extends ResultReceiver {

        private Context context = null;
        private LoginActivity activity = null;

        protected void setParentContext (Context context) {
            this.context = context;
            this.activity = (LoginActivity) context;
        }

        public EmailPasswordResultReceiver(Handler handler) {
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
            	    	error.setText("Password reset instructions has been emailed to you.");
            	    }
            	    else
            	    {
            	    	error.setText("Sorry!! Incorrect email address. Please try again.");
            	    }
   				} else {
   			    	error.setText("Could not connect. Please try again.");
   				}
   			} else {
   		    	error.setText("Could not connect. Please try again.");
   			}
       }
    }
	
}

