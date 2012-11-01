package com.epunchit.user;

import com.google.api.client.util.Key;

public class PlaceDetailsResponse {
		@Key
		public String status;
	
		@Key
		public PlaceDetails result;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public PlaceDetails getPlaceDetails() {
			return result;
		}

		public void setPlaceDetails(PlaceDetails placeDetails) {
			this.result = placeDetails;
		}
		
		
}
