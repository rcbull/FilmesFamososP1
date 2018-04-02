package br.com.rcbull.filmesfamosos;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.com.rcbull.filmesfamosos.databinding.ListItemMovieGridBinding;
import br.com.rcbull.filmesfamosos.models.Movie;

/**
 * Created by rcb on 01/04/2018.
 */

public class MoviesGridAdapter extends RecyclerView.Adapter<MoviesGridAdapter.MovieViewHolder> {

    private ArrayList<Movie> mList;

    @Nullable
    private final MovieClickCallback mOnClickListener;

    public interface MovieClickCallback {
        void onListNewsClick(Movie movie);
    }

    MoviesGridAdapter(@Nullable MovieClickCallback movieClickCallback) {
        mOnClickListener = movieClickCallback;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemMovieGridBinding mBinding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.list_item_movie_grid,
                        parent,
                        false);

        return new MovieViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie news = mList.get(position);

        holder.bindData(news);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }


    public void setList(final List<Movie> newList) {
        final ArrayList<Movie> tempList = new ArrayList<>(newList);

        if (mList == null) {
            mList = tempList;
            notifyItemRangeInserted(0, mList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mList.size();
                }

                @Override
                public int getNewListSize() {
                    return tempList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition).equals(tempList.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Movie newItem = tempList.get(newItemPosition);
                    Movie oldItem = mList.get(oldItemPosition);
                    return oldItem.displayEquals(newItem);
                }
            });
            mList = tempList;
            result.dispatchUpdatesTo(this);
        }
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ListItemMovieGridBinding mBinding;

        MovieViewHolder(ListItemMovieGridBinding binding) {
            super(binding.getRoot());

            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setFocusable(true);

            this.mBinding = binding;
        }

        @Override
        public void onClick(View v) {
            Movie clicked = mBinding.getMovie();

            if (mOnClickListener != null)
                mOnClickListener.onListNewsClick(clicked);
        }

        void bindData(Movie movie) {
            mBinding.setMovie(movie);

            String averageVote = mBinding.getRoot().getContext()
                    .getString(R.string.average_vote, movie.printVoteAverage());
            mBinding.voteAverageTextview.setText(averageVote);
            mBinding.titleTextview.setText(movie.getOriginalTitle());

            if (movie.getPosterFullPath() != null) {
                Picasso.with(mBinding.getRoot().getContext())
                        .load(movie.getPosterFullPath())
                        .into(mBinding.moviePosterImageview);
            }
        }
    }
}