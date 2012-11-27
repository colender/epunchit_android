package com.epunchit.user;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.epunchit.Constants;
import com.epunchit.user.ImageDownloader.Mode;
import com.epunchit.utils.LauncherUtils;
import com.epunchit.utils.Utils;
import static com.epunchit.Constants.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class EPPlaceArrayAdapter extends ArrayAdapter<EPPlace> {
    Context context;
    int layoutResourceId;   
    List<EPPlace> data;
    Map<String,Bitmap> imageMap;
    Location curLoc;
    PlacesDatabase placesDB;
    LayoutInflater inflater = null;
    ImageLoader imageLoader = null;
    Bitmap stubIcon = null;
    private final String TAG = "EPPlaceArrayAdapter";
    private ImageDownloader imageDownloader;
    private ProgressDialog progressbar;
    public EPPlaceArrayAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.inflater = ((Activity)context).getLayoutInflater();
        this.curLoc = Utils.getLastKnownLocation(context);
        this.imageLoader = new ImageLoader(context);
    	stubIcon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.no_face_logo);

    }
    
    

    public EPPlaceArrayAdapter(Context context, int layoutResourceId, List<EPPlace> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.inflater = ((Activity)context).getLayoutInflater();
        this.imageLoader = new ImageLoader(context);
       
    	stubIcon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.no_face_logo);
        this.data = data;
        this.curLoc = Utils.getLastKnownLocation(context);
        this.progressbar= null;
        //imageDownloader = new ImageDownloader(this.context,this.progressbar);
        //imageDownloader.setMode(Mode.NO_DOWNLOADED_DRAWABLE);
        //this.placesDB = PlacesDatabase.get(context);
        //createImageMap(data);
    }

    
    public EPPlaceArrayAdapter(Context context, int layoutResourceId, List<EPPlace> data,ProgressDialog progressBar) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.inflater = ((Activity)context).getLayoutInflater();
       // this.imageLoader = new ImageLoader(context);
       
    	stubIcon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.no_face_logo);
        this.data = data;
        this.curLoc = Utils.getLastKnownLocation(context);
        this.progressbar= progressBar;
        imageDownloader = new ImageDownloader(this.context,this.progressbar);
        imageDownloader.setMode(Mode.NO_DOWNLOADED_DRAWABLE);
        //this.placesDB = PlacesDatabase.get(context);
        //createImageMap(data);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final PlaceHolder holder;
       
        if(row == null)
        {
            row = inflater.inflate(layoutResourceId, parent, false);
           
            holder = new PlaceHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            holder.btnFollow = (Button)row.findViewById(R.id.imgFollow);
            holder.txtDistance = (TextView)row.findViewById(R.id.txtDistance);
            //holder.txtPhone = (TextView) row.findViewById(R.id.placePhone);
           
            row.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder)row.getTag();
        }
       
        final EPPlace place = getItem(position);
        if(place != null)
        {
        	if(holder.txtDistance != null)
        	{
        		if(curLoc != null)
        		{
       	   			Location dest = new Location("destination");
       	   			if(place.getLat() != null && place.getLat() != "")
       	   				dest.setLatitude(Double.valueOf(place.getLat())); 
       	   			if(place.getLng() != null && place.getLng() != "")
       	   				dest.setLongitude(Double.valueOf(place.getLng()));
       	   			String distanceMiles = new DecimalFormat("#.##").format(curLoc.distanceTo(dest) * 0.000621371192);
       	   			holder.txtDistance.setText(String.valueOf(distanceMiles)+" miles");
        		} else {
        			holder.txtDistance.setText("? miles");
        		}
        	}
        	
        	//Removes HTML Links on titles 
        	holder.txtTitle.setText(place.getName());
        	// should not make text title clickable.
        	/*holder.txtTitle.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
				   	List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
			      	NameValuePair nvPair = new BasicNameValuePair("placeURL",place.getUrl());
			    	nvPairs.add(nvPair);
			    	LauncherUtils.launchActivity(R.layout.epplacedetailsview, context, nvPairs);    	
				}
			});*/
        	
        	/*
        	if(holder.txtPhone != null)
        	{
        		holder.txtPhone.setText(place.getPhone());
        	}
        	*/
        	if(holder.btnFollow != null)
        	{
        		holder.btnFollow.setOnClickListener(new View.OnClickListener() {
				
        			@Override
        			public void onClick(View v) {
        				// This must be changed causing error in Favorites activity 
        				// Should just send remove from favorites to sever 
        				
        				//PlacesView placesView = (PlacesView)context;
        				String action = "follow";
        				if(place.isFollowing())
        				{
        					place.setFollowing(false);
               				holder.btnFollow.setBackgroundResource(R.drawable.not_following);
               				action = "unfollow";
        				}
        				else 
        				{
        					place.setFollowing(true);
            				holder.btnFollow.setBackgroundResource(R.drawable.following);
        				}
        				Utils.followPlaceRequest(place.getUrl(), action,getContext());
        			}
        			});
            	if(place.isFollowing())
            	{
            		holder.btnFollow.setBackgroundResource(R.drawable.following);
            	} else {
            		holder.btnFollow.setBackgroundResource(R.drawable.not_following);
            	}
        	}
        	/*
        	if(imageMap.containsKey(place.getUrl()))
        	{
        		holder.imgIcon.setImageBitmap(imageMap.get(place.getUrl()));
        	}
        	*/
//        	else {
        		String photoURL = null;
        		if(place.getFace() != null)
        			photoURL = BASE_IMAGE_PATH + place.getFace();
        		if(DEBUG)
        			Log.d("EPPlaceArrayAdapter","getView photoURL:"+photoURL);
        		
        		try {
        			if(photoURL != null)
        			{
        				//Log.d(TAG,photoURL);
        				imageLoader.DisplayImage(photoURL, holder.imgIcon);
        				//imageDownloader.download(photoURL, holder.imgIcon);
        				/*
        				Bitmap bitmapIcon = Utils.getBitmapFromURL(photoURL, context);
    					//ByteArrayOutputStream stream = new ByteArrayOutputStream();
    					//bitmapIcon.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    					//byte[] byteArrayIcon = stream.toByteArray();
        				//PlacesDatabase.insertPlace(context,place.getUrl(), byteArrayIcon, place.getLat(), place.getLng());
        				holder.imgIcon.setImageBitmap(bitmapIcon);
        				imageMap.put(place.getUrl(), bitmapIcon);
        				*/
        			} else {
           				holder.imgIcon.setImageBitmap(stubIcon);
        			}
        		} catch(Exception ex) {
       				holder.imgIcon.setImageBitmap(stubIcon);
       				if(DEBUG)
       					Log.d("EPPlaceArrayAdapter","getView error:"+ex.getMessage());
       				//	ex.printStackTrace();
        		}
  //      	}
        	
        }
        //row.setTag(holder);
        return row;
    }
    
    @Override
    public int getCount() {
    	if(data != null)
    		return data.size();
    	return 0;
    }

    @Override
    public EPPlace getItem(int position) {
    	if(data != null && data.size() > 0)
    		return data.get(position);
    	return null;
    }

    private void createImageMap(List<EPPlace> data)
    {
    	Iterator<EPPlace> iter = data.iterator();
    	EPPlace place = null;
    	imageMap = new HashMap<String,Bitmap>();
    	Bitmap bitmapIcon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.no_face_logo);
   		imageMap.put("no_face_logo",bitmapIcon);

			//holder.imgIcon.setImageResource(R.drawable.no_face_logo);
    	String placeUrl = null;
    	while(iter.hasNext())
    	{
    		place = iter.next();
    		placeUrl = place.getUrl();
    		if(placeUrl != null)
    		{
    			//byte[] byteArrayIcon = placesDB.lookupIcon(place.getUrl());
    			//if(byteArrayIcon != null)
    			//{
//    				bitmapIcon = BitmapFactory.decodeByteArray(
    				//	byteArrayIcon, 0,
//    					byteArrayIcon.length);

  //  			} else {
    				String photoURL = null;
    				if(place.getFace() != null && !("" == place.getFace()))
    					photoURL = BASE_IMAGE_PATH + place.getFace();
    				try {
    					if(photoURL != null)
    					{
    						bitmapIcon = Utils.getBitmapFromURL(photoURL, context);
    					//	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    						//bitmapIcon.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    						//byteArrayIcon = stream.toByteArray();
    					}
    				} catch(Exception ex)
    				{
    		    		if(DEBUG)
    		    			Log.d("EPPlaceArrayAdapter","createImageMap error:"+ex.getMessage());
    				}

    //			}
    			if(bitmapIcon != null)
    			{
    				imageMap.put(place.getUrl(), bitmapIcon);
    				//PlacesDatabase.insertPlace(context,place.getUrl(), byteArrayIcon, place.getLat(), place.getLng());
    			}
    		}
    	}
    }
    
    static class PlaceHolder
    {
        public ImageView imgIcon;
        public TextView txtTitle;
        public Button btnFollow;
        public TextView txtDistance;
        //public TextView txtPhone;
    }
}
