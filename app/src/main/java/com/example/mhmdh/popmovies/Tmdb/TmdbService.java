package com.example.mhmdh.popmovies.Tmdb;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * This interface class is designed to be used with the TmdbInterface interface,
 * and also to be used with Retrofit.
 * http://square.github.io/retrofit/
 */
public class TmdbService {
    private final static String BASE_URL = "http://api.themoviedb.org/3";
    private final static String API_KEY = new ApiKey().getKey();

    private static RestAdapter mRestAdapter;

    public static RestAdapter getRestAdapter() {
        if (mRestAdapter == null) {
            mRestAdapter = new RestAdapter.Builder()
                    .setEndpoint(BASE_URL)
                    .setRequestInterceptor(mRequestInterceptor)
                    .build();
        }
        return mRestAdapter;
    }

    private static RequestInterceptor mRequestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addQueryParam("api_key", API_KEY);
        }
    };
}