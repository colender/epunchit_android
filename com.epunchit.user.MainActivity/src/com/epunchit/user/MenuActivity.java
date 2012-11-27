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

public class MenuActivity extends Activity {
	public static String errorText;
    Button ok;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.placeordermenu1);
    }
    
    public void SelectButtonHandler(View view)
    {
    	LauncherUtils.launchActivity(R.layout.placeordermenu2, this, null);
    }
    
    public void HomeButtonHandler(View view) {
    	LauncherUtils.launchActivity(R.layout.main, this, null);    	
    }
    public void ScanButtonHandler(View view) {
    	Toast.makeText(this, "Coming soon!", Toast.LENGTH_LONG).show();
    }
    public void BackButtonHandler(View view) {
    	finish();
    }

}

