package com.epunchit.user;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A single places item (child view for expandable list).
 */
public class PlacesItemView extends LinearLayout {
    private final TextView mTextView;
    private final TextView mUrlText;
    private final TextView mTimeText;
    private final ImageView mImageView;
    private final Context mContext;

    public PlacesItemView(Context context) {
        super(context);
        mContext = context;
        LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.places_item, this);
        mTextView = (TextView) findViewById(R.id.title);
        mUrlText = (TextView) findViewById(R.id.url);
        mImageView = (ImageView) findViewById(R.id.icon);
        mTimeText = (TextView) findViewById(R.id.time);
    }

    public void setTitle(String title) {
        mTextView.setText(ellipsis(title));
    }

    public void setUrl(String url) {
        int id = R.drawable.places_maps_item_indicator;
        Drawable icon = mContext.getResources().getDrawable(id);
        mImageView.setImageDrawable(icon);
        mUrlText.setText(ellipsis(url));
    }

    public void setTime(String strTime) {
        mTimeText.setText(ellipsis(strTime));
    }

    private String ellipsis(String string) {
        int MAX_LENGTH = 50;
        if (string.length() > MAX_LENGTH - 3) {
            string = string.substring(0, MAX_LENGTH - 3);
            string += "...";
        }
        return string;
    }
}

