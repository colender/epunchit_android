package com.epunchit.user;

import com.epunchit.utils.Geometry;
import com.google.api.client.util.Key;

public class PlaceDetails {
		@Key
		public String name;
	
		@Key
		public String formatted_phone_number;
		
		@Key
		public String formatted_address;
		
		@Key
		public Geometry geometry;
		

		@Override
		public String toString() {
				return name+"-"+formatted_address;
		}

		

		public String getName() {
			return name;
		}



		public void setName(String name) {
			this.name = name;
		}



		public String getFormatted_phone_number() {
			return formatted_phone_number;
		}


		public void setFormatted_phone_number(String formatted_phone_number) {
			this.formatted_phone_number = formatted_phone_number;
		}


		public String getFormatted_address() {
			return formatted_address;
		}


		public void setFormatted_address(String formatted_address) {
			this.formatted_address = formatted_address;
		}
		
		
}
