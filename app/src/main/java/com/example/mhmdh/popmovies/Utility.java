package com.example.mhmdh.popmovies;

import android.app.Activity;
import android.graphics.Typeface;

import com.example.mhmdh.popmovies.Tmdb.Movies;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {
    public static final String MOVIE_PARCEL = "MOVIE_PARCEL";
    public static final String RESTORED_STATE = "RESTORED_STATE";

    public static final String STANDARD_BACKDROP_SIZE = Movies.Movie.SIZE_W780;
    public static final String STANDARD_POSTER_SIZE = Movies.Movie.SIZE_W185;

    public static final int STANDARD_SECOND_HEADLINE_TYPEFACE = Typeface.BOLD;

    public Utility() {
    }

    public static String formatDate(String dateToFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        Date date = null;
        try {
            date = formatter.parse(dateToFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        formatter = new SimpleDateFormat("yyyy");

        return formatter.format(date);
    }

    /**
     * This method is used with the res/values/integer file
     * <p/>
     * Takes two arguments, the first is the current activity of the file, e.g. getActivity()
     * The second is the fieldName of the desired field you want to get the integer value from.
     *
     * @param activity
     * @param fieldName the name of the field you want to get the integer value from
     * @return integer value from the corresponding field name in the integer resource file
     */
    public static int getIntFromResourceFile(Activity activity, int fieldName) {
        return activity.getResources().getInteger(fieldName);
    }
}
