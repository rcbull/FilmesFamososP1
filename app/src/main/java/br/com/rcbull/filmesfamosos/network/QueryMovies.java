package br.com.rcbull.filmesfamosos.network;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.URL;

import br.com.rcbull.filmesfamosos.models.DatasetMovies;

/**
 * query movies in MovieDB API
 * Created by rcb on 01/04/2018.
 */

public class QueryMovies extends AsyncTaskLoader<DatasetMovies> {

    private final WeakReference<Activity> mActivity;
    private final boolean mPopular;
    private final int mPage;

    private QueryMovies(Activity activity, boolean popular, int page) {
        super(activity);

        this.mActivity = new WeakReference<>(activity);
        this.mPopular = popular;
        this.mPage = page;
    }

    /*
    Load popular movies
     */
    public static QueryMovies createPopularMoviesLoader(Activity activity, int page) {
        return new QueryMovies(activity, true, page);
    }

    /*
    Load top rated movies
     */
    public static QueryMovies createTopRatedMoviesLoader(Activity activity, int page) {
        return new QueryMovies(activity, false, page);
    }

    @Override
    public DatasetMovies loadInBackground() {
        Activity context = mActivity.get();
        if (context == null) return null;

        try {
            URL url = NetworkUtils.getMoviesUrl(mPopular, mPage);
            if (url == null)
                return null;

            Bundle response = NetworkUtils.getResponseFromHttpUrl(url);
            if (response.containsKey("DATA")) {
                Type type = new TypeToken<DatasetMovies>() {
                }.getType();
                return new Gson().fromJson(response.getString("DATA"), type);
            } else
                return null;
        } catch (Exception e) {
            return null;
        }
    }
}
