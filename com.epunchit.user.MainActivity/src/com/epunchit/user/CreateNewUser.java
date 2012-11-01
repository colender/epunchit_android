package com.epunchit.user;

import static com.epunchit.Constants.*;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;


import com.epunchit.Constants;
import com.epunchit.user.EPPlaceDetailsView.DatePickerFragment;
import com.epunchit.user.EPPlaceDetailsView.TimePickerFragment;
import com.epunchit.utils.LauncherUtils;
import com.epunchit.utils.Utils;

import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class CreateNewUser extends FragmentActivity {
	LoginResultReceiver loginResultReceiver	= null;
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
	
	public static EditText pw, un, fn, ln, ph, city, state, bday;
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
        ok=(Button)findViewById(R.id.btn_save);
        error=(TextView)findViewById(R.id.txtError);
        ph = (EditText)findViewById(R.id.txtPhone);
        state = (EditText)findViewById(R.id.txtState);
        city = (EditText) findViewById(R.id.txtCity);
        gender = (RadioGroup) findViewById(R.id.genderGrp);
        bday = (EditText) findViewById(R.id.txtBday);
        
        bday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDateDialog();
			}
		});
        
        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	ok.setEnabled(false);
            	userName = un.getText().toString();
            	firstName = fn.getText().toString();
            	lastName = ln.getText().toString();
            	password = pw.getText().toString();
            	phone = ph.getText().toString();
            	bdate = bday.getText().toString();
            	strState = state.getText().toString();
            	strCity = city.getText().toString();
            	
               	int genderId = ((RadioGroup) findViewById(R.id.genderGrp)).getCheckedRadioButtonId();
            	if(genderId == R.id.radio_male)
            		strGender = "male";
            	else if(genderId == R.id.radio_female)
            		strGender = "female";
            	else strGender = "";
            	
            	
            	if(strGender == null || strGender == "")
            	{
            		error.setText("Please select the gender field. Thank you.");
            		return;
            	}
            	
            	if(phone == null || phone == "")
            	{
            		error.setText("Please enter valid phone number with area code. Thank you.");
            		return;
            	}
            	if(!Utils.validatePhone(phone))
            	{
            		error.setText("Phone number format must be 5555555555 or 555-555-5555. Thank you.");
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
	    	params.putString("birthday", bdate);
	    	params.putString("state", strState);
	    	params.putString("city", strCity);
	    	params.putString("gender", strGender);
	    	
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
            	    if(responseString.equalsIgnoreCase("1"))
            	    {
            	    	Toast.makeText(context, "Signup success. Confirmation email sent.", Toast.LENGTH_LONG);
            	    	error.setText("Signup success. The confirmation email has been sent to your inbox. Thank you for signing up with ePunchIT rewards.");
            	    	LauncherUtils.launchActivity(R.layout.login, context, null);
            	    }
            	    else
            	    {
            	    	error.setText("Error creating new user. Please try again.");
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

