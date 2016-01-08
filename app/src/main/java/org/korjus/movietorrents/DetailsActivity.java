package org.korjus.movietorrents;

import android.Manifest;
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
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;

import java.io.File;
import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = "u8i9 DetailsActivity";
    public static Movie movie;
    public static DetailsFragment detailsFragment;
    private final int PERMISSION_WRITE_RESULT = 0;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        settings = getSharedPreferences("settings", 0);

        // Set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Main Database
        SQLiteDatabase db = ((MainActivity) MainActivity.getContext()).getDb();

        // Movie position in Database
        Intent intent = getIntent();
        long movieNrInDb = intent.getLongExtra("nr", 0L);

        // Current Movie
        movie = cupboard().withDatabase(db).get(Movie.class, movieNrInDb);

        // Replace placeholder with detailsFragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        detailsFragment = new DetailsFragment();
        ft.replace(R.id.placeholderForFragment, detailsFragment);
        ft.commit();
    }

    @Override
    protected void onResume() {
        initiateFloatingActionButton();

        // Edit button icon
        final String downloadAction = settings.getString("downloadAction", "default");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        switch (downloadAction) {
            case "default":
                fab.setImageResource(R.drawable.ic_download_white);
                break;
            case "magnet":
                fab.setImageResource(R.drawable.ic_magnet_white);
                break;
            case "share":
                fab.setImageResource(R.drawable.ic_share_white);
                break;
            case "mail":
                fab.setImageResource(R.drawable.ic_mail_white);
                break;
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        // Find menu item
        MenuItem hdPoster = menu.findItem(R.id.menu_hd);

        // Load settings
        boolean isHdPoster = settings.getBoolean("hdPoster", false);

        // Set menu checkbox values
        hdPoster.setChecked(isHdPoster);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        int id = item.getItemId();

        SharedPreferences.Editor editor = settings.edit();

        switch (id) {
            case R.id.menu_hd:
                // Toggle poster's image quality
                item.setChecked(!item.isChecked());
                editor.putBoolean("hdPoster", item.isChecked());
                break;
            case R.id.menu_custom:
            case R.id.menu_search:
                // Opens search Activity
                Intent goToCustomMenu = new Intent(this, SearchActivity.class);
                startActivity(goToCustomMenu);
                break;
            case R.id.menu_update:
                // User can manually check if there is updates in dropbox folder
                // This will be removed when the app becomes available in play store
                String updateUrl =
                        "https://www.dropbox.com/sh/6afaza65f37mlze/AADXVimhKAZDzw7d9Fc_QTuXa?dl=0";
                Intent checkUpdates = new Intent(Intent.ACTION_VIEW);
                checkUpdates.setData(Uri.parse(updateUrl));
                startActivity(checkUpdates);
                break;
            case R.id.menu_settings:
                // Opens Settings Activity where user can choose download button's action
                Intent goToSettings = new Intent(this, SettingsActivity.class);
                startActivity(goToSettings);
                break;
        }

        // Saves settings
        editor.apply();
        return super.onOptionsItemSelected(item);
    }

    private void initiateFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        // Loads user submitted button's action
        final String downloadAction = settings.getString("downloadAction", "default");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (downloadAction) {
                    case "default":
                        downloadTorrentPermission();
                        break;
                    case "magnet":
                        magnetTorrent();
                        break;
                    case "share":
                        shareTorrent();
                        break;
                    case "mail":
                        mailTorrent();
                        break;
                }
            }
        });
    }

    // Ask Permission to write to external storage if it's android 6
    private void downloadTorrentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Check if permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted!
                downloadTorrent();
            } else {
                // Request permission if it's not granted

                // Ask's permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_WRITE_RESULT);
            }
        } else {
            // If its not android 6
            downloadTorrent();
        }
    }

    // Permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_RESULT: {
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Result granted!
                    downloadTorrentPermission();

                } else {
                    // Permission denied!
                    Toast.makeText(this, "Permission denied, change settings", Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
    }

    // Downloads and saves torrent to "downloads/Movie Torrents" directory
    private void downloadTorrent() {
        // Directory location
        final File directory = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + "Movie Torrents" + File.separator);

        // Have the object build the directory structure, if needed
        //noinspection ResultOfMethodCallIgnored
        directory.mkdirs();

        // torrent file absolute path
        File path = new File(directory, movie.getFileName());

        // If file exists show snackbar with Browse button
        if (path.exists()) {
            // Browse button click listener
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Try to open file browser
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setDataAndType(Uri.parse(directory.toString()), "*/*");
                        startActivity(Intent.createChooser(intent, "Open folder"));
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                    }
                }
            };

            // Snackbar
            final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                    .coordinatorLayout);
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "File already exists in \nDownloads/Movie Torrents",
                            Snackbar.LENGTH_LONG)
                    .setAction("Browse", onClickListener)
                    .setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
        } else {
            // If file doesn't exist
            // Download torrent to downloads/Movie Torrents directory
            DownloadManager.Request request = new DownloadManager
                    .Request(Uri.parse(movie.getTorrentUrl()));
            request.setTitle(movie.getTitleLong());
            // In order for this if to run, you must use the android 3.2 to compile your app
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager
                        .Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            }
            request.setDestinationUri(Uri.fromFile(path));

            // Get download service and enqueue file
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);

            // Snackbar to notify user that download has started
            Snackbar.make(this.findViewById(android.R.id.content),
                    "Downloading...", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    // New intent with magnet torrent URL (magnet:?xt=urn:btih:TORRENT_HASH&dn=...)
    private void magnetTorrent() {
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
        } else {
            // Open play store if there's no activities that can handle magnet torrent links

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
                        Intent goToWebMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
                                .parse("https://play.google.com/store/search?q=torrent"));
                        startActivity(goToWebMarket);
                    }
                }
            };

            // Show snackbar with Download button
            final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                    .coordinatorLayout);
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Didn't find any torrent application.",
                            Snackbar.LENGTH_LONG)
                    .setAction("Download", onClickListener)
                    .setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
        }
    }

    // Share movie/torrent info/links
    private void shareTorrent() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                movie.getTitleLong() + " [Movie Torrents]");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, movie.getMailBody());
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    // Send a email in background without user interaction
    // https://github.com/yesidlazaro/GmailBackground
    private void mailTorrent() {
        String mail = settings.getString("mailAddress", "").toLowerCase();
        BackgroundMail backgroundMail = new BackgroundMail(this);
        backgroundMail.setGmailUserName("frommovietorrents@gmail.com");
        backgroundMail.setGmailPassword("");
        backgroundMail.setMailTo(mail);
        backgroundMail.setFormBody(movie.getMailBody());
        backgroundMail.setFormSubject(movie.getTitleLong() + " [Movie Torrents]");
        backgroundMail.send();
    }

    // Opens movie IMDb site
    public void goToImdb(View view) {
        Intent goToImdb = new Intent(Intent.ACTION_VIEW);
        goToImdb.setData(Uri.parse(movie.getImdbUrl()));
        startActivity(goToImdb);
    }

    // Opens movie poster in full screen
    public void goToFullScreen(View view) {
        Intent goToFullScreen = new Intent(this, PosterPopupActivity.class);
        goToFullScreen.putExtra("url", movie.getPosterHd());
        startActivity(goToFullScreen);
    }
}