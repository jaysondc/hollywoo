package shakeup.hollywoo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;

public class MovieDetailActivity extends AppCompatActivity {
    private Long movieID;
    private JSONObject mMovie;
    private JSONObject mVideos;
    private JSONObject mReviews;

    ImageView mHeaderImage;
    TextView mTitle;
    TextView mGenres;
    TextView mRating;
    TextView mReleaseDate;
    TextView mSynopsis;
    LinearLayout mTrailerLayout;

    static final int TRAILER_HEIGHT = 128;
    static final int TRAILER_WIDTH = 228;

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
        String movieUrl = getString(R.string.URL_MOVIE) + movieID + "?" +
                getString(R.string.API_KEY) + BuildConfig.MOVIE_DB_API_KEY;
        // Build videos request URL
        String videoUrl = "http://api.themoviedb.org/3/movie/" + movieID + "/videos?" +
                "api_key=" + BuildConfig.MOVIE_DB_API_KEY;
        // Build reviews request URl
        String reviewUrl = "http://api.themoviedb.org/3/movie/" + movieID + "/reviews?" +
                "api_key=" + BuildConfig.MOVIE_DB_API_KEY;

        Log.d(LOG_TAG, "URL: " + movieUrl);

        // Build Object Request for the movie
        JsonObjectRequest movieRequest = new JsonObjectRequest(Request.Method.GET, movieUrl, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(LOG_TAG, "Response received.");
                        // Store results
                        mMovie = response;
                        // Update detail card with results
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

        // Build JSONObject request for videos
        JsonObjectRequest videoRequest = new JsonObjectRequest(Request.Method.GET, videoUrl, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(LOG_TAG, "Response received.");
                        // Store results
                        mVideos = response;
                        // Update video card with results
                        updateVideos();
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

        // Build JSONObject request for reviews
        JsonObjectRequest reviewRequest = new JsonObjectRequest(Request.Method.GET, reviewUrl, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(LOG_TAG, "Response received.");
                        // Store results
                        mReviews = response;
                        // Update review card with results
                        updateReviews();
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

        // Launch requests
        queue.add(movieRequest);
        queue.add(videoRequest);
        queue.add(reviewRequest);


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

        // Update header image
        Glide.with(this).load(posterUrl).into(mHeaderImage);
        // Update text fields
        mTitle.setText(title);
        mRating.setText(rating);
        mGenres.setText(genres);
        mReleaseDate.setText(releaseRuntime);
        mSynopsis.setText(synopsis);
    }

    private void updateVideos(){

        mTrailerLayout = (LinearLayout) findViewById(R.id.detail_trailer_layout);
        ArrayList<String> movieKeys = new ArrayList<String>();

        if(mVideos != null) {
            // Parse movie data
            try {
                JSONArray jVideos = mVideos.getJSONArray("results");
                for(int i = 0; i < jVideos.length(); i++){
                    JSONObject oVideo = jVideos.getJSONObject(i);
                    movieKeys.add(oVideo.getString("key"));
                }
            } catch (JSONException error) {
                Log.d(LOG_TAG, "JSON Error: " + error);
            }
        }
        for(String trailerKey : movieKeys){
            mTrailerLayout.addView(createYouTubeView(trailerKey,
                    TRAILER_WIDTH, TRAILER_HEIGHT));
        }
    }

    private void updateReviews(){

    }

    /**
     * This class takes a YouTube video ID and and returns
     * a clickable ImageView that takes you to the video inside YouTube.
     *
     * @param url The YouTube video ID
     * @param width Width of the ImageView in DP
     * @param height Height of the ImageView in DP
     * @return Returns an ImageView object
     */
    private FrameLayout createYouTubeView(String videoId, int width, int height){
        // Populate trailers
        final String imgUrl = "http://img.youtube.com/vi/" + videoId + "/mqdefault.jpg";
        final String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

        FrameLayout youTubeView = new FrameLayout(this);
        ImageView thumbnailView = new ImageView(this);
        ImageView playButtonView = new ImageView(this);

        // Convert width and height to DP
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthDp = (width * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
        int heightDp = (height * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;

        // Set up thumbnail
        FrameLayout.LayoutParams frameLayoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
        thumbnailView.setLayoutParams(frameLayoutParams);
        // Remove this dependency to make this more reusable
        Glide.with(this).load(imgUrl).into(thumbnailView);
        thumbnailView.setImageResource(R.drawable.trailer0);
        thumbnailView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Set up play button
        frameLayoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
        frameLayoutParams.gravity = Gravity.CENTER;
        playButtonView.setLayoutParams(frameLayoutParams);
        playButtonView.setImageResource(android.R.drawable.ic_media_play);

        // Set up Frame
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(widthDp, heightDp);
        youTubeView.setLayoutParams(layoutParams);
        youTubeView.addView(thumbnailView);
        youTubeView.addView(playButtonView);

        youTubeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW, Uri.parse(videoUrl)
                );
                startActivity(intent);
            }
        });

        return youTubeView;
    }
}
