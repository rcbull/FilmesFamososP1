package br.com.rcbull.filmesfamosos;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import br.com.rcbull.filmesfamosos.databinding.ActivityMainBinding;
import br.com.rcbull.filmesfamosos.models.DatasetMovies;
import br.com.rcbull.filmesfamosos.models.Movie;
import br.com.rcbull.filmesfamosos.models.MovieDetails;
import br.com.rcbull.filmesfamosos.network.QueryMovies;
import br.com.rcbull.filmesfamosos.network.NetworkUtils;

/**
 * MainActivity
 * <p>
 * Show movie grid and fab button to load movies
 * <p>
 * Created by rcb on 01/04/2018.
 */
public class MainActivity extends AppCompatActivity implements
        MoviesGridAdapter.MovieClickCallback, LoaderManager.LoaderCallbacks,
        SwipeRefreshLayout.OnRefreshListener {

    /*
    default values to query movies
     */
    private static final int LOADER_POPULAR_MOVIES_ID = 1001;
    private static final int LOADER_TOP_RATED_MOVIES_ID = 1002;
    private static final int LOADER_MORE_POPULAR_MOVIES_ID = 1011;
    private static final int LOADER_MORE_TOP_RATED_MOVIES_ID = 1012;

    private ActivityMainBinding mBinding;

    private DatasetMovies datasetMovies;
    private boolean popular = true;
    private int lastItemPosition = -1;
    private int firstVisibleItemPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(mBinding.toolbar);

        mBinding.swipeRefresh.setOnRefreshListener(this);
        mBinding.swipeRefresh.setRefreshing(false);

        initGrid();
        initData(savedInstanceState);
        initFab();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        hideProgressBar();
        if (savedInstanceState == null) return;
        if (!savedInstanceState.containsKey("DATASET")) return;
        if (!savedInstanceState.containsKey("POPULAR")) return;

        popular = savedInstanceState.getBoolean("POPULAR");
        loadMoreMovies();

        datasetMovies = savedInstanceState.getParcelable("DATASET");

        if (savedInstanceState.containsKey("FIRST_VISIBLE_ITEM_POS"))
            firstVisibleItemPosition = savedInstanceState.getInt("FIRST_VISIBLE_ITEM_POS");
        else
            firstVisibleItemPosition = 0;

        MoviesGridAdapter adapter = (MoviesGridAdapter) mBinding.listRecyclerview.getAdapter();
        adapter.setList(datasetMovies.getResults());
        adapter.notifyDataSetChanged();

        mBinding.listRecyclerview.scrollToPosition(firstVisibleItemPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (datasetMovies != null)
            outState.putParcelable("DATASET", datasetMovies);
        outState.putInt("FIRST_VISIBLE_ITEM_POS", firstVisibleItemPosition);
        outState.putBoolean("POPULAR", popular);
    }

    private void showProgressBar() {
        mBinding.swipeRefresh.setRefreshing(true);
    }

    private void hideProgressBar() {
        mBinding.swipeRefresh.setRefreshing(false);
    }

    private void initGrid() {
        /*
         * The second parameter is for the number of rows.
         * the third parameter is for the horizontal scroll.
         * the fourth parameter is boolean, when it set to false, layout from start to end
         */
        GridLayoutManager gridHorizontal = new GridLayoutManager(this,
                2,
                GridLayoutManager.VERTICAL,
                false);
        mBinding.listRecyclerview.setLayoutManager(gridHorizontal);
        mBinding.listRecyclerview.setHasFixedSize(true);

        final MoviesGridAdapter moviesAdapter = new MoviesGridAdapter(this);
        mBinding.listRecyclerview.setAdapter(moviesAdapter);

        mBinding.listRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean listScrolling = false;
            boolean shouldUpdate = false;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItems = mBinding.listRecyclerview.getAdapter().getItemCount();
                GridLayoutManager layoutManager = (GridLayoutManager)
                        recyclerView.getLayoutManager();
                lastItemPosition = layoutManager.findLastVisibleItemPosition();
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                shouldUpdate = lastItemPosition == totalItems - 1;

                if (!listScrolling && shouldUpdate)
                    loadMoreMovies();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        listScrolling = false;
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        listScrolling = true;
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        listScrolling = true;
                        break;
                }

                if (!listScrolling && shouldUpdate)
                    loadMoreMovies();
            }
        });
    }

    synchronized private void loadMoreMovies() {
        Log.i("FilmesFamosos:", "loadMoreMovies");
        if (datasetMovies != null && datasetMovies.getPage() < datasetMovies.getTotalPages()) {
            final int loader_id = popular ? LOADER_MORE_POPULAR_MOVIES_ID : LOADER_MORE_TOP_RATED_MOVIES_ID;

            loadData(loader_id);
        }
    }

    private void initData(Bundle savedInstanceState) {
        Log.i("FilmesFamosos:", "initData");
        if (savedInstanceState == null || !savedInstanceState.containsKey("DATASET"))
            onRefresh();
    }

    private void initFab() {
        Log.i("FilmesFamosos:", "initFab");
        mBinding.fab.setBackgroundResource(R.drawable.ic_star_white);
        mBinding.fab.setOnClickListener(v -> {
            popular = !popular;
            updateMovieSort();

            onRefresh();
        });
    }


    @Override
    public void onRefresh() {
        int filter = popular ? LOADER_POPULAR_MOVIES_ID : LOADER_TOP_RATED_MOVIES_ID;

        loadData(filter);
    }

    private void loadData(int popular) {
        mBinding.swipeRefresh.setRefreshing(true);

        if (NetworkUtils.isDeviceOnline(this)) {
            getSupportLoaderManager()
                    .restartLoader(popular, null, MainActivity.this)
                    .forceLoad();
        } else {
            Snackbar.make(mBinding.coordinatorView, R.string.error_no_internet,
                    Snackbar.LENGTH_LONG).setAction(R.string.retry, v -> {
                getSupportLoaderManager()
                        .restartLoader(popular, null, MainActivity.this)
                        .forceLoad();
            }).show();
            mBinding.swipeRefresh.setRefreshing(false);
        }
    }

    private void updateMovieSort() {
        if (popular) {
            mBinding.toolbar.setTitle(R.string.title_popular_movies);
            mBinding.fab.setImageResource(R.drawable.ic_star_white);
        } else {
            mBinding.toolbar.setTitle(R.string.title_top_rated_movies);
            mBinding.fab.setImageResource(R.drawable.ic_movie_white);
        }
    }

    private void handleMovies() {
        if (datasetMovies != null) {
            MoviesGridAdapter adapter = (MoviesGridAdapter) mBinding.listRecyclerview.getAdapter();
            adapter.setList(datasetMovies.getResults());
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onListNewsClick(Movie movie) {
        Intent intent = new Intent(this, MovieDetails.class);
        intent.putExtra("MOVIE", movie);
        startActivity(intent);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_MORE_POPULAR_MOVIES_ID:
                showProgressBar();
                int nextPage = datasetMovies.getPage() + 1;
                return QueryMovies.createPopularMoviesLoader(this, nextPage);

            case LOADER_POPULAR_MOVIES_ID:
                showProgressBar();
                return QueryMovies.createPopularMoviesLoader(this, 1);

            case LOADER_MORE_TOP_RATED_MOVIES_ID:
                showProgressBar();
                nextPage = datasetMovies.getPage() + 1;
                return QueryMovies.createTopRatedMoviesLoader(this, nextPage);

            case LOADER_TOP_RATED_MOVIES_ID:
                showProgressBar();
                return QueryMovies.createTopRatedMoviesLoader(this, 1);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        hideProgressBar();

        DatasetMovies newDataset = (DatasetMovies) data;
        switch (loader.getId()) {
            case LOADER_POPULAR_MOVIES_ID:
            case LOADER_TOP_RATED_MOVIES_ID:
                datasetMovies = newDataset;
                break;

            case LOADER_MORE_POPULAR_MOVIES_ID:
            case LOADER_MORE_TOP_RATED_MOVIES_ID:
                datasetMovies.setPage(newDataset.getPage());
                datasetMovies.getResults().addAll(((DatasetMovies) data).getResults());
                break;
        }

        handleMovies();
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }
}
