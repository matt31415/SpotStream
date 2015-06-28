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
 * The ArtistArrayAdapter is used when working with pairs of images and strings.  In this case
 * the image will be represented by URLs pointing to image resources.   The adapter works with
 * ArrayLists of Artist objects (a really simple object containing a string and a URL  that
 * points to an image).
 *
 * This was inspired by:
 *   http://www.ezzylearning.com/tutorial/customizing-android-listview-items-with-custom-arrayadapter
 * although I didn't like that authors use of theView tag to store random data, so I'm risking a
 * bit of a performance hit instead..
 */
public class ArtistArrayAdapter extends ArrayAdapter<Artist> {
    private final String LOG_TAG = ArtistArrayAdapter.class.getSimpleName();

    private Context mContext;
    private int mLayoutResource;
    private ArrayList<Artist> mArtistData;
    private int mTextResource;
    private int mImageResource;


    /**
     * Basic cosnstructor to override Array Adapeter.
     *
     * @param context
     * @param resource The resource id for the layout containing a single item from the list
     * @param objects The list of Artist objects that we wish to add to the adapter.
     * @param textResource The resource id for the field into which we want to place the text
     * @param imageResource The resource id for the field into which we want to place the image
     */
    public ArtistArrayAdapter(
            Context context,
            int resource,
            ArrayList<Artist> objects,
            int textResource,
            int imageResource
    ) {
        super (context,resource,objects);

        mContext = context;
        mLayoutResource = resource;
        mArtistData = objects;
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

        Artist artistInfo = mArtistData.get(position);

        ImageView artistImageView = (ImageView) convertView.findViewById(mImageResource);

        if (artistInfo.mImageURL != null) {
            Picasso.with(mContext)
                    .load(mArtistData.get(position).mImageURL)
                    .fit()
                    .centerCrop()
                    .into(artistImageView);
        }
        else {
            artistImageView.setImageResource(R.mipmap.profile);
        }

        TextView nameText = (TextView)convertView.findViewById(mTextResource);
        nameText.setText(artistInfo.mName);

        return convertView;
    }
}
