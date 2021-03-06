package org.korjus.movietorrents;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;

// Handles all JSON downloading
// https://github.com/mcxiaoke/android-volley
public class VolleySingleton {
    private static final String TAG = "u8i9 VolleySingleton";
    private static VolleySingleton instance;
    private static Context context;
    private RequestQueue requestQueue;

    private VolleySingleton(Context context) {
        VolleySingleton.context = context;
        // This requestQueue will be used for whole lifecycle of this app
        requestQueue = getRequestQueue();
    }

    //  For singleton pattern
    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    // Download json data
    public synchronized void startDownload(final String url, final DataTypeEnum type) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest
                (url, null, new Response.Listener<JSONObject>() {

                    // Downloading successful! Response:
                    @Override
                    public void onResponse(JSONObject response) {
                        switch (type) {
                            case HOME:
                                ParseJson parseJson = new ParseJson();
                                parseJson.extractData(response);
                                break;
                            case DETAILS_MAIN:
                                DetailsData detailsData = DetailsData.fromJson(response);
                                DetailsActivity.detailsFragment.updateUI(detailsData);
                                break;
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Download NOT successful
                        if(isInternetAvailable()) {
                            Toast.makeText(MainActivity.getContext(),
                                    "Error at loading data - 404?", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.getContext(),
                                    "No internet connection.", Toast.LENGTH_LONG).show();
                        }


                        // Deletes settings to avoid this error in future
                        ((MainActivity) MainActivity.getContext()).resetSettings();

                        Intent goToCustomMenu = new
                                Intent(MainActivity.getContext(), SearchActivity.class);

                        // Deletes back stack so user can't come back in here.
                        // Works only in honeycomb+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            goToCustomMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        }

                        // Return's back to Search Activity
                        MainActivity.getContext().startActivity(goToCustomMenu);
                    }
                });

        // Fixes the slow response problem
        jsonObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000, // Tries 5 seconds to download Json from web
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        addToRequestQueue(jsonObjRequest);
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    private <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    // Ping the Google server
    private boolean isInternetAvailable() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
