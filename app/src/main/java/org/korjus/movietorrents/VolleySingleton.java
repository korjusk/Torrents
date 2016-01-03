package org.korjus.movietorrents;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

// Handles all downloading
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

    public synchronized void startDownload(final String url, final DataTypeEnum type) {
        if (type.equals(DataTypeEnum.UPDATE)) {
            Log.d(TAG, "Update");
        }

        JsonObjectRequest jsonObjRequest = new JsonObjectRequest
                (url, null, new Response.Listener<JSONObject>() {

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
                        Toast.makeText(MainActivity.getContext(),
                                "Error at loading from www.yts.ag - 404?", Toast.LENGTH_LONG).show();
                        Log.d(TAG, type.toString() + url + error.toString());

                        // Deletes settings to avoid this error in future
                        ((MainActivity) MainActivity.getContext()).resetSettings();

                        Intent goToCustomMenu = new
                                Intent(MainActivity.getContext(), SearchActivity.class);

                        // Deletes backs tack to avoid this error in future
                        goToCustomMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

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

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static void longInfo(String str) {
        if (str.length() > 4000) {
            Log.d(TAG, str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.d(TAG, str);
    }

}
