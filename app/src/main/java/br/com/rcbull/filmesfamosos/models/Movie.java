package br.com.rcbull.filmesfamosos.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Movie
 * Created by rcb on 01/04/2018.
 */

public class Movie implements Parcelable {

    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); //format date to movie detail

    private static final DecimalFormat voteFormat = new DecimalFormat("#.##"); //format vote

    private int id;
    @SerializedName("vote_average")
    private float voteAverage;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("original_title")
    private String originalTitle;
    private String overview;
    @SerializedName("release_date")
    private Date releaseDate;

    private Movie(Parcel in) {
        setId(in.readInt());
        setVoteAverage(in.readFloat());
        setPosterPath(in.readString());
        setOriginalTitle(in.readString());
        setOverview(in.readString());
        setReleaseDate(in.readString());
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    private void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    private int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    private float getVoteAverage() {
        return voteAverage;
    }

    public String printVoteAverage() {
        return voteFormat.format(getVoteAverage());
    }

    private void setVoteAverage(float voteAverage) {
        this.voteAverage = voteAverage;
    }

    private String getPosterPath() {
        return posterPath;
    }

    private void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    private void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterFullPath() {
        return "http://image.tmdb.org/t/p/w185" + posterPath;
    } //path to image

    private Date getReleaseDate() {
        return releaseDate;
    }

    public String printReleaseDate() {
        if (getReleaseDate() != null)
            return dateFormat.format(getReleaseDate());
        else
            return "";
    }

    private void setReleaseDate(String releaseDate) {
        try {
            this.releaseDate = dateFormat.parse(releaseDate);
        } catch (ParseException e) {
            this.releaseDate = null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Movie)) return false;

        Movie other = (Movie) obj;
        return this.getId() == other.getId();
    }

    public boolean displayEquals(Object obj) {
        if (!(obj instanceof Movie)) return false;

        Movie other = (Movie) obj;
        return this.getPosterPath().equals(other.getPosterPath()) &&
                this.getOriginalTitle().equals(other.getOriginalTitle()) &&
                this.getReleaseDate().getTime() == other.getReleaseDate().getTime();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeFloat(getVoteAverage());
        dest.writeString(getPosterPath());
        dest.writeString(getOriginalTitle());
        dest.writeString(getOverview());
        dest.writeString(printReleaseDate());
    }

    static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
