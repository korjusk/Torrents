package org.korjus.movietorrents;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


public class DetailsFragment extends Fragment {
    private static final String TAG = "u8i9 DetailsFragment";
    CreditsImageAdapter adapter;
    Movie movie;
    MainActivity mainActivity;

    public DetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movie = DetailsActivity.movie;
        VolleySingleton.getInstance(getContext())
                .startDownload(movie.getUrlMain(), DataTypeEnum.DETAILS_MAIN);
        mainActivity = (MainActivity) MainActivity.getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        // Find Views
        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        TextView tvImdbRating = (TextView) view.findViewById(R.id.tvRatingV);
        TextView tvRuntime = (TextView) view.findViewById(R.id.tvRuntimeV);
        TextView tvLanguage = (TextView) view.findViewById(R.id.tvLanguageV);
        TextView tvGenres = (TextView) view.findViewById(R.id.tvGenres);
        TextView tvSeeds = (TextView) view.findViewById(R.id.tvSeedsV);
        TextView tvSize = (TextView) view.findViewById(R.id.tvSizesV);
        final ImageView ivPoster = (ImageView) view.findViewById(R.id.ivPoster);

        // Set Value to views
        tvTitle.setText(Html.fromHtml
                ("<b>" + movie.getTitle() + "</b>" + " (" + movie.getYear() + ")"));
        tvImdbRating.setText(movie.getImdbRating());
        tvRuntime.setText(Html.fromHtml
                ("Runtime: " + "<font color=\"#000000\">" + movie.getRuntime() + "</font>"));
        tvLanguage.setText(Html.fromHtml
                ("Language: " + "<font color=\"#000000\">" + movie.getLanguage() + "</font>"));
        tvGenres.setText(movie.getGenres());
        tvSeeds.setText(Html.fromHtml
                ("Seeds: " + "<font color=\"#000000\">" + movie.getSeeds() + "</font>"));
        tvSize.setText(Html.fromHtml
                ("File Size: " + "<font color=\"#000000\">" + movie.getSize_gb() +
                        "</font>" + "GB"));

        ivPoster.getLayoutParams().width = (int) (mainActivity.getDisplayWith() * 0.45);

        Picasso.with(mainActivity)
                .load(movie.getPoster())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(ivPoster);


        // Setup layout manager for items
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        // Control orientation of the items
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);


        // Lookup the recyclerview in activity layout
        RecyclerView rvContacts = (RecyclerView) view.findViewById(R.id.rvCreditsContainer);
        // Empty list before adding to atapter
        DetailsData.credits.clear();
        // Create adapter passing in the data
        adapter = new CreditsImageAdapter(DetailsData.credits);

        // Set layout manager to position the items
        rvContacts.setLayoutManager(layoutManager);

        // Attach the adapter to the recyclerview to populate items
        rvContacts.setAdapter(adapter);

        rvContacts.getLayoutParams().height = CreditsImageAdapter.imageHeight + 70;
        // todo update to wrap content


        return view;
    }


    public void updateUI(DetailsData detailsData) {
        TextView tvPlot = (TextView) getView().findViewById(R.id.tvPlot);
        TextView tvTagline = (TextView) getView().findViewById(R.id.tvTagline);
        TextView tvBudget = (TextView) getView().findViewById(R.id.tvBudgetV);
        TextView tvRevenue = (TextView) getView().findViewById(R.id.tvRevenueV);
        TextView tvProduction = (TextView) getView().findViewById(R.id.tvProductionV);

        tvPlot.setText(detailsData.getOverview());
        tvTagline.setText(detailsData.getTagline());
        if (!detailsData.getBudget().equals("0")) {
            tvBudget.setText(Html.fromHtml
                    ("Budget: " + "<font color=\"#000000\">" +
                            detailsData.getBudget() + "</font>" + "m"));
        } else {
            tvBudget.setVisibility(View.GONE);
        }
        if (!detailsData.getRevenue().equals("0")) {
            tvRevenue.setText(Html.fromHtml
                    ("Revenue: " + "<font color=\"#000000\">" +
                            detailsData.getRevenue() + "</font>" + "m"));
        } else {
            tvRevenue.setVisibility(View.GONE);
        }
        if (!detailsData.getCountries().equals("")) {
            tvProduction.setText(Html.fromHtml
                    ("Countries: " + "<font color=\"#000000\">" +
                            detailsData.getCountries() + "</font>"));
        } else {
            tvProduction.setVisibility(View.GONE);
        }

    }

    public void setCreditsImageVisible() {
        RecyclerView rvContacts = (RecyclerView) getView().findViewById(R.id.rvCreditsContainer);
        rvContacts.setVisibility(View.VISIBLE);
    }

    public CreditsImageAdapter getAdapter() {
        return adapter;
    }
}
