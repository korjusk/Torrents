package org.korjus.movietorrents;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

// It's used to search movies or apply costume sorting for home page GridView
public class SearchActivity extends Activity {
    private static final String TAG = "u8i9 SearchActivity";
    EditText etSearch;
    Spinner spnImdbRating;
    Spinner spnGenres;
    Spinner spnSortBy;
    Spinner spnOrderBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Find View's
        etSearch = (EditText) findViewById(R.id.etSearch);
        spnImdbRating = (Spinner) findViewById(R.id.spnImdbRating);
        spnGenres = (Spinner) findViewById(R.id.spnGenres);
        spnSortBy = (Spinner) findViewById(R.id.spnSortBy);
        spnOrderBy = (Spinner) findViewById(R.id.spnOrderBy);

        // Adding data to spinners
        ArrayAdapter<CharSequence> spnImdbRatingAdapter = ArrayAdapter.createFromResource
                (this, R.array.array_imdb_rating, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> spnGenresAdapter = ArrayAdapter.createFromResource
                (this, R.array.array_genre, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> spnSortByAdapter = ArrayAdapter.createFromResource
                (this, R.array.array_sort_by, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> spnOrderByAdapter = ArrayAdapter.createFromResource
                (this, R.array.array_order_by, android.R.layout.simple_spinner_item);

        spnImdbRatingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGenresAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSortByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnOrderByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnImdbRating.setAdapter(spnImdbRatingAdapter);
        spnGenres.setAdapter(spnGenresAdapter);
        spnSortBy.setAdapter(spnSortByAdapter);
        spnOrderBy.setAdapter(spnOrderByAdapter);
    }

    // Clears database, makes new URL, start's intent to download Json from that URL
    public void buttonClick(View view) {
        StringBuilder url =
                new StringBuilder("https://yts.ag/api/v2/list_movies.json?quality=1080p");

        String searchValue = etSearch.getText().toString();
        long imdbRating = spnImdbRating.getSelectedItemId();

        if (searchValue.trim().length() > 0) {
            url.append("&query_term=")
                    .append(searchValue.replace(" ", "%20"));
        }
        if (imdbRating > 0) {
            url.append("&minimum_rating=")
                    .append(imdbRating + 1);
        }
        if (spnGenres.getSelectedItemId() > 0) {
            url.append("&genre=")
                    .append(spnGenres.getSelectedItem().toString());
        }
        if (spnSortBy.getSelectedItemId() > 0) {
            url.append("&sort_by=")
                    .append(spnSortBy.getSelectedItem().toString().replace(" ", "_"));
        }
        if (spnOrderBy.getSelectedItemId() > 0) {
            url.append("&order_by=asc");
        }

        //New Intent
        ((MainActivity) MainActivity.getContext())
                .getDb().close();
        this.deleteDatabase("movieDatabase.db");

        SharedPreferences settings = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("SortOrderEnum", 4);
        editor.putInt("pageNr", 1);
        editor.putLong("nrOfItemsInDb", 0);
        editor.apply();
        ParseJson.isImageAdapterNotified = false;

        Intent goToHome = new Intent(this, MainActivity.class);
        goToHome.putExtra("costumeUrl", url.toString().toLowerCase());
        startActivity(goToHome);
    }
}