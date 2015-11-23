package com.example.mhmdh.popmovies.Tmdb;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.mhmdh.popmovies.MainActivityFragment;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * The following classes are meant to be used with TMDb (the Movie Database)
 * It uses GSON to populate the data
 * The getters correspond to the movies JSON data
 * Familiarity with TMDb's API is expected.
 * https://www.themoviedb.org/documentation/api
 */
public class Movies {

    public ArrayList<Movie> results;

    public static class Movie implements Parcelable {
        @SerializedName("id")
        private int mId;
        @SerializedName("backdrop_path")
        private String mBackdropPath;
        @SerializedName("poster_path")
        private String mPosterPath;
        @SerializedName("original_title")
        private String mOriginalTitle;
        @SerializedName("original_language")
        private String mOriginalLanguage;
        @SerializedName("overview")
        private String mOverview;
        @SerializedName("release_date")
        private String mReleaseDate;
        @SerializedName("vote_average")
        private double mVoteAverage;
        @SerializedName("vote_count")
        private int mVoteCount;
        @SerializedName("popularity")
        private double mPopularity;

        private ArrayList<Trailers.Trailer> mTrailers;
        private ArrayList<Reviews.Review> mReviews;

        private final String IMAGE_URL = "http://image.tmdb.org/t/p/";
        private final String[] AVAILABLE_SIZES = {
                "w92",
                "w154",
                "w185",
                "w342",
                "w500",
                "w780",
                "original"
        };

        public static final String SIZE_W92 = "w92";
        public static final String SIZE_W154 = "w154";
        public static final String SIZE_W185 = "w185";
        public static final String SIZE_W342 = "w342";
        public static final String SIZE_W500 = "w500";
        public static final String SIZE_W780 = "w780";
        public static final String SIZE_ORIGINAL = "original";

        public void setTrailers(ArrayList<Trailers.Trailer> trailers) {
            mTrailers = trailers;
        }

        public void setReviews(ArrayList<Reviews.Review> reviews) {
            mReviews = reviews;
        }

        /**
         * @return the movies ID
         */
        public int getId() {
            return mId;
        }

        /**
         * @param size one of the specified size's above e.g. SIZE_W185
         * @param path backdropPath or posterPath
         * @return complete url where the desired image can be found
         */
        private String getImageUrl(String size, String path) {
            return IMAGE_URL + size + "/" + path;
        }

        /**
         * @return complete url where the movies backdrop image can be found with the original size
         */
        public String getBackdropPath() {
            return getBackdropPath(SIZE_ORIGINAL);
        }

        /**
         * @param size one of the specified sizes at the top of the file e.g. SIZE_W185
         * @return complete url where the movies backdrop image can be be found
         */
        public String getBackdropPath(String size) {
            return getImageUrl(size, mBackdropPath);
        }

        /**
         * @return complete url where the movies poster image can be found with the original size
         */
        public String getPosterPath() {
            return getPosterPath(SIZE_ORIGINAL);
        }

        /**
         * @param size one of the specified  at the top of the file above e.g. SIZE_W185
         * @return complete url where the movies poster image can be found
         */
        public String getPosterPath(String size) {
            return getImageUrl(size, mPosterPath);
        }

        /**
         * @return original title of the movie
         */
        public String getOriginalTitle() {
            return mOriginalTitle;
        }

        /**
         * @return original language of the movie
         */
        public String getOriginalLanguage() {
            return mOriginalLanguage;
        }

        /**
         * @return overview of the movie
         */
        public String getOverview() {
            return mOverview;
        }

        /**
         * @return release date of the movie
         */
        public String getReleaseDate() {
            return mReleaseDate;
        }

        /**
         * @return vote average of the movie
         */
        public double getVoteAverage() {
            return mVoteAverage;
        }

        /**
         * @return vote count of the movie
         */
        public int getVoteCount() {
            return mVoteCount;
        }

        /**
         * @return popularity of the movie
         */
        public double getPopularity() {
            return mPopularity;
        }

        public ArrayList<Trailers.Trailer> getTrailers() {
            return mTrailers;
        }

        public ArrayList<Reviews.Review> getReviews() {
            return mReviews;
        }

        public Movie(Cursor cursor) {
            mId = cursor.getInt(MainActivityFragment.COL_MOVIE_ID);
            mBackdropPath = cursor.getString(MainActivityFragment.COL_BACKDROP_PATH);
            mPosterPath = cursor.getString(MainActivityFragment.COL_POSTER_PATH);
            mOriginalTitle = cursor.getString(MainActivityFragment.COL_ORIGINAL_TITLE);
            mOriginalLanguage = cursor.getString(MainActivityFragment.COL_ORIGINAL_LANGUAGE);
            mOverview = cursor.getString(MainActivityFragment.COL_OVERVIEW);
            mReleaseDate = cursor.getString(MainActivityFragment.COL_RELEASE_DATE);
            mVoteAverage = cursor.getDouble(MainActivityFragment.COL_VOTE_AVERAGE);
            mVoteCount = cursor.getInt(MainActivityFragment.COL_VOTE_COUNT);
            mPopularity = cursor.getDouble(MainActivityFragment.COL_POPULARITY);
        }

        private Movie(Parcel in) {
            mId = in.readInt();
            mBackdropPath = in.readString();
            mPosterPath = in.readString();
            mOriginalTitle = in.readString();
            mOriginalLanguage = in.readString();
            mOverview = in.readString();
            mReleaseDate = in.readString();
            mVoteAverage = in.readDouble();
            mVoteCount = in.readInt();
            mPopularity = in.readDouble();
            mTrailers = in.readArrayList(Trailers.Trailer.class.getClassLoader());
            mReviews = in.readArrayList(Reviews.Review.class.getClassLoader());
        }

        public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
            @Override
            public Movie createFromParcel(Parcel in) {
                return new Movie(in);
            }

            @Override
            public Movie[] newArray(int size) {
                return new Movie[size];
            }
        };

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(mId);
            out.writeString(getBackdropPath(SIZE_W780));
            out.writeString(getPosterPath(SIZE_W185));
            out.writeString(mOriginalTitle);
            out.writeString(mOriginalLanguage);
            out.writeString(mOverview);
            out.writeString(mReleaseDate);
            out.writeDouble(mVoteAverage);
            out.writeInt(mVoteCount);
            out.writeDouble(mPopularity);
            out.writeList(mTrailers);
            out.writeList(mReviews);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}