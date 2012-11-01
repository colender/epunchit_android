package com.epunchit.user;
import com.epunchit.utils.Geometry;
import com.google.api.client.util.Key;


public class Place {
	
	@Key
	public String id;

	@Key
	public String name;

	@Key
	public String reference;
	
	@Key
	public Geometry geometry;
	
	@Key
	public String vicinity;
	
	

	public String getVicinity() {
		return vicinity;
	}

	public void setVicinity(String vicinity) {
		this.vicinity = vicinity;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}
	

}
