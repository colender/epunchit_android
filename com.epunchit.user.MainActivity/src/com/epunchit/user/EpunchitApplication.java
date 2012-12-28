package com.epunchit.user;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.CustomPushNotificationBuilder;
import com.urbanairship.push.PushManager;

import android.app.Application;

public class EpunchitApplication extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	     AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(this);
	     options.developmentAppKey = "slPlcM-JSdivA4wAej2jFg";
	     options.developmentAppSecret = "sEcNRebUSQSFZ0x_8VsbAw";
	     options.inProduction = false;
	     options.gcmSender = "777185617037";
	     options.transport = "gcm";
	     UAirship.takeOff(this,options);
		PushManager.enablePush();
		 CustomPushNotificationBuilder nb = new CustomPushNotificationBuilder();
		 nb.layout = R.layout.notification;
		 nb.layoutIconDrawableId = R.id.nb_icon;
		 nb.layoutSubjectId = R.id.nb_subject;
		 nb.layoutMessageId = R.id.nb_message;
		
		
		PushManager.shared().setNotificationBuilder(nb);
		//PushManager.shared().setIntentReceiver()
		
	}

}
