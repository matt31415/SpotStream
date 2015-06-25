package com.example.android.spotstream;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * The TextPlusImageArrayAdapter is used when working with pairs of images and strings.  In this case
 * the image will be represented by URLs pointing to image resources.   The adapter works with
 * ArrayLists of TextPlusImage objects (a really simple object containing a string and a URL  that
 * points to an image).
 *
 * This was inspired by:
 *   http://www.ezzylearning.com/tutorial/customizing-android-listview-items-with-custom-arrayadapter
 * although I didn't like that authors use of theView tag to store random data, so I'm risking a
 * bit of a performance hit instead..
 */
public class TextPlusImageArrayAdapter extends ArrayAdapter<TextPlusImage> {
    private final String LOG_TAG = TextPlusImageArrayAdapter.class.getSimpleName();

    private Context mContext;
    private int mLayoutResource;
    private ArrayList<TextPlusImage> mTextPlusImageData;
    private int mTextResource;
    private int mImageResource;


    /**
     * Basic cosnstructor to override Array Adapeter.
     *
     * @param context
     * @param resource The resource id for the layout containing a single item from the list
     * @param objects The list of TextPlusImage objects that we wish to add to the adapter.
     * @param textResource The resource id for the field into which we want to place the text
     * @param imageResource The resource id for the field into which we want to place the image
     */
    public TextPlusImageArrayAdapter(
            Context context,
            int resource,
            ArrayList<TextPlusImage> objects,
            int textResource,
            int imageResource
    ) {
        super (context,resource,objects);

        mContext = context;
        mLayoutResource = resource;
        mTextPlusImageData = objects;
        mTextResource = textResource;
        mImageResource = imageResource;
    }

    /**
     * Ovverrides the ArrayAdapter's getView function so that we can render an individual list item
     *
     * @param position Index of the data element we want to render
     * @param convertView The view for the list element (might be null if it hasn't been created yet
     * @param parent The parent ViewGroup
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mLayoutResource, parent, false);
        }

        TextPlusImage artistInfo = mTextPlusImageData.get(position);

        ImageView artistImageView = (ImageView)convertView.findViewById(mImageResource);
        Picasso.with(mContext)
                .load(mTextPlusImageData.get(position).mImageURL)
                .fit()
                .centerCrop()
                .into(artistImageView);

        TextView nameText = (TextView)convertView.findViewById(mTextResource);
        nameText.setText(artistInfo.mText);

        return convertView;
    }
}
