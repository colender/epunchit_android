package com.epunchit.utils;

import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;

import com.epunchit.user.AddPlacesView;
import com.epunchit.user.CreateNewUser;
import com.epunchit.user.EPPlaceDetailsView;
import com.epunchit.user.FacebookActivity;
import com.epunchit.user.FavoritesView;
import com.epunchit.user.LoginActivity;
import com.epunchit.user.MainActivity;
import com.epunchit.user.MenuActivity;
import com.epunchit.user.PlacesView;
import com.epunchit.user.PurchaseActivity;
import com.epunchit.user.R;
import com.epunchit.user.SelectMenuActivity;
import com.epunchit.user.SettingsView;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.ClipboardManager;
/**
 * Common set of utility functions for launching apps.
 */
public class LauncherUtils {

     public static int generateNotification(Context context, 
    										String source,
    										String message,
    										Intent intent) {
       int icon = R.drawable.status_icon;
       long when = System.currentTimeMillis();
       SharedPreferences settings = Prefs.get(context);
       int notificatonID = settings.getInt("notificationID", 0); // allow multiple notifications
       int newNotificationID = ++notificatonID % 32;

       Notification notification = new Notification(icon, message, when);
       PendingIntent pendingIntent = null; 
       if(intent != null)
    	   pendingIntent = PendingIntent.getActivity(context, newNotificationID , intent, 0);
       else 
    	   pendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
   	   notification.setLatestEventInfo(context, URLDecoder.decode(source), 
   			   URLDecoder.decode(message), pendingIntent); 
       notification.flags |= Notification.DEFAULT_SOUND | Notification.FLAG_AUTO_CANCEL;
       NotificationManager nm =
               (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
       nm.notify(notificatonID, notification);
       playNotificationSound(context);

       SharedPreferences.Editor editor = settings.edit();
       editor.putInt("notificationID", newNotificationID); // allow multiple notifications
       editor.commit();
       return newNotificationID;

       /*
       // On tablets, the ticker shows the sender, the first line of the message,
       // the photo of the person and the app icon.  For our sample, we just show
       // the same icon twice.  If there is no sender, just pass an array of 1 Bitmap.
       notif.tickerTitle = from;
       notif.tickerSubtitle = message;
       notif.tickerIcons = new Bitmap[2];
       notif.tickerIcons[0] = getIconBitmap();;
       notif.tickerIcons[1] = getIconBitmap();;
       */
   }

   public static void playNotificationSound(Context context) {
       Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
       if (uri != null) {
           Ringtone rt = RingtoneManager.getRingtone(context, uri);
           if (rt != null) {
               rt.setStreamType(AudioManager.STREAM_NOTIFICATION);
               rt.play();
           }
       }
   }

   public static String parseTelephoneNumber(String sel) {
       if (sel == null || sel.length() == 0) return null;

       // Hack: Remove trailing left-to-right mark (Google Maps adds this)
       if (sel.codePointAt(sel.length() - 1) == 8206) {
           sel = sel.substring(0, sel.length() - 1);
       }

       String number = null;
       if (sel.matches("([Tt]el[:]?)?\\s?[+]?(\\(?[0-9|\\s|\\-|\\.]\\)?)+")) {
           String elements[] = sel.split("([Tt]el[:]?)");
           number = elements.length > 1 ? elements[1] : elements[0];
           number = number.replace(" ", "");

           // Remove option (0) in international numbers, e.g. +44 (0)20 ...
           if (number.matches("\\+[0-9]{2,3}\\(0\\).*")) {
               int openBracket = number.indexOf('(');
               int closeBracket = number.indexOf(')');
               number = number.substring(0, openBracket) +
                       number.substring(closeBracket + 1);
           }
       }
       return number;
   }

   public static void launchActivity(int id, Context context, List<NameValuePair> params) {
   	Class cls = null;
   	int intentFlags = 0;
   	
   	switch(id) {
		case R.layout.main:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = MainActivity.class;
   			break;
   		case R.layout.placesview:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = PlacesView.class;
   			break;
   		case R.layout.favoriteplacesview:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = FavoritesView.class;
   			break;
   		case R.layout.settingsview:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = SettingsView.class;
   			break;
  		case R.layout.login:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = LoginActivity.class;
   			break;
 		case R.layout.addplacesview:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = AddPlacesView.class;
   			break;
 		case R.layout.epplacedetailsview:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = EPPlaceDetailsView.class;
   			break;
  		case R.layout.createnewuser:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = CreateNewUser.class;
   			break;
  		case R.layout.facebookactivity:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = FacebookActivity.class;
   			break;
  		case R.layout.placeordermenu1:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = MenuActivity.class;
   			break;
  		case R.layout.placeordermenu2:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = SelectMenuActivity.class;
   			break;
  		case R.layout.placeordermenu3:
   			intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
   			cls = PurchaseActivity.class;
   			break;
     }
   			
   	if(cls != null) {
  		Intent intent = new Intent(context, cls);   		
   		if(params != null) {
   			Iterator<NameValuePair> iter = params.iterator();
   			while(iter.hasNext()) {
   				NameValuePair param = iter.next();
   	   			intent.putExtra(param.getName(),param.getValue());
   			}
   		}
   		intent.setFlags(intentFlags);
   		context.startActivity(intent);
   	} 
   }
   
   public static void launchActivityForResult(Class cls, Activity context, List<NameValuePair> params, int requestCode) {
	   	if(cls != null) {
	   		Intent intent = new Intent(context, cls);
	   		if(params != null) {
	   			Iterator<NameValuePair> iter = params.iterator();
	   			while(iter.hasNext()) {
	   				NameValuePair param = iter.next();
	   	   			intent.putExtra(param.getName(),param.getValue());
	   			}
	   		}
	   		context.startActivityForResult(intent, requestCode);
	   	} 
	}
 
   public static void launchActivity(Class cls, Activity context, List<NameValuePair> params) {
	   	int intentFlags = 0;
		intentFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;

	   if(cls != null) {
   		Intent intent = new Intent(context, cls);
   		if(params != null) {
   			Iterator<NameValuePair> iter = params.iterator();
   			while(iter.hasNext()) {
   				NameValuePair param = iter.next();
   	   			intent.putExtra(param.getName(),param.getValue());
   			}
   		}
   		intent.setFlags(intentFlags);
   		context.startActivity(intent);
   	} 
   }
   public static void launchPhoneDialer(Context context, String dialNumber) {
	   Uri callUri = null;
	   if(dialNumber != null) {
		   callUri = Uri.parse("tel:"+dialNumber);
	   }
    	Intent callIntent = new Intent(Intent.ACTION_DIAL,callUri);
    	callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    	
        context.startActivity(callIntent);    	
   }

}
