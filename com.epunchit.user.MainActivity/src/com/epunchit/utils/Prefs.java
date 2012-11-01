/*
 * Copyright 2012 EPUNCHIT Corp..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epunchit.utils;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public final class Prefs {
	private static String uniqueID = null;
	private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
	private static final String APP_PREF_TAG = "EPUNCHIT_PREFS";
	
    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences(APP_PREF_TAG, 0);
    }
    
    public synchronized static String appUUID(Context context) {
    	if(uniqueID == null) {
    		SharedPreferences prefs = context.getSharedPreferences(
    				PREF_UNIQUE_ID,Context.MODE_PRIVATE);
    		uniqueID = prefs.getString(PREF_UNIQUE_ID, null);
    		if(uniqueID == null) {
    			uniqueID = UUID.randomUUID().toString();
    			Editor editor = prefs.edit();
    			editor.putString(PREF_UNIQUE_ID, uniqueID);
    			editor.commit();
    		}
    	}
    	return uniqueID;
    }
}
