package shakeup.hollywoo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
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

import shakeup.hollywoo.data.DbHelper;
import shakeup.hollywoo.data.MovieRecord;

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
    LinearLayout mMediaLayout;
    LinearLayout mReviewLayout;
    ImageView mFavoritesImage;
    ImageView mWatchImage;

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
        mFavoritesImage = (ImageView) findViewById(R.id.detail_favorites);
        mWatchImage = (ImageView) findViewById(R.id.detail_watched);

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

        // Get local movie record
        final MovieRecord movieRecord = DbHelper.getMovie(movieID);

        // Update header image
        Glide.with(this).load(posterUrl).into(mHeaderImage);
        // Update text fields
        mTitle.setText(title);
        mRating.setText(rating);
        mGenres.setText(genres);
        mReleaseDate.setText(releaseRuntime);
        mSynopsis.setText(synopsis);

        // Update Favorites
        if(movieRecord.favorite){
            mFavoritesImage.setImageResource(R.drawable.ic_favorite_black_36dp);
        } else {
            mFavoritesImage.setImageResource(R.drawable.ic_favorite_border_black_36dp);
        }
        mFavoritesImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ImageView myView = (ImageView) view;
                if (movieRecord.favorite){
                    // true -> false
                    movieRecord.favorite = false;
                    myView.setImageResource(R.drawable.ic_favorite_border_black_36dp);
                } else {
                    // false -> true
                    movieRecord.favorite = true;
                    myView.setImageResource(R.drawable.ic_favorite_black_36dp);
                }
                movieRecord.save();
                //TODO Notify gridview that data has changed
            }
        });


        // Update watch
        if(movieRecord.watched){
            mWatchImage.setImageResource(R.drawable.ic_visibility_black_36dp);
        } else {
            mWatchImage.setImageResource(R.drawable.ic_visibility_off_black_36dp);
        }
        mWatchImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ImageView myView = (ImageView) view;
                if (movieRecord.watched){
                    // true -> false
                    movieRecord.watched = false;
                    myView.setImageResource(R.drawable.ic_visibility_off_black_36dp);
                } else {
                    // false -> true
                    movieRecord.watched = true;
                    myView.setImageResource(R.drawable.ic_visibility_black_36dp);
                }
                movieRecord.save();
                //TODO Notify gridview that data has changed
            }
        });
    }

    private void updateVideos(){

        mMediaLayout = (LinearLayout) findViewById(R.id.detail_media_layout);
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
            mMediaLayout.addView(createYouTubeView(trailerKey));
        }
    }

    private void updateReviews(){
        mReviewLayout = (LinearLayout) findViewById(R.id.detail_review_layout);
        LayoutInflater inflater = getLayoutInflater();

        if(mReviews != null) {
            // Parse review data
            try {
                JSONArray jReviews = mReviews.getJSONArray("results");
                for(int i = 0; i < jReviews.length(); i++){
                    JSONObject oReview = jReviews.getJSONObject(i);

                    // Get new review view
                    LinearLayout reviewView = (LinearLayout) inflater.inflate(
                            R.layout.detail_item_review, null);

                    // Setup author view
                    final String author = oReview.getString("author");
                    TextView authorView = (TextView) reviewView.findViewById(R.id.review_author);
                    authorView.setText(author);

                    // Setup content view
                    String content = oReview.getString("content");
                    String paragraphs[] = content.split("\\r?\\n");
                    String firstParagraph = paragraphs[0];
                    TextView reviewContentView = (TextView) reviewView.findViewById(R.id.review_text);
                    reviewContentView.setText(firstParagraph);

                    final String url = oReview.getString("url");
                    reviewContentView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        }
                    });

                    mReviewLayout.addView(reviewView);

                    // Add divider
                    if(i < jReviews.length()-1){
                        inflater.inflate(R.layout.horizontal_divider, mReviewLayout, true);
                    }

                }
            } catch (JSONException error) {
                Log.d(LOG_TAG, "JSON Error: " + error);
            }
        }
    }

    /**
     * This class takes a YouTube video ID and and returns
     * a clickable ImageView that takes you to the video inside YouTube.
     *
     * @param url The YouTube video ID
     * @return Returns an ImageView object
     */
    private FrameLayout createYouTubeView(String videoId){
        // Populate trailers
        final String imgUrl = "http://img.youtube.com/vi/" + videoId + "/mqdefault.jpg";
        final String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

        LayoutInflater inflater = getLayoutInflater();
        FrameLayout youTubeView = (FrameLayout) inflater.inflate(R.layout.detail_item_video, null);

        ImageView thumbnailView = (ImageView) youTubeView.findViewById(R.id.media_thumbnail);

        Glide.with(this).load(imgUrl).into(thumbnailView);

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
