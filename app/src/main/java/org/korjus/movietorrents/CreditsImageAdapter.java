package org.korjus.movietorrents;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;


// The adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class CreditsImageAdapter extends
        RecyclerView.Adapter<CreditsImageAdapter.ViewHolder> {
    private static final String TAG = "u8i9CreditsImageAdapter";
    public static int imageHeight;
    private List<DetailsData.Credits> credits;
    private MainActivity mainActivity;
    private int imageWidth;

    // Pass in the data into the constructor and calculate image size
    public CreditsImageAdapter(List<DetailsData.Credits> credit) {
        credits = credit;
        mainActivity = (MainActivity) MainActivity.getContext();
        imageWidth = mainActivity.getDisplayWith() / 3;
        imageHeight = (int) (imageWidth * 1.5);
    }

    // Inflating a layout from XML and return the holder
    @Override
    public CreditsImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_credits, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(CreditsImageAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        DetailsData.Credits credits = this.credits.get(position);

        // Set item views based on the data model
        TextView textView = viewHolder.tvName;
        textView.setText(credits.getName());

        ImageView ivCreditsImage = viewHolder.ivCreditsImage;
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(imageWidth, imageHeight);
        ivCreditsImage.setLayoutParams(layoutParams);
        ivCreditsImage.setScaleType(ImageView.ScaleType.FIT_XY);

        Picasso.with(mainActivity)
                .load(credits.getPoster())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(ivCreditsImage);
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return credits.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView tvName;
        public ImageView ivCreditsImage;
        public RecyclerView rvContacts;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.tvCreditsName);
            ivCreditsImage = (ImageView) itemView.findViewById(R.id.ivCreditsImage);
            rvContacts = (RecyclerView) itemView.findViewById(R.id.rvCreditsContainer);
        }
    }
}


