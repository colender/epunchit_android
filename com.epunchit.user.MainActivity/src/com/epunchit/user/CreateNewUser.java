package com.epunchit.user;

import static com.epunchit.Constants.CREATE_NEW_USER_PATH;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.epunchit.Constants;
import com.epunchit.utils.LauncherUtils;
import com.epunchit.utils.Utils;

public class CreateNewUser extends FragmentActivity {
	LoginResultReceiver loginResultReceiver	= null;
	private final String TAG = "CreateNewUser: ";
	public static String userName;
	public static String password;
	public static String firstName;
	public static String lastName;
	public static String errorText;
	public static String phone;
	public static String bdate;
	public static String strState;
	public static String strCity;
	public static String strGender;
	public static String passwordCon;
	public static EditText pw, un, fn, ln, ph, city, state, bday,pwc;
	RadioGroup gender;
	TextView error;
    Button ok;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createnewuser);
        un=(EditText)findViewById(R.id.txtUserName);
        fn=(EditText)findViewById(R.id.txtFirstName);
        ln=(EditText)findViewById(R.id.txtLastName);
        pw=(EditText)findViewById(R.id.txtPassword);
        pwc = (EditText)findViewById(R.id.txtPasswordComfirm);
        ok=(Button)findViewById(R.id.btn_save);
        error=(TextView)findViewById(R.id.txtError);
        ph = (EditText)findViewById(R.id.txtPhone);
        /*state = (EditText)findViewById(R.id.txtState);
        city = (EditText) findViewById(R.id.txtCity);
        gender = (RadioGroup) findViewById(R.id.genderGrp);
        bday = (EditText) findViewById(R.id.txtBday);
        
        bday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDateDialog();
			}
		});*/
        
        pwc.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){ 
					String passCon = pwc.getText().toString();
					String pass = pw.getText().toString();
					if(!passCon.equals(pass)){ 
						pwc.setError("Does not match with password please make sure they are the same");
						Log.d(TAG,"Focus Out");
					}
					
					
				}
				
			}
		});
        
        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
              
            	userName = un.getText().toString();
            	
            	String name = fn.getText().toString();
            	String[] splitName = name.split(" ");
            	
            	firstName = fn.getText().toString();
            	lastName = ln.getText().toString();
            	password = pw.getText().toString();
            	passwordCon = pwc.getText().toString();
            	phone = ph.getText().toString();
            /*	bdate = bday.getText().toString();
            	strState = state.getText().toString();
            	strCity = city.getText().toString();*/
            	
               	/*int genderId = ((RadioGroup) findViewById(R.id.genderGrp)).getCheckedRadioButtonId();
            	if(genderId == R.id.radio_male)
            		strGender = "male";
            	else if(genderId == R.id.radio_female)
            		strGender = "female";
            	else strGender = "";
            	*/
            	
            	/*if(strGender == null || strGender == "")
            	{
            		error.setText("Please select the gender field. Thank you.");
            		return;
            	}
            	*/
            	
            	
            	if(userName == null || userName.equalsIgnoreCase("")){
            		un.setError("You must enter your email.");
            		error.setText("You must enter your email.");
            	
            		return;
            	}
            	if(phone == null || phone == "")
            	{
            		ph.setError("Please enter valid phone number with area code.Thank you!");
            		error.setText("Please enter valid phone number with area code. Thank you.");
            		return;
            	}
            	if(!Utils.validatePhone(phone))
            	{
            		ph.setError("Phone number format must be 5555555555 or 555-555-5555. Thank you.");
            		error.setText("Phone number format must be 5555555555 or 555-555-5555. Thank you.");
            		return;
            	}
            	
            	if(!passwordCon.equalsIgnoreCase(password)){ 
            		error.setText("Passwords do not Match");
            		
            		return;
            	}
            	saveSettings();
            }
        });
    }
    
    public void showDateDialog()
    {
    	DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
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
			bdate = month+1 + "/" + day + "/" + year;
			bday.setText(bdate);
		}
	}
	public void saveSettings() {
	    try {
 	        Bundle params = new Bundle();
	    	params.putString("user", userName);
	    	params.putString("first", firstName);
	    	params.putString("last", lastName);
	    	params.putString("password", password);
	    	params.putString("phone", phone);
	    /*	params.putString("birthday", bdate);
	    	params.putString("state", strState);
	    	params.putString("city", strCity);
	    	params.putString("gender", strGender);*/
	    	
	    	final Intent intent = new Intent(this, RESTClientService.class);
	        intent.setData(Uri.parse(CREATE_NEW_USER_PATH));
	        intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
	    	loginResultReceiver = new LoginResultReceiver(new Handler());
	    	loginResultReceiver.setParentContext(this);
	    	intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER, loginResultReceiver);
	        intent.putExtra(RESTClientService.EXTRA_HTTP_VERB, RESTClientService.GET);
	        startService(intent);            
		} catch (Exception e) {
			ok.setEnabled(true);
	    	error.setText("Error creating new user. Please try again.");
			if(Constants.DEBUG)
				e.printStackTrace();
		}	
	}
	

	public class LoginResultReceiver extends ResultReceiver {

        private Context context = null;
        private CreateNewUser activity = null;

        protected void setParentContext (Context context) {
            this.context = context;
            this.activity = (CreateNewUser) context;
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
   					JSONObject json;
   					try {
						json = new JSONObject(responseString);
						String success = json.getString("Success");
						if(success.equalsIgnoreCase("true"))
		            	{
							Toast.makeText(context, "Signup success. Confirmation email sent.", Toast.LENGTH_LONG).show();
		            	    error.setText("Signup success. The confirmation email has been sent to your inbox. Thank you for signing up with ePunchIT rewards.");
		            	    LauncherUtils.launchActivity(R.layout.login, context, null);
		            	}else
		            	{
		            		
		            		String errorStr = json.getString("Error");
		            		
		            		error.setText(errorStr);
		            	}
					} catch (JSONException e) {
						Log.d(TAG,e.getMessage());
					}
   					
            	   
   				} else {
        	    	error.setText("Error creating new user. Please try again.");
   				}
   			} else {
    	    	error.setText("Error creating new user. Please try again.");
   			}
        }
    }

}

