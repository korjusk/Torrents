package org.korjus.movietorrents;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends Activity {
    private static final String TAG = "u8i9 MainActivity";
    private static MainActivity instance;
    private long nrOfItemsInDb = 0L;
    private int pageNr = 1;
    private String movieQuality;
    private String customUrl;
    private SQLiteDatabase db;
    private MainImageAdapter mainImageAdapter;
    private GridView gridView;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private boolean fromSearchActivity;

    public MainActivity() {
        // Set's context.
        instance = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Crash reporting API, https://docs.fabric.io/
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences("settings", 0);
        editor = settings.edit();

        warnIfWifiDisabled();
        loadSettings();
        InstantiateDatabaseHelper();
        downloadData();
        setImageAdapterAndOnScrollListener();
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
        fromSearchActivity = settings.getBoolean("fromSearchActivity", false);
        Movie.isPosterHd = settings.getBoolean("hdPoster", false);
        nrOfItemsInDb = settings.getLong("nrOfItemsInDb", 0);
        movieQuality = settings.getString("movieQuality", "1080p");
        customUrl = settings.getString("customUrl", null);

        Log.d(TAG, "Load Settings: " + settings.getAll().toString());
    }

    private void InstantiateDatabaseHelper() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
    }

    public void downloadData() {
        // Download Json if there's no data in database
        if (pageNr == 1) {
            VolleySingleton.getInstance(MainActivity.getContext())
                    .startDownload(getUrl(), DataTypeEnum.HOME);
        }
    }

    private void setImageAdapterAndOnScrollListener() {
        mainImageAdapter = new MainImageAdapter(this, getDisplayWith());
        gridView = (GridView) findViewById(R.id.MainActivityGridView);
        gridView.setAdapter(mainImageAdapter);
        gridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                VolleySingleton.getInstance(MainActivity.getContext())
                        .startDownload(getUrl(), DataTypeEnum.HOME);
                return true;
            }
        });
    }

    private void increasePageNr() {
        pageNr++;
        editor.putInt("pageNr", pageNr);
        editor.apply();
    }

    public void resetSettings() {
        editor.putInt("pageNr", 1);
        editor.putLong("nrOfItemsInDb", 0);
        editor.putBoolean("fromSearchActivity", false);
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
        editor.putString("customUrl", customUrl);
        editor.putString("movieQuality", movieQuality);
        editor.putBoolean("fromSearchActivity", fromSearchActivity);
        editor.apply();

        //Log.d(TAG, "onStop Settings: " + settings.getAll().toString());
    }

    public int getDisplayWith() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    private String getUrl() {
        final String BASE_URL = "https://yts.ag/api/v2/list_movies.json?quality=";
        final String DEFAULT_SORT_ORDER = "&sort_by=date_added&minimum_rating=6";
        String pageWithNr = "&page=" + String.valueOf(pageNr);
        increasePageNr();

        if (fromSearchActivity){
            // Custom search
            return customUrl + pageWithNr;
        } else {
            // Default URL
            return BASE_URL + movieQuality + DEFAULT_SORT_ORDER + pageWithNr;
        }
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

    public String getMovieQuality() {
        return movieQuality;
    }
}
