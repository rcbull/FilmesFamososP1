package br.com.rcbull.filmesfamosos.models;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.squareup.picasso.Picasso;

import br.com.rcbull.filmesfamosos.R;
import br.com.rcbull.filmesfamosos.databinding.ActivityDetailsBinding;

/**
 * Show movie details
 * Created by rcb on 01/04/2018.
 */

public class MovieDetails extends AppCompatActivity {

    private ActivityDetailsBinding mBinding;

    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);

        parseData();
        initToolbar();
    }

    private void parseData() {
        Intent startIntent = getIntent();
        if (startIntent != null)
            mMovie = startIntent.getParcelableExtra("MOVIE");

        mBinding.titleTextview.setText(mMovie.getOriginalTitle());
        mBinding.descriptionTextview.setText(mMovie.getOverview());
        mBinding.voteAverageTextview.setText(
                getString(R.string.average_vote, mMovie.printVoteAverage()));
        mBinding.releaseDateTextview.setText(mMovie.printReleaseDate());
    }

    private void initToolbar() {
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Picasso.with(this)
                .load(mMovie.getPosterFullPath())
                .into(mBinding.image);

        mBinding.collapsingToolbar.setTitle(" ");
        mBinding.collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.defaultTextColor));
        mBinding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    mBinding.collapsingToolbar.setTitle(mMovie.getOriginalTitle());
                    mBinding.image.setAlpha(.7f);
                    isShow = true;
                } else if (isShow) {
                    // There should a space between double quote otherwise it won't work
                    mBinding.collapsingToolbar.setTitle(" ");
                    mBinding.image.setAlpha(1f);
                    isShow = false;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
