package org.korjus.movietorrents;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = "u8i9 DetailsActivity";
    public static Movie movie;
    public static DetailsFragment detailsFragment;
    private long movieNrInDb;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Movie position in Database
        Intent intent = getIntent();
        movieNrInDb = intent.getLongExtra("nr", 0L);

        // Main Database
        db = ((MainActivity) MainActivity.getContext()).getDb();

        // Current Movie
        movie = cupboard().withDatabase(db).get(Movie.class, movieNrInDb);

        // Replace placeholder with detailsFragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        detailsFragment = new DetailsFragment();
        ft.replace(R.id.placeholderForFragment, detailsFragment);
        ft.commit();

        initiateFloatingActionButton();
    }

    private void initiateFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences settings = getSharedPreferences("settings", 0);
                boolean isLinkMagnet = settings.getBoolean("menu_magnet", false);
                if (isLinkMagnet) {
                    magnetTorrent();
                } else {
                    downloadTorrent();
                }
            }
        });
    }

    public void magnetTorrent() {
        // New intent with magnet torrent URL (magnet:?xt=urn:btih:TORRENT_HASH&dn=...)
        Intent magnetTorrent = new Intent(Intent.ACTION_VIEW);
        magnetTorrent.setData(Uri.parse(movie.getMagnetTorrent()));

        // Get the package manager
        PackageManager packageManager = getPackageManager();
        // Get activities that can handle the intent
        List<ResolveInfo> activities = packageManager.queryIntentActivities(magnetTorrent, 0);
        // If 1 or more were returned then it safe to start intent
        boolean isIntentSafe = activities.size() > 0;

        if (isIntentSafe) {
            startActivity(magnetTorrent);
            Log.d(TAG, "Downloading file to phone: " +
                    movie.getFileName() + ", from: " + movie.getMagnetTorrent());
        } else {
            // If there's no activities that can handle magnet torrent links

            // Snackbar download button click listener
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Search torrent aps in Play Store intent
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse("market://search?q=torrent"));
                        startActivity(goToMarket);
                    } catch (android.content.ActivityNotFoundException e) {
                        // Search torrent aps in web if there's no play store installed
                        Intent goToWebMarket = new Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse("https://play.google.com/store/search?q=torrent"));
                        startActivity(goToWebMarket);
                    }
                }
            };

            final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                    .coordinatorLayout);
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Didn't find any torrent application.", Snackbar.LENGTH_LONG)
                    .setAction("Download", onClickListener)
                    .setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
        }
    }

    private void downloadTorrent() {
        // Download torrent to /torrent/ directory.
        DownloadManager.Request request = new DownloadManager
                .Request(Uri.parse(movie.getTorrentUrl()));
        request.setTitle(movie.getTitleLong());
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager
                    .Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir("/torrents/", movie.getFileName());

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Log.d(TAG, "Downloading torrent file: " +
                movie.getFileName() + ", from: " + movie.getTorrentUrl());
        Snackbar.make(this.findViewById(android.R.id.content),
                "Download Successful!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        SharedPreferences settings = getSharedPreferences("settings", 0);

        boolean isHdPoster = settings.getBoolean("hdPoster", false);
        boolean isMenuMagnet = settings.getBoolean("menu_magnet", false);

        MenuItem hd = menu.findItem(R.id.menu_hd);
        MenuItem magnet = menu.findItem(R.id.menu_magnet);

        hd.setChecked(isHdPoster);
        magnet.setChecked(isMenuMagnet);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        SharedPreferences settings = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();

        switch (id) {
            case R.id.menu_hd:
                item.setChecked(!item.isChecked());
                editor.putBoolean("hdPoster", item.isChecked());
                break;
            case R.id.menu_magnet:
                item.setChecked(!item.isChecked());
                editor.putBoolean("menu_magnet", item.isChecked());
                break;
            case R.id.menu_latest:
                saveSettingsAndResetDb(1);
                break;
            case R.id.menu_top_rated:
                saveSettingsAndResetDb(2);
                break;
            case R.id.menu_custom:
            case R.id.menu_search:
                Intent goToCustomMenu = new Intent(this, SearchActivity.class);
                startActivity(goToCustomMenu);
                break;
            case R.id.menu_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = movie.getImdbUrl() + "\n\n\n" + movie.getSize_gb() +
                        "GB torrent with " + movie.getSeeds() + " seeders:\n" +
                        movie.getTorrentUrl() + "\n\nMagnet link: \n" + movie.getMagnetTorrent();
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, movie.getTitleLong());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
        }
        editor.apply();
        return super.onOptionsItemSelected(item);
    }

    public void goToImdb(View view) {
        Intent goToImdb = new Intent(Intent.ACTION_VIEW);
        goToImdb.setData(Uri.parse(movie.getImdbUrl()));
        startActivity(goToImdb);
    }

    public void goToFullScreen(View view) {
        Intent goToFullScreen = new Intent(this, PosterPopupActivity.class);
        goToFullScreen.putExtra("url", movie.getPosterHd());
        startActivity(goToFullScreen);
    }

    public void saveSettingsAndResetDb(int sortOrder) {
        db.close();
        this.deleteDatabase("movieDatabase.db");

        SharedPreferences settings = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt("SortOrderEnum", sortOrder);
        editor.putInt("pageNr", 1);
        editor.putLong("nrOfItemsInDb", 0);
        editor.apply();

        ParseJson.isImageAdapterNotified = false;
        NavUtils.navigateUpFromSameTask(this);
    }

}
