package org.korjus.movietorrents;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

// It's used to search movies or apply custom sorting for home page GridView
public class SearchActivity extends Activity {
    private static final String TAG = "u8i9 SearchActivity";
    EditText etSearch;
    Spinner spnImdbRating;
    Spinner spnGenres;
    Spinner spnSortBy;
    Spinner spnMovieQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Find View's
        etSearch = (EditText) findViewById(R.id.etSearch);
        spnImdbRating = (Spinner) findViewById(R.id.spnImdbRating);
        spnGenres = (Spinner) findViewById(R.id.spnGenres);
        spnSortBy = (Spinner) findViewById(R.id.spnSortBy);
        spnMovieQuality = (Spinner) findViewById(R.id.spnMovieQuality);

        // Adding data to spinners
        ArrayAdapter<CharSequence> spnImdbRatingAdapter = ArrayAdapter.createFromResource
                (this, R.array.array_imdb_rating, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> spnGenresAdapter = ArrayAdapter.createFromResource
                (this, R.array.array_genre, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> spnSortByAdapter = ArrayAdapter.createFromResource
                (this, R.array.array_sort_by, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> spnMovieQualityAdapter = ArrayAdapter.createFromResource
                (this, R.array.array_movie_quality, android.R.layout.simple_spinner_item);

        spnImdbRatingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGenresAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSortByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMovieQualityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnImdbRating.setAdapter(spnImdbRatingAdapter);
        spnGenres.setAdapter(spnGenresAdapter);
        spnSortBy.setAdapter(spnSortByAdapter);
        spnMovieQuality.setAdapter(spnMovieQualityAdapter);
    }

    // Clears database, makes new URL, start's intent to download Json from that URL
    public void buttonClick(View view) {
        SharedPreferences settings = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();

        // Base URL
        StringBuilder url =
                new StringBuilder("https://yts.ag/api/v2/list_movies.json?quality=");

        String searchValue = etSearch.getText().toString();
        long imdbRating = spnImdbRating.getSelectedItemId();

        // Add to URL everything that is inserted by user

        if (spnMovieQuality.getSelectedItemId() == 2){
            url.append("3D"); ////   Needs to be in upper case   ////
            editor.putString("movieQuality", "3D");
        } else if (spnMovieQuality.getSelectedItemId() == 1) {
            url.append("720p");
            editor.putString("movieQuality", "720p");
        } else {
            // Default value
            url.append("1080p");
            editor.putString("movieQuality", "1080p");
        }

        if (searchValue.trim().length() > 0) {
            url.append("&query_term=")
                    .append(searchValue.replace(" ", "%20").toLowerCase());
        }

        if (imdbRating > 0) {
            url.append("&minimum_rating=")
                    .append(imdbRating + 1);
        }

        if (spnGenres.getSelectedItemId() > 0) {
            url.append("&genre=")
                    .append(spnGenres.getSelectedItem().toString().toLowerCase());
        }

        if (spnSortBy.getSelectedItemId() > 0) {
            url.append("&sort_by=");

            String sortByValue = spnSortBy.getSelectedItem().toString();
            switch (sortByValue) {
                case "Rating":
                    url.append("rating");
                    break;
                case "Download count":
                    url.append("download_count");
                    break;
                case "Newest first":
                    url.append("year");
                    break;
                case "Oldest first":
                    url.append("year&order_by=asc");
                    break;
                case "Alphabetically":
                    url.append("title&order_by=asc");
                    break;
            }

        }

        // Close and delete Database so I could be recreated
        ((MainActivity) MainActivity.getContext())
                .getDb().close();
        this.deleteDatabase("movieDatabase.db");

        // Save settings
        editor.putBoolean("fromSearchActivity", true);
        editor.putInt("pageNr", 1);
        editor.putLong("nrOfItemsInDb", 0);
        editor.putString("customUrl", url.toString());
        ParseJson.isImageAdapterNotified = false;
        editor.apply();

        // Start new search intent
        Intent goToHome = new Intent(this, MainActivity.class);
        goToHome.putExtra("customUrl", url.toString());
        startActivity(goToHome);
    }
}