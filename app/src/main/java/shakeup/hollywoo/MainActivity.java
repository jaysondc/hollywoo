package shakeup.hollywoo;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
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

/**
 * Created by Jayson Dela Cruz 8/30/2016
 *
 */

public class MainActivity extends AppCompatActivity {

    public final String POPULARITY = "popularity";
    public final String RATING = "rating";
    public final String LOG_TAG = getClass().getSimpleName();
    public final String BUNDLE_MOVIE_ADAPTER = "movie_adapter";
    private String SORT_BY = POPULARITY;
    private JSONArray mResults;
    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // Set up gridview
        mGridView = (GridView) findViewById(R.id.movie_gridview);

        if(savedInstanceState == null || !savedInstanceState.containsKey(BUNDLE_MOVIE_ADAPTER)){
            mGridView.setAdapter(new MovieAdapter());
        } else {
            // Restore adapter from saved state
            mGridView.setAdapter((MovieAdapter)
                    savedInstanceState.getParcelable(BUNDLE_MOVIE_ADAPTER));
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

        // Instantiate the RequestQueue from the singleton VolleyRequestManager
        final RequestQueue queue = VolleyRequestManager.getInstance(
                this.getApplicationContext()).getRequestQueue();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toggle sort on FAB click
                if(SORT_BY.equals(POPULARITY)){
                    SORT_BY = RATING;
                    updateMovies(queue);
                } else if(SORT_BY.equals(RATING)){
                    SORT_BY = POPULARITY;
                    updateMovies(queue);
                }
            }
        });

        if(savedInstanceState == null || !savedInstanceState.containsKey(BUNDLE_MOVIE_ADAPTER)){
            updateMovies(queue);
        }
    }

    public boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void updateMovies(RequestQueue queue){
        Log.d(LOG_TAG, "Connection test = " + isNetworkAvailable());
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if(!isNetworkAvailable()){
            Snackbar.make(fab,
                    getResources().getString(R.string.NO_CONNECTION),
                    Snackbar.LENGTH_LONG)
            .show();
        } else {
            // Create URL String
            String url = "";
            if (SORT_BY.equals(POPULARITY)) {
                url = getString(R.string.URL_POPULARITY) +
                        getString(R.string.API_KEY) +
                        BuildConfig.MOVIE_DB_API_KEY;
                Snackbar.make(fab,
                        getResources().getString(R.string.SORT_POPULARITY_SNACKBAR),
                        Snackbar.LENGTH_LONG)
                .show();
            } else if (SORT_BY.equals(RATING)) {
                url = getString(R.string.URL_RATING) +
                        getString(R.string.API_KEY) +
                        BuildConfig.MOVIE_DB_API_KEY;
                Snackbar.make(fab,
                        getResources().getString(R.string.SORT_RATING_SNACKBAR),
                        Snackbar.LENGTH_LONG)
                .show();
            }
            Log.d(LOG_TAG, "URL: " + url);

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
                }
            });
            // Launch request
            queue.add(jsonObjectRequest);
        }
    }

    /**
     * Save list state from http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Store the movie adapter as parcelable
        outState.putParcelable(BUNDLE_MOVIE_ADAPTER, (MovieAdapter) mGridView.getAdapter());
        super.onSaveInstanceState(outState);
    }


    public class MovieAdapter extends BaseAdapter implements Parcelable {
        private String mPosterUrl;
        private long mMovieID;
        private final LayoutInflater mInflater;
        private JSONArray mResultsArray;

        public MovieAdapter() {
            mResultsArray = mResults;
            mInflater = LayoutInflater.from(getApplicationContext());
        }

        private MovieAdapter(Parcel in){
            try{
                mResultsArray = new JSONArray(in.readString());
            } catch (JSONException exception) {
                Log.e(LOG_TAG, "JSON Exception: " + exception);
                Log.d(LOG_TAG, "Failed to import JSON Parcel");
            }
            mInflater = LayoutInflater.from(getApplicationContext());
        }

        public void setResultsArray(JSONArray results){
            mResultsArray = results;
        }

        public int getCount() {
            return mResultsArray != null ? mResultsArray.length() : 0;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return mMovieID != 0 ? mMovieID : 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            View movieLayout = convertView;
            ImageView imageView;

            if(mResultsArray != null) {
                // Parse movie data
                try {
                    JSONObject movie = (JSONObject) mResultsArray.get(position);
                    String title = movie.getString("title");
                    String posterPath = movie.getString("poster_path");
                    String popularity = movie.getString("popularity");
                    String voteAverage = movie.getString("vote_average");
                    mMovieID = Long.parseLong(movie.getString("id"));

                    mPosterUrl = getString(R.string.BASE_IMG_URL) + getString(R.string.IMG_SIZE_342) + posterPath;

                    // Log.d(LOG_TAG, "Title: " + title);
                } catch (JSONException error) {
                    Log.d(LOG_TAG, "JSON Error: " + error);
                }
            }

            // Get recycled item
            if (convertView == null) {
                // if it's not recycled, assign new image and text
                movieLayout = mInflater.inflate(R.layout.movie_grid_item, parent, false);
            }
            // Put mMovieID in tag for use in details screen
            movieLayout.setTag(mMovieID);

            imageView = (ImageView) movieLayout.findViewById(R.id.movie_poster);
            Glide.with(getApplicationContext()).load(mPosterUrl).into(imageView);

            return movieLayout;
        }

        // Implement Parcelable methods
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(mResults.toString());
        }

        public final Parcelable.Creator<MovieAdapter> CREATOR =
                new Parcelable.Creator<MovieAdapter>(){
            @Override
            public MovieAdapter createFromParcel(Parcel parcel) {
                return new MovieAdapter(parcel);
            }
            @Override
            public MovieAdapter[] newArray(int i) {
                return new MovieAdapter[i];
            }

        };

    }
}
