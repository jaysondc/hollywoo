package shakeup.hollywoo;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import shakeup.hollywoo.data.DbHelper;
import shakeup.hollywoo.data.MovieRecord;

/**
 * Created by Jayson Dela Cruz 8/30/2016
 *
 */

public class MainActivity extends AppCompatActivity {

    public final String POPULARITY = "popularity";
    public final String RATING = "rating";
    public final String FAVORITES = "favorites";
    public final String LOG_TAG = getClass().getSimpleName();
    public final String BUNDLE_MOVIE_ADAPTER = "movie_adapter";
    public final String BUNDLE_SCROLL_POSITION = "scroll_position";
    public final String BUNDLE_SORT_BY = "sort_by";
    private String SORT_BY = POPULARITY;
    private JSONArray mResults;
    private GridView mGridView;
    private RequestQueue mRequestQueue;
    private CoordinatorLayout mCoordinatorLayout;
    private int mScrollPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCoordinatorLayout  = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        setSupportActionBar(toolbar);
        com.github.clans.fab.FloatingActionButton fabFilterFavorites =
                (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_filter_favorites);
        com.github.clans.fab.FloatingActionButton fabFilterPopular =
                (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_filter_popular);
        com.github.clans.fab.FloatingActionButton fabFilterRating =
                (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_filter_rating);

        // Instantiate the RequestQueue from the singleton VolleyRequestManager
        mRequestQueue = VolleyRequestManager.getInstance(
                this.getApplicationContext()).getRequestQueue();

        // Set up gridview
        mGridView = (GridView) findViewById(R.id.movie_gridview);

        // Get MovieAdapter from savedInstanceState if it exists
        if(savedInstanceState == null || !savedInstanceState.containsKey(BUNDLE_MOVIE_ADAPTER)){
            mGridView.setAdapter(new MovieAdapter());
            updateMovies();
        } else {
            SORT_BY = savedInstanceState.getString(BUNDLE_SORT_BY);
            mScrollPosition = savedInstanceState.getInt(BUNDLE_SCROLL_POSITION);

            // Restore adapter from saved state
            /**
             * Android isn't reliable about actually calling the parcelable implementation methods
             * and therefore copies the entire adapter object along with outdated references to
             * things like mGridview, causing all sorts of problems.
             * http://stackoverflow.com/questions/21301989/implementing-parcelable-in-android-onrestoreinstancestate-is-not-getting-called
             *
             * I implemented getters and setters to get the necessary data to re-create the adapter
             * myself.
             */
            MovieAdapter newAdapter = new MovieAdapter();
            MovieAdapter recoveredAdapter =
                    savedInstanceState.getParcelable(BUNDLE_MOVIE_ADAPTER);

            newAdapter.setResultsArray(recoveredAdapter.getResultsArray());
            newAdapter.setMovieRecordArray(recoveredAdapter.getMovieRecordArray());

            mGridView.setAdapter(newAdapter);
            mGridView.smoothScrollToPosition(mScrollPosition);
        }

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                // Create detail activity intent
                Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);

                Long movieID = (long) v.getTag();
                intent.putExtra(getString(R.string.EXTRA_MOVIE_ID), movieID);
                startActivity(intent);
            }
        });

        // Enable nested scrolling for use in CoordinatorLayout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setNestedScrollingEnabled(mGridView, true);
        }

        fabFilterFavorites.setOnClickListener(new filterClickListener());
        fabFilterPopular.setOnClickListener(new filterClickListener());
        fabFilterRating.setOnClickListener(new filterClickListener());

    }

    public boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    /**
     * Main method for updating the grid adapter. This shoots off the proper volley request
     * based on the filter type specified in the method variable SORT_BY
     */
    private void updateMovies(){

        Log.d(LOG_TAG, "Connection test = " + isNetworkAvailable());

        final com.github.clans.fab.FloatingActionMenu fabMenu =
                (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fab_filter_menu);

        if(!isNetworkAvailable()){
            // Show error if no connection is detected
            Snackbar.make(mCoordinatorLayout,
                    getResources().getString(R.string.NO_CONNECTION),
                    Snackbar.LENGTH_LONG)
            .show();
        } else {
            // Create URL String based on sort criteria
            String url = "";
            switch(SORT_BY){
                case POPULARITY:
                    url = getString(R.string.URL_POPULARITY) +
                                getString(R.string.API_KEY) +
                                BuildConfig.MOVIE_DB_API_KEY;
                    Snackbar.make(mCoordinatorLayout,
                                getResources().getString(R.string.SORT_POPULARITY_SNACKBAR),
                                Snackbar.LENGTH_LONG)
                                .show();
                break;
                case RATING:
                    url = getString(R.string.URL_RATING) +
                                getString(R.string.API_KEY) +
                                BuildConfig.MOVIE_DB_API_KEY;
                    Snackbar.make(mCoordinatorLayout,
                                getResources().getString(R.string.SORT_RATING_SNACKBAR),
                                Snackbar.LENGTH_LONG)
                                .show();
                break;
                case FAVORITES:
                    Snackbar.make(mCoordinatorLayout,
                            getResources().getString(R.string.SORT_FAVORITES_SNACKBAR),
                            Snackbar.LENGTH_LONG)
                            .show();
                    break;
            }
            Log.d(LOG_TAG, "URL: " + url);

            if(SORT_BY.equals(FAVORITES)){
                // For favorites, load the data locally
                ArrayList<MovieRecord> favoriteMovies = DbHelper.getFavorites();
                MovieAdapter adapter = (MovieAdapter) mGridView.getAdapter();
                adapter.setMovieRecordArray(favoriteMovies);
                adapter.notifyDataSetChanged();
                mGridView.setAdapter(adapter);
            } else {
                // For Rating and Popular, create the volley request

                // Build JSONArray Request for movies
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(LOG_TAG, "Response received.");
                                try {
                                    // Store results
                                    mResults = response.getJSONArray("results");
                                    // Update adapter with results
                                    MovieAdapter adapter = (MovieAdapter) mGridView.getAdapter();
                                    adapter.setResultsArray(mResults);
                                    mGridView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                } catch (JSONException error) {
                                    Log.d(LOG_TAG, "JSON Error: " + error);
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "Response error.");
                        // error handling stuff
                        Snackbar.make(mCoordinatorLayout,
                                getResources().getString(R.string.NETWORK_ERROR),
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
                });
                // Launch request
                mRequestQueue.add(jsonObjectRequest);
            }
        }
    }

    /**
     * Save list state from http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Store the movie adapter as parcelable
        MovieAdapter retainAdapter = (MovieAdapter) mGridView.getAdapter();
        outState.putParcelable(BUNDLE_MOVIE_ADAPTER, retainAdapter);

        // Store scroll position and sort method
        outState.putInt(BUNDLE_SCROLL_POSITION, mGridView.getFirstVisiblePosition());
        outState.putString(BUNDLE_SORT_BY, SORT_BY);

        super.onSaveInstanceState(outState);
    }


    public class MovieAdapter extends BaseAdapter implements Parcelable {

        private JSONArray mResultsArray;
        private ArrayList<MovieRecord> mMovieRecordArray;

        public MovieAdapter() {
            mResultsArray = mResults;
        }

        private MovieAdapter(Parcel in){
            try{
                mResultsArray = new JSONArray(in.readString());
            } catch (JSONException exception) {
                Log.e(LOG_TAG, "JSON Exception: " + exception);
                Log.d(LOG_TAG, "Failed to import JSON Parcel");
            }
        }

        public void setResultsArray(JSONArray results){
            mResultsArray = results;
        }

        public void setMovieRecordArray(ArrayList<MovieRecord> movieRecords){
            mMovieRecordArray = movieRecords;
        }

        public JSONArray getResultsArray(){
            return this.mResultsArray;
        }

        public ArrayList<MovieRecord> getMovieRecordArray(){
            return this.mMovieRecordArray;
        }

        public int getCount() {
            if (SORT_BY.equals(FAVORITES)) {
                return mMovieRecordArray != null ? mMovieRecordArray.size() : 0;
            } else {
                return mResultsArray != null ? mResultsArray.length() : 0;
            }
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            long movieId = -1;
            if(mResultsArray != null) {
                try {
                    JSONObject movie = (JSONObject) mResultsArray.get(position);
                    movieId = Long.parseLong(movie.getString("id"));
                    // Log.d(LOG_TAG, "Title: " + title);
                } catch (JSONException error) {
                    Log.d(LOG_TAG, "JSON Error: " + error);
                }
            }
            return movieId;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            View movieLayout = convertView;
            ImageView imageView;
            ImageView watchedView;
            ImageView favoriteView;
            final MovieRecord movieRecord;
            long movieId = -1;
            String posterUrl = "";

            if(SORT_BY.equals(FAVORITES)){
                // Retrieve the data from the favorite movie array
                movieRecord = mMovieRecordArray.get(position);
                posterUrl = movieRecord.imageUrl;
                movieId = movieRecord.movieId;

            } else {
                // Parse movie data from the JSONArray
                if(mResultsArray != null) {
                    try {
                        JSONObject movie = (JSONObject) mResultsArray.get(position);
                        String title = movie.getString("title");
                        String posterPath = movie.getString("poster_path");
                        String popularity = movie.getString("popularity");
                        String voteAverage = movie.getString("vote_average");
                        movieId = Long.parseLong(movie.getString("id"));

                        posterUrl = getString(R.string.BASE_IMG_URL) + getString(R.string.IMG_SIZE_342) + posterPath;

                        // Log.d(LOG_TAG, "Title: " + title);
                    } catch (JSONException error) {
                        Log.d(LOG_TAG, "JSON Error: " + error);
                    }
                }
                // Retrieve movie from local db and update image
                movieRecord = DbHelper.getMovie(movieId);
                movieRecord.imageUrl = posterUrl;
                movieRecord.save();
            }

            // Get recycled item
            if (convertView == null) {
                // if it's not recycled, assign new image and text
                movieLayout = getLayoutInflater().inflate(R.layout.movie_grid_item, parent, false);
            }

            // Put mMovieID in tag for use in details screen
            movieLayout.setTag(movieId);

            // Set image
            imageView = (ImageView) movieLayout.findViewById(R.id.movie_poster);
            Glide.with(getApplicationContext()).load(posterUrl).into(imageView);

            // Set watched
            watchedView = (ImageView) movieLayout.findViewById(R.id.grid_watched);
            if(movieRecord.watched){
                watchedView.setAlpha(Float.valueOf("1"));
            } else {
                watchedView.setAlpha(Float.valueOf("0.25"));
            }
            watchedView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (movieRecord.watched){
                        // true -> false
                        movieRecord.watched = false;
                        view.setAlpha(Float.valueOf("0.25"));
                    } else {
                        // false -> true
                        movieRecord.watched = true;
                        view.setAlpha(Float.valueOf("1"));
                    }
                    movieRecord.save();
                }
            });

            // Set favorite
            favoriteView = (ImageView) movieLayout.findViewById(R.id.grid_favorite);
            if(movieRecord.favorite){
                favoriteView.setAlpha(Float.valueOf("1"));
            } else {
                favoriteView.setAlpha(Float.valueOf("0.25"));
            }
            favoriteView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (movieRecord.favorite){
                        // true -> false
                        movieRecord.favorite = false;
                        view.setAlpha(Float.valueOf("0.25"));
                    } else {
                        // false -> true
                        movieRecord.favorite = true;
                        view.setAlpha(Float.valueOf("1"));
                    }
                    movieRecord.save();
                }
            });

            // Resize the layout to fit a poster
            ViewGroup.LayoutParams params = movieLayout.getLayoutParams();
            int movieWidth = mGridView.getColumnWidth();
            int movieHeight = (int) Math.floor(movieWidth * 1.5);
            params.height = (movieHeight);
            movieLayout.setLayoutParams(params);

            return movieLayout;
        }

        // Implement Parcelable methods
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            Log.d(LOG_TAG, "MovieAdapter written to parcel!");
            parcel.writeString(mResultsArray.toString());
        }

        public final Parcelable.Creator<MovieAdapter> CREATOR =
                new Parcelable.Creator<MovieAdapter>(){
            @Override
            public MovieAdapter createFromParcel(Parcel parcel) {
                Log.d(LOG_TAG, "MovieAdapter created from parcel!");
                return new MovieAdapter(parcel);
            }
            @Override
            public MovieAdapter[] newArray(int i) {
                Log.d(LOG_TAG, "MovieAdapter array created from parcel!");
                return new MovieAdapter[i];
            }

        };

    }


    private class filterClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            com.github.clans.fab.FloatingActionMenu fabMenu =
                    (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fab_filter_menu);
            fabMenu.close(true);
            switch(view.getId()){
                case R.id.fab_filter_favorites:
                    SORT_BY = FAVORITES;
                    updateMovies();
                    break;
                case R.id.fab_filter_popular:
                    SORT_BY = POPULARITY;
                    updateMovies();
                    break;
                case R.id.fab_filter_rating:
                    SORT_BY = RATING;
                    updateMovies();
                    break;
            }

            Log.d(LOG_TAG, "Now sorted by " + SORT_BY);

        }
    }
}
