package com.epunchit.utils;

import com.google.api.client.util.Key;

public class Geometry {
	@Key
	public Location location;

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
}
