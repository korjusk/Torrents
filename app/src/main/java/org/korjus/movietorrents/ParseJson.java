package org.korjus.movietorrents;

import android.content.Intent;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

// Converts Json input to Movie class and adds it to database
public class ParseJson {
    private static final String TAG = "u8i9 ParseJson";
    private static StringBuilder genres = new StringBuilder();
    public static boolean isImageAdapterNotified = false;

    public void extractData(JSONObject response) {
        try {
            MainActivity mainActivity = (MainActivity) MainActivity.getContext();
            // Correct location from where to get data
            JSONObject data = response.getJSONObject("data");
            int movieCount = data.getInt("movie_count");
            if (movieCount == 0) {
                Toast.makeText(MainActivity.getContext(),
                        "Didn't find any movie.", Toast.LENGTH_LONG).show();
                mainActivity.resetSettings();

                Intent goToCustomMenu = new Intent(MainActivity.getContext(), SearchActivity.class);
                goToCustomMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.getContext().startActivity(goToCustomMenu);

            } else {
                JSONArray arrayMovies = data.getJSONArray("movies");
                for (int i = 0; i < arrayMovies.length(); i++) {
                    JSONObject currentMovie = arrayMovies.getJSONObject(i);
                    genres.delete(0, 999);

                    // Getting data from json
                    int id = currentMovie.getInt("id");
                    String movieUrl = currentMovie.getString("url");
                    String imdbCode = currentMovie.getString("imdb_code");
                    String title = currentMovie.getString("title");
                    String titleLong = currentMovie.getString("title_long");
                    String slug = currentMovie.getString("slug");
                    String year = currentMovie.getString("year");
                    double imdbRating = currentMovie.getDouble("rating");
                    String runtime = currentMovie.getString("runtime");
                    String language = currentMovie.getString("language");
                    String trailer = currentMovie.getString("yt_trailer_code");
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
                        int tPeers = currentTorrent.getInt("peers");
                        double tSize_bytes = currentTorrent.getDouble("size_bytes");

                        if (tQuality.equals("1080p")) {
                            // Add data to database
                            long NrOfItemsInDb = cupboard()
                                    .withDatabase(mainActivity.getDb()).put(new Movie
                                    (genres.toString(), id, imdbCode, imdbRating, language, trailer,
                                            movieUrl, poster, runtime, slug, tHash, title,
                                            titleLong, tPeers, tSeeds, tSize_bytes, tUrl, year));

                            mainActivity.setNrOfItemsInDb(NrOfItemsInDb);
                        }
                    }
                }

                if (!isImageAdapterNotified) {
                    mainActivity.getMainImageAdapter().notifyDataSetChanged();
                    isImageAdapterNotified = true;
                    // For smoother scrolling it wont notify Data Set Change after first time.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

                /*
                String rt_critics_score = currentMovie.getString("rt_critics_score");
                String rt_critics_rating = currentMovie.getString("rt_critics_rating");
                String rt_audience_score = currentMovie.getString("rt_audience_score");
                String rt_audience_rating = currentMovie.getString("rt_audience_rating");
                String rt_critics_score = "88";
                String rt_critics_rating = "89";
                String rt_audience_score = "98";
                String rt_audience_rating = "99";
                */
