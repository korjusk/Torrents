package org.korjus.movietorrents;

import android.os.Build;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

// Data about movies, stored in SQLite Database
public class Movie {
    private static final String TAG = "u8i9 Movie";
    public static boolean isPosterHd = false;
    public Long _id; // for cupboard

    static {
        // register our models
        cupboard().register(Movie.class);
    }

    private String imdbCode;
    private String title;
    private String titleLong;
    private String slug;
    private String year;
    private double imdbRating;
    private String runtime;
    private String language;
    private String poster;
    private String genres;
    private String torrentUrl;
    private String torrentHash;
    private int torrentSeeds;
    private double torrentBytesSize;

    public Movie(String genres, String imdbCode, double imdbRating, String language, String poster,
                 String runtime, String slug, String torrentHash, String title, String titleLong,
                 int torrentSeeds, double torrentBytesSize, String torrentUrl, String year) {
        this.genres = genres;
        this.imdbCode = imdbCode;
        this.imdbRating = imdbRating;
        this.language = language;
        this.poster = poster;
        this.runtime = runtime;
        this.slug = slug;
        this.torrentHash = torrentHash;
        this.title = title;
        this.titleLong = titleLong;
        this.torrentSeeds = torrentSeeds;
        this.torrentBytesSize = torrentBytesSize;
        this.torrentUrl = torrentUrl;
        this.year = year;
    }

    public Movie() {
    }

    public String getPoster() {
        if (isPosterHd) {
            return getPosterHd();
        } else {
            return poster;
        }
    }

    public String getPosterHd() {
        return poster.replace("medium-cover.jpg", "large-cover.jpg");
    }

    public String getGenres() {
        return genres;
    }

    public String getImdbRating() {
        return String.valueOf(imdbRating);
    }

    public String getLanguage() {
        return language;
    }

    public String getRuntime() {
        int hours = Integer.valueOf(runtime) / 60;
        int minutes = Integer.valueOf(runtime) % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    public String getTitleLong() {
        return titleLong;
    }

    public String getSeeds() {
        return String.valueOf(torrentSeeds);
    }

    public String getSize_gb() {
        return String.format("%.1f", torrentBytesSize / 1073741824);
    }

    public String getUrlMain() {
        // http://api.themoviedb.org/3/movie/tt0137523?external_source=imdb_id&api_key=477c5c8124a39a23666fe14b445cea78&append_to_response=credits,trailers
        return "http://api.themoviedb.org/3/movie/" + imdbCode +
                "?external_source=imdb_id&api_key=477c5c8124a39a23666fe14b445cea78&append_to_response=credits,trailers";
    }

    public String getImdbUrl() {
        return "http://www.imdb.com/title/" + imdbCode;
    }

    public String getMagnetTorrent() {
        // magnet:?xt=urn:btih:TORRENT_HASH&dn=Url+Encoded+Movie+Name&tr=http://track.one:1234/announce&tr=udp://track.two:80
        String tracker = "&tr=udp://open.demonii.com:1337/announce&tr=udp://tracker.openbittorrent.com:80&tr=udp://tracker.coppersurfer.tk:6969&tr=udp://glotorrents.pw:6969/announce&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://torrent.gresille.org:80/announce&tr=udp://p4p.arenabg.com:1337&tr=udp://tracker.leechers-paradise.org:6969";
        return "magnet:?xt=urn:btih:" + getTorrentHash() + "&dn=" + getSlug() + tracker;
    }

    public String getFileName() {
        return slug + ".torrent";
    }

    public String getTorrentUrl() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // Gingerbread doesn't support https
            return torrentUrl.replace("https", "http");
        }
        return torrentUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    private String getTorrentHash() {
        return torrentHash;
    }

    private String getSlug() {
        return slug;
    }

    public String getMailBody() {
        return getImdbUrl() +
                "\n\n" + getSize_gb() + "GB torrent with " + getSeeds() + " seeders:\n"
                + getTorrentUrl() +
                "\n\nMagnet link: \n"
                + getMagnetTorrent();
    }

}