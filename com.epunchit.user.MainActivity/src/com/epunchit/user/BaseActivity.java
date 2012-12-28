package com.epunchit.user;

import static com.epunchit.Constants.EP_QRCODE_REDEEM_PATH;
import static com.epunchit.Constants.EP_QRCODE_UPDATE_PATH;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Html;
import android.util.Log;

import com.epunchit.Constants;
import com.epunchit.utils.Utils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class BaseActivity extends Activity {

	private final String TAG = "BaseActivity";

	BaseActivity.UserUpdateResultReceiver userUpdateResultReceiver = null;

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == IntentIntegrator.REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				IntentResult scanResult = IntentIntegrator.parseActivityResult(
						requestCode, resultCode, intent);
				if (scanResult != null) {
					String content = scanResult.getContents();

					String format = scanResult.getFormatName();

					Map<String, String> contents = parseContent(content);
					confirmActionDialog("Content Being Sent", contents, content);
				}
			} else if (resultCode == RESULT_CANCELED) {
				// handle cancel
			}
		}
	}

	public HashMap<String, String> parseContent(String content) {
		String[] strPairs = content.split("&");
		HashMap<String, String> contents = new HashMap<String, String>();

		for (String kPair : strPairs) {

			String[] kv = kPair.split("=");
			String key = kv[0];
			String value = kv[1];
			if (key.equals("place"))
				contents.put("place", value);
			if (key.equalsIgnoreCase("point"))
				contents.put("point", value);
			if (key.equalsIgnoreCase("type"))
				contents.put("type", value);
			if (key.equalsIgnoreCase("places"))
				contents.put("places", value);

		}

		return contents;

	}

	public void confirmActionDialog(final String title,
			final Map<String, String> contents, final String content) {

		StringBuilder strB = new StringBuilder();
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		// String text = "Please confirm:"+content;
		String place = "Place: " + contents.get("place") + "\n";
		String points;
		String msg;
		if (contents.get("type") != null
				&& contents.get("type").equalsIgnoreCase("redeem")) {
			msg = "You are going to use a Redeem would you like to continue?";
			points = "<br /><font color='#90EE90'>*Please make sure to show cashier that Redeem was successful.</font>\n";
		} else {
			msg = "Do you want to add point";
			points = "Points: " + contents.get("point") + "\n";
		}

		strB.append(msg + points);
		alert.setTitle(title);
		alert.setMessage(Html.fromHtml(strB.toString()));
		Log.d(TAG, strB.toString());
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				updateUserInfo(content);
			}
		});
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
		alert.show();
	}

	// updates the user infromation
	public void updateUserInfo(String data) {
		try {
			String place = "";
			String points = "";
			String redeem = "";
			String[] kvPairs = data.split("&");
			for (String kvPair : kvPairs) {
				String[] kv = kvPair.split("=");
				String key = kv[0];
				String value = kv[1];
				if (key.equals("place"))
					place = value;
				if (key.equalsIgnoreCase("point"))
					points = value;
				if (key.equalsIgnoreCase("type"))
					redeem = value;
				if (key.equalsIgnoreCase("places"))
					place = value;
			}

			Bundle params = new Bundle();

			params.putString("type", redeem);
			// TODO After code is being used delete code from SharedPreference a
			// look for a new code.
			Intent intent = new Intent(this, RESTClientService.class);
			Uri updatepath = null;
			if (redeem.equalsIgnoreCase("redeem")) {
				updatepath = Uri.parse(EP_QRCODE_REDEEM_PATH);
				params.putString("code", Utils.getRedeemCode(this));
				params.putString("userid", Utils.getAccountName(this));
				params.putString("place", place);
				Log.d("REDEEM:", "Inside the redeem");
				Log.d("Code:", Utils.getRedeemCode(this));
			} else {
				updatepath = Uri.parse(EP_QRCODE_UPDATE_PATH);
				params.putString("uid", Utils.getAccountName(this));
				params.putString("point", points);
				params.putString("place", place);
			}
			intent.setData(updatepath);
			intent.putExtra(RESTClientService.EXTRA_PARAMS, params);
			userUpdateResultReceiver = new UserUpdateResultReceiver(
					new Handler());
			userUpdateResultReceiver.setParentContext(this);
			intent.putExtra(RESTClientService.EXTRA_RESULT_RECEIVER,
					userUpdateResultReceiver);
			intent.putExtra(RESTClientService.EXTRA_HTTP_VERB,
					RESTClientService.GET);
			startService(intent);
		} catch (Exception e) {
			if (Constants.DEBUG)
				Log.e("MainActivity",
						"error updating qr input:" + e.getMessage());
		}

	}

	class UserUpdateResultReceiver extends ResultReceiver {

		private Context context = null;

		protected void setParentContext(Context context) {
			this.context = context;
		}

		public UserUpdateResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {

			if (resultData != null) {
				Log.d("MaintActivtiy:", "Inside receive");
				String responseString = resultData
						.getString(RESTClientService.REST_RESPONSE);
				Log.d("MainActivity:", responseString);
				try {
					JSONObject json = new JSONObject(responseString);
					String success = json.getString("Success");
					if (success.equalsIgnoreCase("true")) {

						String type = json.getString("type");
						if (type.equalsIgnoreCase("redeem")) {
							String body = getString(R.string.redeem_success);

							// getUserData();
							confirmationRedeemDialog("Redeem Success", body);
						} else {
							// getUserData();
							confirmationDialog("Success",
									"Your points have been added");
						}

					} else {
						String errormsg = json.getString("Error");
						Log.d("UserUpdateResultReciver:error msg:", errormsg);
						confirmationDialog("Error- Program Error", errormsg);

					}
				} catch (JSONException e) {
					Log.d(TAG, e.getMessage());
					confirmationDialog("Error", "Field Does not Exist");
				}
			}
		}
	}

	public void confirmationRedeemDialog(String title, String bodymsg) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(title);
		alert.setIcon(android.R.drawable.ic_dialog_info);
		alert.setMessage(Html.fromHtml(bodymsg));
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				onStart();
			}
		});
		alert.show();

	}

	public void confirmationDialog(final String text, final String message) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		String title = text;
		alert.setTitle(title);
		alert.setIcon(android.R.drawable.ic_dialog_info);
		alert.setMessage(message);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				onStart();
			}
		});
		alert.show();
	}

}
