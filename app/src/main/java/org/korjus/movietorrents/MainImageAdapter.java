package org.korjus.movietorrents;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

// Adapter for home page GridView
public class MainImageAdapter extends BaseAdapter {
    private static final String TAG = "u8i9 MainImageAdapter";
    private Context context;
    private MainActivity mainActivity;
    private int posterWidth;
    private int posterHeight;

    // Pass in the context into the constructor and calculate image size
    public MainImageAdapter(Context c, int displayWidth) {
        context = c;
        mainActivity = (MainActivity) context;
        posterWidth = displayWidth / 2;
        posterHeight = (int) (posterWidth * 1.5);
    }

    // Create a new ImageView for each item referenced by the Adapter.
    public View getView(int position,
                        View convertView,
                        ViewGroup parent) {
        ImageView imageView;
        final long pos = (long) position + 1;
        Movie movie = cupboard().withDatabase(mainActivity.getDb()).get(Movie.class, pos);
        String src = movie.getPoster();

        if (convertView == null) {
            // If it's not recycled, initialize some attributes
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(posterWidth, posterHeight));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            imageView = (ImageView) convertView;
            // When you get a recycled item, check if it's not already the one you want to display
            String newSrc = (String) imageView.getTag();
            if (src.equals(newSrc)) {
                // If so, return it directly
                return imageView;
            }
        }
        // Load images to gridView with picasso
        if (mainActivity.getNrOfItemsInDb() > 0) {

            Picasso.with(mainActivity)
                    .load(movie.getPoster())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(imageView);

            imageView.setTag(src);

            preFetch(pos);
        }
        // Set on click listener to gridview and start new activity if pressed
        mainActivity.getGridView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View v,
                                    int position,
                                    long id) {
                long movieNrInDb = (long) position + 1;

                // new intent
                Intent detailsIntent = new Intent(mainActivity, DetailsActivity.class);
                detailsIntent.putExtra("nr", movieNrInDb);
                mainActivity.startActivity(detailsIntent);

            }
        });

        return imageView;
    }

    public int getCount() {
        return (int) mainActivity.getNrOfItemsInDb();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    private void preFetch(long pos){
        long fetchPos = pos + 4;
        if (fetchPos <= getCount()){
            Picasso.with(mainActivity)
                    .load(cupboard().withDatabase(mainActivity.getDb()).get(Movie.class, fetchPos).getPoster())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .fetch();
        }
    }
}
