package com.example.mhmdh.popmovies.Tmdb;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * This interface is designed to be used with the TmdbService class,
 * and also to be used with Retrofit.
 * http://square.github.io/retrofit/
 */
public interface TmdbInterface {
    @GET("/discover/movie")
    void getMovieList(
            @Query("sort_by") String sortBy,
            @Query("vote_count.gte") String voteCount,
            Callback<Movies> callback
    );

    @GET("/movie/{id}/videos")
    void getTrailerList(
            @Path("id") int movieId,
            Callback<Trailers> callback
    );

    @GET("/movie/{id}/reviews")
    void getReviewList(
            @Path("id") int movieId,
            Callback<Reviews> callback
    );
}