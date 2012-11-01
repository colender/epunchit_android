package com.epunchit.utils;

import com.google.api.client.util.Key;


public class Location {
	@Key
	public double latitude;
	public double longitude;
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
