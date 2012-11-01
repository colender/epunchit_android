package com.epunchit.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.epunchit.utils.Utils;
import static com.epunchit.Constants.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AddPlacesArrayAdapter extends ArrayAdapter<Place> {
    Context context;
    int layoutResourceId;   
    List<Place> data;
   
    public AddPlacesArrayAdapter(Context context, int layoutResourceId, List<Place> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PlaceHolder holder = null;
       
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
           
            holder = new PlaceHolder();
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            holder.txtDetails = (TextView)row.findViewById(R.id.txtDetails);
                      
            row.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder)row.getTag();
        }
        
        Place place = getItem(position);
        if(place != null)
        {
        	holder.txtTitle.setText(place.toString());
        	holder.txtDetails.setText(place.getVicinity());
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
    public Place getItem(int position) {
    	if(data != null && data.size() > 0)
    		return data.get(position);
    	return null;
    }

    static class PlaceHolder
    {
        public TextView txtTitle;
        public TextView txtDetails;
    }
}
