package org.korjus.movietorrents;

import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

// Converts Json input to Movie class and adds it to database
class ParseJson {
    private static final String TAG = "u8i9 ParseJson";
    public static boolean isImageAdapterNotified = false;
    private static StringBuilder genres = new StringBuilder();

    public void extractData(JSONObject response) {
        try {
            MainActivity mainActivity = (MainActivity) MainActivity.getContext();

            // Correct location from where to get data
            JSONObject data = response.getJSONObject("data");
            int movieCount = data.getInt("movie_count");
            if (movieCount == 0) {
                // If results came back without any movies
                Toast.makeText(MainActivity.getContext(),
                        "Didn't find any movies.", Toast.LENGTH_LONG).show();
                mainActivity.resetSettings();

                // Go back to search activity
                Intent goToCustomMenu = new Intent(MainActivity.getContext(), SearchActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    goToCustomMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                MainActivity.getContext().startActivity(goToCustomMenu);

            } else {
                JSONArray arrayMovies = data.getJSONArray("movies");
                String movieQuality = mainActivity.getMovieQuality();

                // Each movie is represented by int i
                for (int i = 0; i < arrayMovies.length(); i++) {
                    JSONObject currentMovie = arrayMovies.getJSONObject(i);
                    genres.delete(0, 999);

                    // Getting data from json
                    String imdbCode = currentMovie.getString("imdb_code");
                    String title = currentMovie.getString("title");
                    String titleLong = currentMovie.getString("title_long");
                    String slug = currentMovie.getString("slug");
                    String year = currentMovie.getString("year");
                    double imdbRating = currentMovie.getDouble("rating");
                    String runtime = currentMovie.getString("runtime");
                    String language = currentMovie.getString("language");
                    String poster = currentMovie.getString("medium_cover_image");

                    // Genres
                    JSONArray jsonGenres = currentMovie.getJSONArray("genres");
                    genres.append(jsonGenres.getString(0));
                    for (int e = 1; e < jsonGenres.length(); e++) {
                        genres.append(" | ");
                        genres.append(jsonGenres.getString(e));
                    }

                    // Torrent
                    JSONArray jsonTorrent = currentMovie.getJSONArray("torrents");
                    for (int t = 0; t < jsonTorrent.length(); t++) {
                        JSONObject currentTorrent = jsonTorrent.getJSONObject(t);

                        String tUrl = currentTorrent.getString("url");
                        String tHash = currentTorrent.getString("hash");
                        String tQuality = currentTorrent.getString("quality");
                        int tSeeds = currentTorrent.getInt("seeds");
                        double tSize_bytes = currentTorrent.getDouble("size_bytes");

                        if (tQuality.equals(movieQuality)) {
                            // Add data to database if there is torrent file with right quality
                            long NrOfItemsInDb = cupboard()
                                    .withDatabase(mainActivity.getDb()).put(new Movie
                                            (genres.toString(), imdbCode, imdbRating, language,
                                                    poster, runtime, slug, tHash, title, titleLong,
                                                    tSeeds, tSize_bytes, tUrl, year));

                            mainActivity.setNrOfItemsInDb(NrOfItemsInDb);
                        }
                    }
                }

                if (!isImageAdapterNotified) {
                    mainActivity.getMainImageAdapter().notifyDataSetChanged();
                    isImageAdapterNotified = true;
                    // For smoother scrolling it wont notify Data Set Change after first time
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}