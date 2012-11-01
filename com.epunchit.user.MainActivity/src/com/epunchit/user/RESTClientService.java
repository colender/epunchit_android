package com.epunchit.user;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.epunchit.Constants;
import com.epunchit.utils.CustomSSLSocketFactory;
import com.epunchit.utils.FullX509TrustManager;
import com.epunchit.utils.Utils;
import com.google.api.client.http.HttpStatusCodes;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class RESTClientService extends IntentService {

	public static final String TAG = RESTClientService.class.getName();
    public static final int GET    = 0x1;
    public static final int POST   = 0x2;
    public static final int PUT    = 0x3;
    public static final int DELETE = 0x4;
    public static final String EXTRA_HTTP_VERB       = "com.epunchit.EXTRA_HTTP_VERB";
    public static final String EXTRA_PARAMS          = "com.epunchit.EXTRA_PARAMS";
    public static final String EXTRA_RESULT_RECEIVER = "com.epunchit.EXTRA_RESULT_RECEIVER";
    
    public static final String REST_RESPONSE = "com.epunchit.REST_RESPONSE";
    
    public static Context mContext = null;
	
	public RESTClientService(String name) {
		super(TAG);
	}

	public RESTClientService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

        Uri    action = intent.getData();
        Bundle extras = intent.getExtras();
        if(Constants.DEBUG)
        	Log.d("RESTClientService", "inside RESTClientService...");
        
        if (extras == null || action == null) { //no data to process
            return;
        }
        
        mContext = this;
		
        // We default to POST if no verb was specified.
        int verb = POST;
        
        if(extras.containsKey(EXTRA_HTTP_VERB))
        	verb = extras.getInt(EXTRA_HTTP_VERB, GET);
        
        Bundle params = extras.getParcelable(EXTRA_PARAMS);
        ResultReceiver receiver = null;
        if(extras.containsKey(EXTRA_RESULT_RECEIVER)) {
        	receiver = extras.getParcelable(EXTRA_RESULT_RECEIVER);
        }
        try {
        	HttpResponse response = null;
        	
            switch (verb) {
            	case GET: {
                    response = makeGetRequest(action.toString(), paramsToList(params));
            		break;
            	}
            	case PUT: {
                	break;
            	}
            	case DELETE: {
                	break;
            	}
            	default: {
            		response = makePostRequest(action.toString(), paramsToList(params));
            		Log.d("RESTClientService:",response.toString());
            	}
            }
            if(response != null)
            {
            	HttpEntity responseEntity = response.getEntity();
            	StatusLine responseStatus = response.getStatusLine();
            	int statusCode = responseStatus != null ? responseStatus.getStatusCode() : 0;
           		if (responseEntity != null) {
           			Bundle resultData = new Bundle();
           			resultData.putString(REST_RESPONSE, EntityUtils.toString(responseEntity, HTTP.UTF_8));
           			if(receiver != null) {
                        Log.d("RESTClientService", "RESTClientService...calling result receiver");

           				receiver.send(statusCode, resultData);
           			}
           			else //send an ordered broadcast
           			{
           				Intent broadcastIntent = new Intent();
           				broadcastIntent.setAction(REST_RESPONSE);
           				//broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
           				broadcastIntent.putExtra("returnCode", statusCode);
           				broadcastIntent.putExtra("returnData", resultData);
  //         				sendOrderedBroadcast(broadcastIntent, null);
           			}
           		}
           		else {
           			if(receiver != null)
           			{
               			receiver.send(statusCode, null);
           			}
           			else //send an ordered broadcast
           			{
           				Intent broadcastIntent = new Intent();
           				broadcastIntent.setAction(REST_RESPONSE);
           				broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
           				broadcastIntent.putExtra("returnCode", statusCode);
      //     				sendBroadcast(broadcastIntent);
           			}
           		}
            }

        } 
        catch (Exception e) {
        	if(Constants.DEBUG)
        		e.printStackTrace();
            if(receiver != null)
            	receiver.send(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, null);
        }
	}
    private static List<BasicNameValuePair> paramsToList(Bundle params) {
    	
    	if(params == null || params.size() <= 0)
    		return null;
        ArrayList<BasicNameValuePair> formList = new ArrayList<BasicNameValuePair>(params.size());
        
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value != null) formList.add(new BasicNameValuePair(key, value.toString()));
        }
        return formList;
    }
	
	public static HttpResponse makePostRequest(String urlPath, List<BasicNameValuePair> params)
	throws URISyntaxException, ClientProtocolException, IOException 
    {
        // Make POST request
        URI uri = new URI(urlPath);
        HttpPost post = new HttpPost(uri);
        if(params != null) {
        	UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
        	post.setEntity(entity);
        }
        post.setHeader("X-Same-Domain", "1");  // XSRF
        DefaultHttpClient client = Utils.getTrustedHttpClient(mContext);
        HttpResponse res = client.execute(post);
        client.getConnectionManager().shutdown();
        return res;
    }

	public static HttpResponse makeGetRequest(String urlPath, List<BasicNameValuePair> params)
	throws URISyntaxException, ClientProtocolException, IOException 
    {
         HttpGet request = new HttpGet();
        if (params == null) {
            request.setURI(new URI(urlPath));
        }
        else {
        	Uri uri = Uri.parse(urlPath);
            Uri.Builder uriBuilder = uri.buildUpon();
            
            // Loop through our params and append them to the Uri.
            for (BasicNameValuePair param : params) {
                uriBuilder.appendQueryParameter(param.getName(), param.getValue());
            }
            uri = uriBuilder.build();
            request.setURI(new URI(uri.toString()));
        }
        DefaultHttpClient client = Utils.getTrustedHttpClient(mContext);
        HttpResponse res = client.execute(request);
        client.getConnectionManager().shutdown();
        return res;
    }
	

	private static String verbToString(int verb) {
        switch (verb) {
            case GET:
                return "GET";
                
            case POST:
                return "POST";
                
            case PUT:
                return "PUT";
                
            case DELETE:
                return "DELETE";
        }
        
        return "";
    }
}


