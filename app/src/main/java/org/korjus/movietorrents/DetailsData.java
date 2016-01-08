package org.korjus.movietorrents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

// Downloads and parses additional data about the movies
// Contains inner class for credit information
public class DetailsData {
    private static final String TAG = "u8i9 DetailsData";
    public static ArrayList<Credits> credits = new ArrayList<>();
    private int budget;
    private String overview;
    private long revenue;
    private String tagline;
    private String title;
    private StringBuilder countries = new StringBuilder();
    private JSONObject nextItem;

    // Decodes json into DetailsData object
    public static DetailsData fromJson(JSONObject response) {
        DetailsData detailsData = new DetailsData();

        // Deserialize json into object fields
        try {
            detailsData.budget = response.getInt("budget");
            detailsData.overview = response.getString("overview");
            detailsData.revenue = response.getLong("revenue");
            detailsData.tagline = response.getString("tagline");
            detailsData.title = response.getString("original_title");

            JSONArray production_countries = response.getJSONArray("production_countries");

            // If there is countries then add them to StringBuilder
            if (!production_countries.isNull(0)) {
                JSONObject currentItem = production_countries.getJSONObject(0);
                detailsData.countries.append(currentItem.getString("iso_3166_1"));

                for (int e = 1; e < production_countries.length(); e++) {
                    detailsData.countries.append(", ");
                    detailsData.nextItem = production_countries.getJSONObject(e);
                    detailsData.countries.append(detailsData.nextItem.getString("iso_3166_1"));
                }
            }

            // Getting credits information
            JSONObject credRoot = response.getJSONObject("credits");
            JSONArray credArray = credRoot.getJSONArray("cast");

            for (int i = 0; i < credArray.length(); i++) {
                JSONObject current = credArray.getJSONObject(i);
                String name = current.getString("name");
                String poster = current.getString("profile_path");
                if (!poster.equals("null")) {
                    DetailsData.credits.add(detailsData.new Credits(name, poster));
                    DetailsActivity.detailsFragment.getAdapter().notifyItemInserted(i);
                    DetailsActivity.detailsFragment.setCreditsImageVisible();
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return detailsData;

    }

    public String getTagline() {
        return tagline;
    }

    public String getBudget() {
        return String.valueOf(budget / 1000000);
    }

    public String getCountries() {
        return countries.toString();
    }

    public String getOverview() {
        return overview;
    }

    public String getRevenue() {
        return String.valueOf(revenue / 1000000);
    }


    // Inner class for credits
    public class Credits {
        String name;
        String poster;

        public Credits(String name, String poster) {
            this.name = name;
            this.poster = poster;
        }

        public String getName() {
            return name;
        }

        public String getPoster() {
            return "http://image.tmdb.org/t/p/w342" + poster;
        }

    }

}
