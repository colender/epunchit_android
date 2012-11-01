package com.epunchit.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class TargetLocationOverlay extends ItemizedOverlay<OverlayItem> {
	  private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
	  
	  public TargetLocationOverlay (Drawable marker) {
		  super(boundCenterBottom(marker));
		  populate();
	  }	  
	  
	  public void addItem(GeoPoint p, String title, String snippet){
		  OverlayItem newItem = new OverlayItem(p, title, snippet);
		  overlayItemList.add(newItem);
		     populate();
	  }
	  
	  @Override
	  public void draw(Canvas canvas, MapView mapView, boolean shadow) {
	    if (shadow == false) {
//	      [ ... draw stuff here ... ]
	    }
	    super.draw(canvas, mapView, shadow);
	  }

	  @Override
	  public boolean onTap(GeoPoint point, MapView mapView) {
	    return false;
	  }
	  
	@Override
	protected OverlayItem createItem(int i) {
		return overlayItemList.get(i);
	}

	@Override
	public int size() {
		return overlayItemList.size();
	}
}	  
