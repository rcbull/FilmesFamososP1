package br.com.rcbull.filmesfamosos.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import android.util.Log;

/**
 * File with methods to network
 * Created by rcb on 01/04/2018.
 */

public class NetworkUtils {

    private static final String API_KEY = "";

    private static final String FILTER_POPULAR_MOVIE = "https://api.themoviedb.org/3/movie/popular?api_key=" +
            API_KEY;
    private static final String FILTER_TOP_RATED_MOVIE = "https://api.themoviedb.org/3/movie/top_rated?api_key=" +
            API_KEY;

    public static URL getMoviesUrl(boolean popular, int page) {
        try {
            URL url;
            if (popular)
                // query popular movies
                url = new URL(FILTER_POPULAR_MOVIE + "&page=" + String.valueOf(page));
            else
                // query top rated movies
                url = new URL(FILTER_TOP_RATED_MOVIE + "&page=" + String.valueOf(page));

            return url;
        } catch (Exception e) {
            Log.e("FilmesFamosos:", e.getMessage());
            return null;
        }
    }

    /**
     * Processing response from http request
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static Bundle getResponseFromHttpUrl(URL url) throws IOException {
        Bundle data = new Bundle();

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        int responseCode = urlConnection.getResponseCode();
        if (responseCode == 200) {
            try {
                InputStream in = urlConnection.getInputStream();

                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                String response = null;
                if (hasInput) {
                    response = scanner.next();
                }
                scanner.close();

                data.putString("DATA", response);
            } finally {
                urlConnection.disconnect();
            }
        }
        data.putInt("SERVER_RESPONSE", responseCode);

        return data;
    }

    /*
    verify if device is online
     */
    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            return (networkInfo != null && networkInfo.isConnected());
        } else {
            Log.i("FilmesFamosos:", "Limited access");
            return false;
        }
    }
}
