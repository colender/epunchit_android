package com.epunchit.utils;

import static com.epunchit.Constants.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import com.epunchit.Constants;
import com.epunchit.user.EPPlace;
import com.epunchit.user.PlaceDetails;
import com.epunchit.user.PlaceDetailsResponse;
import com.epunchit.user.PlacesDatabase;
import com.epunchit.user.R;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Utils {
	// The different Places API endpoints.
	private static final String PLACES_SEARCH_URL =  "https://maps.googleapis.com/maps/api/place/search/json?";
	private static final String PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
	private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
	private static final HttpTransport transport = new NetHttpTransport();

	public static void init(Context context)
	{
        SharedPreferences settings = Prefs.get(context);
        boolean dbInitialized = settings.getBoolean("DB_INITIALIZED", false);
        if(!dbInitialized)
        {
    		//PlacesDatabase placesDB = PlacesDatabase.get(context);
    		Editor editor = settings.edit();
    		editor.putBoolean("DB_INITIALIZED", true);
    		editor.commit();
        }
	}
	
	public static String getAccountName(Context context)
	{
        SharedPreferences settings = Prefs.get(context);
        String userName = settings.getString("user", null);
        if(userName == null || userName.length() == 0)
        	return null;
        return userName;
	}

	public static String getPassword(Context context)
	{
        SharedPreferences settings = Prefs.get(context);
        String password = settings.getString("password", null);
        if(password == null || password.length() == 0)
        	return null;
        return password;
	}
	
	public static void setAccountInfo(Context context, String userName, String password, boolean remember)
	{
		SharedPreferences prefs = Prefs.get(context);
		Editor editor = prefs.edit();
		editor.putBoolean("UserSignedIn", true);
		editor.putBoolean("RememberMe", remember);
		editor.putString("user", userName);
		editor.putString("password", password);
		editor.commit();
	}
	
	public static boolean isUserSignedIn(Context context)
	{
        SharedPreferences settings = Prefs.get(context);
        boolean signedIn = settings.getBoolean("UserSignedIn", false);
        return signedIn;
	}
	public static boolean isRememberMe(Context context)
	{
        SharedPreferences settings = Prefs.get(context);
        boolean remember = settings.getBoolean("RememberMe", false);
        return remember;
	}
	
	public static void signOutUser(Context context)
	{
        SharedPreferences prefs = Prefs.get(context);
		Editor editor = prefs.edit();
		editor.putBoolean("UserSignedIn", false);
		editor.commit();
	}
	
	public static void setRedeemCode(Context context, String code)
	{
        SharedPreferences settings = Prefs.get(context);
        Editor editor = settings.edit();
        editor.putString("reward_code", code);
        editor.commit();
	}
	public static String getRedeemCode(Context context)
	{
        SharedPreferences settings = Prefs.get(context);
        String userName = settings.getString("reward_code", null);
        return userName;
	}

	public static Location getLastKnownLocation(Context context) {
		Location l = null;
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);
		if(providers != null)
		{
			int initVal = providers.size()-1;
			/* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
			for (int i = initVal; i >= 0; i--) {
				l = lm.getLastKnownLocation(providers.get(i));
				if (l != null) 
					break;
			}
		}
		if(l != null)
			saveLastKnownLocation(context, l.getLatitude(), l.getLongitude());
		else
			l = getSavedLastKnownLocation(context);
		return l;
	}
	
	public static void saveLastKnownLocation(Context context, double lat, double lng)
	{
        SharedPreferences settings = Prefs.get(context);
        Editor editor = settings.edit();
        editor.putString("lat", String.valueOf(lat));
        editor.putString("lng", String.valueOf(lng));
        editor.commit();
	}
	
	public static Location getSavedLastKnownLocation(Context context)
	{
		Location l = new Location("MyLocation");
        SharedPreferences settings = Prefs.get(context);
        String lat = settings.getString("lat", "0");
        String lng = settings.getString("lng", "0");
		l.setLatitude(Double.valueOf(lat));
		l.setLongitude(Double.valueOf(lng));
		return l;
	}

	public static String getDevicePhoneNumber(Context context)
	{
		String phoneNum = "";
	   TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	   if(tMgr != null)
	     phoneNum = tMgr.getLine1Number();
	   if(DEBUG)
	   {
		   if(phoneNum != null && phoneNum != "")
			   Log.d("getDevicePhone returned ",phoneNum);
		   else
			   Log.d("getDevicePhone returned"," null ");
	   }
	   return phoneNum;
	}
	
	public static boolean validatePhone(String phoneText)
	{
		boolean ret = false;
		Pattern pattern = Pattern.compile("(\\d{10})|(\\d{3}-\\d{3}-\\d{4})");
		Matcher matcher = pattern.matcher(phoneText);
		if(matcher.matches())
			ret = true;
		return ret;
	}
	
    public static List<EPPlace> JSONArrayToPlaceList(JSONArray jsonArray) 
    {
    	
    	if(jsonArray == null || jsonArray.length() <= 0)
    		return null;
    	if(DEBUG)
    		Log.d("JSONArrayToPlaceList",jsonArray.toString());
    	try {
    		List<EPPlace> list = new ArrayList<EPPlace>();
    		int len = jsonArray.length();
       		EPPlace place = null;
    		JSONObject placeObj = null;
    		String following = null;
    		for(int i = 0; i < len; i++)
    		{
    			place = new EPPlace();
    			placeObj = jsonArray.getJSONObject(i);
    			try {
    				place.setUrl(placeObj.getString("placeURL"));
    				place.setName(placeObj.getString("placeName"));
    				place.setFace(placeObj.getString("placeFace"));
    				if(placeObj.has("following"))
    				{
    					following = placeObj.getString("following");
    					if(following.equalsIgnoreCase("1"))
    						place.setFollowing(true);
    					else
    						place.setFollowing(false);
    				}
    				if(placeObj.has("placePhone"))
        				place.setPhone(placeObj.getString("placePhone"));
    				if(placeObj.has("placeLat"))
    					place.setLat(placeObj.getString("placeLat"));
    				if(placeObj.has("placeLng"))
    					place.setLng(placeObj.getString("placeLng"));
    				list.add(place);
    			} catch(Exception ex)
    			{
    	        	if(DEBUG)
    	    			Log.d("JSONToPlaceList","JSON Error:"+ex.getMessage());
    			}
    		}
    		return list;
    	} catch(Exception ex)
    	{
    		
    	}
    	return null;
    }

    public static Bitmap drawable_from_url(String url) {
        Bitmap x;

        try {
        HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
        connection.setRequestProperty("User-agent","Mozilla/4.0");

        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        connection.disconnect();
        return x;
        } catch(Exception ex)
        {
        	
        }
        return null;
    }

    public static KeyStore getTrustCertificate(Context context)
    {
		try {
            int timeoutConnection = 10000;
            int timeoutSocket = 10000;

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
            KeyStore trusted = KeyStore.getInstance("BKS");
            InputStream in = context.getResources().openRawResource(R.raw.epunchit);
            try {
            	trusted.load(in, "oside6023".toCharArray());
            } finally {
            	in.close();
            }
            return trusted;
		} catch(Exception ex)
		{
			if(Constants.DEBUG)
				Log.e("getTrustCertificate",ex.getMessage());
		}
		return null;

    }
    
    public static Bitmap getBitmapFromURL(String urlStr, Context context)
    {
    	final int IO_BUFFER_SIZE = 4 * 1024;
    	final int IMAGE_MAX_SIZE = 50;
        Bitmap b = null;
        try {
        	
        	KeyStore trusted = getTrustCertificate(context);
        	   TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        	   tmf.init(trusted);
        	   SSLContext sslContext = SSLContext.getInstance("TLS");
        	   sslContext.init(null, tmf.getTrustManagers(), null);
        	   URL url = new URL(urlStr);
        	   HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        	   urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
        	   InputStream inputStream = urlConnection.getInputStream();
        	BufferedInputStream in = new BufferedInputStream(inputStream,IO_BUFFER_SIZE);
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();
            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }
            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
     	   url = new URL(urlStr);
    	   urlConnection = (HttpsURLConnection) url.openConnection();
    	   urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
    	   inputStream = urlConnection.getInputStream();

            in = new BufferedInputStream(inputStream, IO_BUFFER_SIZE);
            b = BitmapFactory.decodeStream(in, null, o2);
            in.close();
        } catch (Exception e) {
        	if(Constants.DEBUG)
        		e.printStackTrace();
        }
        return b;
    }

	public static DefaultHttpClient getTrustedHttpClient(Context context)
	{
		try {
			/*
            int timeoutConnection = 10000;
            int timeoutSocket = 10000;

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
            KeyStore trusted = KeyStore.getInstance("BKS");
            InputStream in = context.getResources().openRawResource(R.raw.epunchit);
            try {
            	trusted.load(in, "oside6023".toCharArray());
            } finally {
            	in.close();
            }
            */

            int timeoutConnection = 10000;
            int timeoutSocket = 10000;
            KeyStore trusted = getTrustCertificate(context);
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);

            
			SchemeRegistry schemeRegistry = new SchemeRegistry ();
			schemeRegistry.register (new Scheme ("http",PlainSocketFactory.getSocketFactory (), 80));
			schemeRegistry.register (new Scheme ("https",new CustomSSLSocketFactory(trusted), 443));
			ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
			return new DefaultHttpClient (cm, httpParams);
		} catch(Exception ex) {
			Log.d("getTrustedClient:",ex.getMessage());
		}
		return null;
	}
	
	   public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
			  return transport.createRequestFactory(new HttpRequestInitializer() {
			   public void initialize(HttpRequest request) {
			    GoogleHeaders headers = new GoogleHeaders();
			    headers.setApplicationName(APP_NAME);
			    request.setHeaders(headers);
			    JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
			    request.addParser(parser);
			   }
			});
		}


	  public static PlaceDetails getPlaceDetails(String reference) throws Exception {
		  	PlaceDetailsResponse placeDetailsResponse = null;
				try {
					HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
					HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
					request.getUrl().put("key", API_KEY);
					request.getUrl().put("reference", reference);
					request.getUrl().put("sensor", "false");
					placeDetailsResponse = request.execute().parseAs(PlaceDetailsResponse.class);
					return placeDetailsResponse.getPlaceDetails();
				} catch (HttpResponseException e) {
					if(DEBUG)
						Log.e("getPlaceDetails",e.getMessage());
				}
				return null;
	  }

	    public static void CopyStream(InputStream is, OutputStream os)
	    {
	        final int buffer_size=1024;
	        try
	        {
	            byte[] bytes=new byte[buffer_size];
	            for(;;)
	            {
	              int count=is.read(bytes, 0, buffer_size);
	              if(count==-1)
	                  break;
	              os.write(bytes, 0, count);
	            }
	        }
	        catch(Exception ex){}
	    }

}
