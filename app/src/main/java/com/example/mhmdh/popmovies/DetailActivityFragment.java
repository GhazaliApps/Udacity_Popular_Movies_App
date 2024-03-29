package com.example.mhmdh.popmovies;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mhmdh.popmovies.Data.FavoriteMovieContract;
import com.example.mhmdh.popmovies.Tmdb.Movies;
import com.example.mhmdh.popmovies.Tmdb.Reviews;
import com.example.mhmdh.popmovies.Tmdb.TmdbInterface;
import com.example.mhmdh.popmovies.Tmdb.TmdbService;
import com.example.mhmdh.popmovies.Tmdb.Trailers;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    @Bind(R.id.overview_title)
    TextView mOverviewTitle;
    @Bind(R.id.trailers_title)
    TextView mTrailersTitle;
    @Bind(R.id.reviews_title)
    TextView mReviewsTitle;

    @Bind(R.id.backdrop)
    ImageView mMovieBackdropPath;
    @Bind(R.id.poster)
    ImageView mMoviePosterPath;

    @Bind(R.id.original_title)
    TextView mMovieOriginalTitle;
    @Bind(R.id.overview)
    TextView mMovieOverview;

    @Bind(R.id.release_date)
    TextView mMovieReleaseDate;
    @Bind(R.id.rating)
    TextView mMovieRating;

    @Bind(R.id.favoriteBtn)
    android.support.design.widget.FloatingActionButton mFavoriteBtn;

    @Bind(R.id.trailers)
    LinearLayout mTrailersLinearLayout;
    @Bind(R.id.reviews)
    LinearLayout mReviewsLinearLayout;

    private boolean mIsFavorite;
    private boolean mGotData;

    private String mMovieId;

    private Movies.Movie mMovie;
    private MenuItem mShareMenuItem;
    private ShareActionProvider mShareActionProvider;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.getParcelableExtra(Utility.MOVIE_PARCEL) != null) {
            mMovie = intent.getParcelableExtra(Utility.MOVIE_PARCEL);
            mGotData = true;
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mMovie = arguments.getParcelable(Utility.MOVIE_PARCEL);
                mGotData = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if (mGotData && isAdded()) {
            updateDetailActivityWithData(rootView);
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_detail, menu);
        mShareMenuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenuItem);

        mShareMenuItem.setVisible(false);

        // The ifs are coming.. Just trying to avoid NullPointerExceptions when trying to share trailer.
        if (mMovie != null) {
            if (mMovie.getTrailers() != null) {
                if (!mMovie.getTrailers().isEmpty()) {
                    mShareMenuItem.setVisible(true);
                    mShareActionProvider.setShareIntent(createShareMovieIntent());
                }
            }
        }
    }

    /**
     * Creates a share intent that contains a text string with the YouTube URL of the first trailer
     *
     * @return shareIntent used to share the YouTube URL of the first trailer
     */
    @SuppressWarnings("deprecation")
    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        String shareMessage = "Check out the trailer for " + mMovie.getOriginalTitle() + "\n" +
                mMovie.getTrailers().get(0).createYoutubeLink();

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        return shareIntent;
    }

    /**
     * Update the DetailActivity UI with the movie information.
     *
     * @param rootView
     */
    private void updateDetailActivityWithData(View rootView) {
        if (mMovie != null) {
            ButterKnife.bind(this, rootView);
            final Activity activity = getActivity();

            TmdbService tmdbService = new TmdbService();
            RestAdapter retrofit = tmdbService.getRestAdapter();
            final TmdbInterface service = retrofit.create(TmdbInterface.class);

            // mOverviewTitle.setVisibility(View.VISIBLE);
            mTrailersTitle.setVisibility(View.VISIBLE);
            mReviewsTitle.setVisibility(View.VISIBLE);
            mFavoriteBtn.setVisibility(View.VISIBLE);

            mMovieId = "" + mMovie.getId();
            mIsFavorite = isFavorite();

            Picasso.with(activity)
                    .load(mMovie.getBackdropPath(Utility.STANDARD_BACKDROP_SIZE))
                    .into(mMovieBackdropPath);

            Picasso.with(activity)
                    .load(mMovie.getPosterPath(Utility.STANDARD_POSTER_SIZE))
                    .error(R.drawable.placeholder_error)
                    .into(mMoviePosterPath);

            mMoviePosterPath.setVisibility(View.VISIBLE);

            mMovieOriginalTitle.setText(mMovie.getOriginalTitle());
            mMovieOverview.setText(mMovie.getOverview());

            mMovieReleaseDate.setText(Utility.formatDate(mMovie.getReleaseDate()));
            mMovieReleaseDate.setVisibility(View.VISIBLE);

            mMovieRating.setText(String.valueOf(mMovie.getVoteAverage() + "/10"));
            mMovieRating.setVisibility(View.VISIBLE);

            fetchTrailers(service);
            fetchReviews(service);

            updateFavoriteButton();

            mFavoriteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isFavorite()) {
                        unsetFavorite();
                    } else {
                        setFavorite();
                    }
                }
            });
        }
    }

    /**
     * Changes the favorite button according to whether the movie is favored or not.
     */
    private void updateFavoriteButton() {
        int resId;
        if (mIsFavorite) {
            resId = R.drawable.ic_favorite_selected;
        } else {
            resId = R.drawable.ic_favorite;
        }

        mFavoriteBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), resId));
    }

    /**
     * Sets the desired movie to be favored,
     * and adds it to the database.
     *
     * @return the movieId of the movie favored
     */
    public long setFavorite() {
        long movieId;
        Cursor movieCursor = movieExistsInDb();

        if (movieCursor.moveToFirst()) {
            int movieIdIndex = movieCursor.getColumnIndex(FavoriteMovieContract.MovieEntry._ID);
            movieId = movieCursor.getLong(movieIdIndex);
        } else {
            ContentValues movieValues = new ContentValues();

            movieValues.put(FavoriteMovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovieId);
            movieValues.put(FavoriteMovieContract.MovieEntry.COLUMN_BACKDROP_PATH,
                    mMovie.getBackdropPath(Utility.STANDARD_BACKDROP_SIZE));

            movieValues.put(FavoriteMovieContract.MovieEntry.COLUMN_POSTER_PATH,
                    mMovie.getPosterPath(Utility.STANDARD_POSTER_SIZE));

            movieValues.put(FavoriteMovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
                    mMovie.getOriginalTitle());

            movieValues.put(FavoriteMovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
                    mMovie.getOriginalLanguage());

            movieValues.put(FavoriteMovieContract.MovieEntry.COLUMN_OVERVIEW,
                    mMovie.getOverview());
            movieValues.put(FavoriteMovieContract.MovieEntry.COLUMN_RELEASE_DATE,
                    mMovie.getReleaseDate());
            movieValues.put(FavoriteMovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                    mMovie.getVoteAverage());
            movieValues.put(FavoriteMovieContract.MovieEntry.COLUMN_VOTE_COUNT,
                    mMovie.getVoteCount());
            movieValues.put(FavoriteMovieContract.MovieEntry.COLUMN_POPULARITY,
                    mMovie.getPopularity());

            Uri insertedUri = getActivity().getContentResolver().insert(
                    FavoriteMovieContract.MovieEntry.CONTENT_URI,
                    movieValues
            );
            movieId = ContentUris.parseId(insertedUri);
        }
        movieCursor.close();
        mIsFavorite = true;
        updateFavoriteButton();
        return movieId;
    }

    /**
     * Removes a movie from the favorites database.
     */
    public void unsetFavorite() {
        Cursor movieCursor = movieExistsInDb();

        if (movieCursor.moveToFirst()) {
            getActivity().getContentResolver().delete(
                    FavoriteMovieContract.MovieEntry.CONTENT_URI,
                    FavoriteMovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{mMovieId}
            );
        }
        movieCursor.close();
        mIsFavorite = false;
        updateFavoriteButton();
    }

    /**
     * Checks whether the movie is favored or not,
     * returns true if it is, else false.
     *
     * @return boolean whether the movie is favored or not
     */
    private boolean isFavorite() {
        boolean isFavorite = false;

        Cursor movieCursor = movieExistsInDb();

        if (movieCursor.moveToFirst()) {
            isFavorite = true;
        }

        movieCursor.close();
        return isFavorite;
    }

    /**
     * Checks if the movie exists in the database (Only used for favored movies).
     *
     * @return movieCursor
     */
    private Cursor movieExistsInDb() {
        Cursor movieCursor = getActivity().getContentResolver().query(
                FavoriteMovieContract.MovieEntry.CONTENT_URI,
                new String[]{
                        FavoriteMovieContract.MovieEntry._ID,
                        FavoriteMovieContract.MovieEntry.COLUMN_MOVIE_ID
                },
                FavoriteMovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{mMovieId},
                null
        );

        return movieCursor;
    }

    /**
     * Fetches trailers from TMDb API using Retrofit.
     * http://square.github.io/retrofit/
     *
     * @param service TmdbInterface
     */
    private void fetchTrailers(TmdbInterface service) {
        service.getTrailerList(mMovie.getId(), new Callback<Trailers>() {
            @Override
            public void success(Trailers trailers, Response response) {
                mMovie.setTrailers(trailers.results);
                updateTrailers();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(LOG_TAG, "Retrofit error: ", error);
                updateTrailers(); // Used to print "No trailers available" when network is turned off
            }
        });
    }

    /**
     * This method is used to populate the 'Trailers' field in the details view.
     */
    private void updateTrailers() {
        if (mMovie.getTrailers() == null || mMovie.getTrailers().isEmpty()) {
//            addToLinearLayout(mTrailersLinearLayout, getString(
//                            R.string.no_trailers_available),
//                    Utility.STANDARD_SECOND_HEADLINE_TYPEFACE);
        } else {
            if (mShareActionProvider != null) {
                mShareMenuItem.setVisible(true);
                mShareActionProvider.setShareIntent(createShareMovieIntent());
            }

            // For every trailer set an on click listener and when clicked launch trailer on YouTube
            for (final Trailers.Trailer trailer : mMovie.getTrailers()) {
//                TextView textView = new TextView(getActivity());
//                textView.setText(trailer.getName());
//                textView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        startActivity(new Intent(
//                                        Intent.ACTION_VIEW,
//                                        Uri.parse(trailer.createYoutubeLink()))
//                        );
//                    }
//                });
//                mTrailersLinearLayout.addView(textView);
                ImageView imageView = new ImageView(getActivity());
                imageView.setImageResource(R.drawable.media_player);
                imageView.setContentDescription(trailer.getName());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(trailer.createYoutubeLink()))
                        );
                    }
                });
                mTrailersLinearLayout.addView(imageView);
            }
        }
    }

    /**
     * Fetches user reviews from TMDb API using Retrofit.
     * http://square.github.io/retrofit/
     *
     * @param service TmdbInterface
     */
    private void fetchReviews(TmdbInterface service) {
        service.getReviewList(mMovie.getId(), new Callback<Reviews>() {
            @Override
            public void success(Reviews reviews, Response response) {
                mMovie.setReviews(reviews.results);
                updateReviews();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(LOG_TAG, "Retrofit error: ", error);
                //updateReviews(); // Used to print "No reviews available" when network is turned off
            }
        });
    }

    /**
     * This method is used to populate the 'User Reviews' field in the details view.
     */
    private void updateReviews() {
        if (mMovie.getReviews() == null || mMovie.getReviews().isEmpty()) {
//            addToLinearLayout(mReviewsLinearLayout, getString(
//                    R.string.no_reviews_available), Utility.STANDARD_SECOND_HEADLINE_TYPEFACE);
        } else {
            Activity activity = getActivity();
            int numOfReviews = mMovie.getReviews().size();
            int i = 1;
            for (Reviews.Review review : mMovie.getReviews()) {
                addToLinearLayout(mReviewsLinearLayout, review.getAuthor(),
                        Utility.STANDARD_SECOND_HEADLINE_TYPEFACE);

                TextView contentTextView = new TextView(getActivity());
                contentTextView.setText(review.getContent());

                // Change the margins of the last user reviews content
                if (i++ != numOfReviews) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));
                    params.setMargins(
                            Utility.getIntFromResourceFile(activity,
                                    R.integer.last_review_content_left_margin),
                            Utility.getIntFromResourceFile(activity,
                                    R.integer.last_review_content_top_margin),
                            Utility.getIntFromResourceFile(activity,
                                    R.integer.last_review_content_right_margin),
                            Utility.getIntFromResourceFile(activity,
                                    R.integer.last_review_content_bottom_margin)
                    );
                    contentTextView.setLayoutParams(params);
                }

                mReviewsLinearLayout.addView(contentTextView);
            }
        }
    }

    /**
     * This method is specifically designed to be used with the updateTrailers & updateReviews methods.
     * <p/>
     * Takes 3 arguments.
     * The first is a LinearLayout, the LinearLayout you want to add a a TextView to.
     * The second is a text String, the text you want 'printed' in the LinearLayout.
     * The third is a typeface int, the type you want the text to be, e.g. Typeface.BOLD.
     *
     * @param linearLayout
     * @param text
     * @param typeface
     */
    private void addToLinearLayout(LinearLayout linearLayout, String text, int typeface) {
        TextView noInformationAvailable = new TextView(getActivity());
        noInformationAvailable.setText(text);
        noInformationAvailable.setTypeface(null, typeface);
        linearLayout.addView(noInformationAvailable);
    }
}