package shakeup.hollywoo;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieDetailActivity extends AppCompatActivity {
    private Long movieID;
    private JSONObject mMovie;

    ImageView mHeaderImage;
    TextView mTitle;
    TextView mGenres;
    TextView mRating;
    TextView mReleaseDate;
    TextView mSynopsis;

    private final String LOG_TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get Movie ID passed in from intent
        movieID = getIntent().getLongExtra(getString(R.string.EXTRA_MOVIE_ID), 0);

        // Instantiate the RequestQueue from the singleton VolleyRequestManager
        final RequestQueue queue = VolleyRequestManager.getInstance(this.getApplicationContext()).getRequestQueue();


        // Build movie request URL
        String url = getString(R.string.URL_MOVIE) + movieID + "?" +
                getString(R.string.API_KEY) + BuildConfig.MOVIE_DB_API_KEY;

        Log.d(LOG_TAG, "URL: " + url);

        // Build JSONArray Request for movies
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(LOG_TAG, "Response received.");
                        // Store results
                        mMovie = response;
                        // Update adapter with results
                        updateUI();
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Log.d(LOG_TAG, "Response error.");
                // error handling stuff
                Snackbar.make(findViewById(R.id.app_bar),
                        getResources().getString(R.string.NETWORK_ERROR),
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        });
        // Launch request
        queue.add(jsonObjectRequest);


    }

    private void updateUI(){
        // Update UI once volley request is complete.

        mHeaderImage = (ImageView) findViewById(R.id.detail_app_bar_image);
        mTitle = (TextView) findViewById(R.id.detail_title);
        mGenres = (TextView) findViewById(R.id.detail_genres);
        mRating = (TextView) findViewById(R.id.detail_user_rating);
        mReleaseDate = (TextView) findViewById(R.id.detail_release_runtime);
        mSynopsis = (TextView) findViewById(R.id.detail_synopsis);

        // Update background image and details
        String rating = null;
        String releaseRuntime = null;
        String synopsis = null;
        String title = null;
        String genres = "";
        String posterUrl = null;

        if(mMovie != null) {
            // Parse movie data
            try {
                title = mMovie.getString("title");

                rating = mMovie.getString("vote_average") + "/10";
                rating = rating + " - " + mMovie.getString("vote_count") + " votes";

                JSONArray jGenres = mMovie.getJSONArray("genres");
                for(int i = 0; i < jGenres.length(); i++){
                    JSONObject oGenres = jGenres.getJSONObject(i);
                    genres = genres + oGenres.getString("name");
                    if(i != jGenres.length()-1){
                        genres = genres + ", ";
                    }
                }
                //genres = genres.substring(0, genres.length()-3);

                releaseRuntime = mMovie.getString("release_date");
                releaseRuntime = releaseRuntime + " - " + mMovie.getString("runtime") + " min";

                synopsis = mMovie.getString("overview");

                posterUrl = getString(R.string.BASE_IMG_URL) +
                        getString(R.string.IMG_SIZE_780) +
                        mMovie.getString("poster_path");
            } catch (JSONException error) {
                Log.d(LOG_TAG, "JSON Error: " + error);
            }
        }

        // Update title
//        CollapsingToolbarLayout collapsingToolbarLayout =
//                (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
//        collapsingToolbarLayout.setTitle(title);

        // Update header image
        Glide.with(this).load(posterUrl).into(mHeaderImage);
        // Update text fields
        mTitle.setText(title);
        mRating.setText(rating);
        mGenres.setText(genres);
        mReleaseDate.setText(releaseRuntime);
        mSynopsis.setText(synopsis);
    }
}
