package org.korjus.movietorrents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;


public class MainActivity extends Activity {
    private static final String TAG = "u8i9 MainActivity";
    private static MainActivity instance;
    private long nrOfItemsInDb = 0l;
    private int pageNr = 1;
    private String costumeUrl;
    private SQLiteDatabase db;
    private MainImageAdapter mainImageAdapter;
    private GridView gridView;
    private SortOrderEnum sortOrderEnum = SortOrderEnum.DEFAULT;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    public MainActivity() {
        // Set's context.
        instance = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences("settings", 0);
        editor = settings.edit();

        warnIfWifiDisabled();
        loadSettings();
        InstantiateDatabaseHelper();
        downloadData();

        // Set Image Adapter and on scroll listener
        mainImageAdapter = new MainImageAdapter(this);
        gridView = (GridView) findViewById(R.id.MainActivityGridView);
        gridView.setAdapter(mainImageAdapter);
        gridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                VolleySingleton.getInstance(MainActivity.getContext())
                        .startDownload(getUrl(sortOrderEnum), DataTypeEnum.HOME);
                return true;
            }
        });
    }

    private void warnIfWifiDisabled() {
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        if (!wm.isWifiEnabled()) {
            Log.d(TAG, "Wifi disabled");
            Toast.makeText(instance, "Wifi is Disabled!", Toast.LENGTH_LONG);
            Snackbar.make(this.findViewById(android.R.id.content),
                    "Wifi is Disabled!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void loadSettings() {
        pageNr = settings.getInt("pageNr", 1);
        sortOrderEnum = SortOrderEnum.fromInteger(settings.getInt("SortOrderEnum", 0));
        Movie.isPosterHd = settings.getBoolean("hdPoster", false);
        nrOfItemsInDb = settings.getLong("nrOfItemsInDb", 0);
    }

    private void InstantiateDatabaseHelper() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
    }

    public void downloadData() {
        // Download Json if there's no data in database
        if (pageNr == 1) {
            VolleySingleton.getInstance(MainActivity.getContext())
                    .startDownload(getUrl(sortOrderEnum), DataTypeEnum.HOME);
        }
    }

    private void getCostumeUrl() {
        Intent intent = getIntent();
        String costumeUrlFromIntent = intent.getStringExtra("costumeUrl");
        if(costumeUrlFromIntent != null) {
            costumeUrl = costumeUrlFromIntent;
            editor.putString("costumeUrl", costumeUrl);
            editor.apply();
        }
        if(costumeUrl == null){
            costumeUrl = settings.getString("costumeUrl",
                    "https://yts.ag/api/v2/list_movies.json?quality=1080p");
        }
    }

    private void increasePageNr() {
        pageNr++;
        editor.putInt("pageNr", pageNr);
        editor.apply();
    }

    public void resetSettings() {
        editor.putInt("SortOrderEnum", 1);
        editor.putInt("pageNr", 1);
        editor.putLong("nrOfItemsInDb", 0);
        editor.apply();
        ParseJson.isImageAdapterNotified = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
    }

    @Override
    protected void onStop() {
        super.onStop();
        editor.putLong("nrOfItemsInDb", nrOfItemsInDb);
        editor.putString("costumeUrl", costumeUrl);
        editor.apply();
        Log.d(TAG, "onStop Settings: " + settings.getAll().toString());

    }

    private String getUrl(SortOrderEnum sortOrderEnum) {
        final String BASE_URL = "https://yts.ag/api/v2/list_movies.json?quality=1080p&";
        String pageWithNr = "&page=" + String.valueOf(pageNr);
        increasePageNr();

        switch (sortOrderEnum) {
            case DEFAULT:
                return BASE_URL + "sort_by=like_count&minimum_rating=6" + pageWithNr;
            case LATEST:
                Log.d(TAG, BASE_URL + "sort_by=date_added" + pageWithNr);
                return BASE_URL + "sort_by=date_added" + pageWithNr;
            case TOP_RATED:
                return BASE_URL + "sort_by=rating" + pageWithNr;
            case MOST_SEEDED:
                Log.d(TAG, BASE_URL + "sort_by=seeds" + pageWithNr);
                return BASE_URL + "sort_by=seeds" + pageWithNr;
            case COSTUME:
                if(costumeUrl == null){
                    getCostumeUrl();
                }
                Log.d(TAG, costumeUrl + pageWithNr);
                return costumeUrl + pageWithNr;
        }
        return "error at getURL";
    }

    public GridView getGridView() {
        return gridView;
    }

    public MainImageAdapter getMainImageAdapter() {
        return mainImageAdapter;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public long getNrOfItemsInDb() {
        return nrOfItemsInDb;
    }

    public static Context getContext() {
        return instance;
    }

    public void setNrOfItemsInDb(long nrOfItemsInDb) {
        this.nrOfItemsInDb = nrOfItemsInDb;
    }
}
