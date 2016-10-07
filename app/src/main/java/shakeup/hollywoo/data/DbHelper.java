package shakeup.hollywoo.data;

import android.content.Context;

import com.orm.SugarContext;

import java.util.ArrayList;

/**
 * Created by Jayson Dela Cruz on 10/4/2016.
 */

public class DbHelper {

    /**
     * Get or initalize the movie record if it doesnt exist. If it does, return the existing record.
     * @param movieId
     * @return corresponding movie record
     */
    public static MovieRecord getMovie(Long movieId){
        ArrayList<MovieRecord> movieRecord = (ArrayList<MovieRecord>) MovieRecord.find(
                MovieRecord.class,
                "MOVIE_ID = ?", Long.toString(movieId));

        //List<MovieRecord> allRecords = MovieRecord.listAll(MovieRecord.class);
        //Log.i("DbHelper", "Num of movie records: " + allRecords.size());

        if (movieRecord.isEmpty()){
            // Create movie and add it to database
            MovieRecord myMovie = new MovieRecord(movieId, false, false);
            myMovie.save();
            return myMovie;
        } else {
            // Return the movie
            return movieRecord.get(0);
        }
    }

    /**
     * List all the movies in the database
     * @return List of all movies in database
     */
    public static ArrayList<MovieRecord> getAllMovies(){
        ArrayList<MovieRecord> allMovies =
                (ArrayList<MovieRecord>) MovieRecord.listAll(MovieRecord.class);
        return allMovies;
    }

    /**
     * List all the favorite movies in the database
     * @return List of favorite movies in database
     */
    public static ArrayList<MovieRecord> getFavorites(){
        ArrayList<MovieRecord> favoriteMovies = (ArrayList<MovieRecord>) MovieRecord.find(
                MovieRecord.class,
                "FAVORITE = true");
        return favoriteMovies;
    }

    /**
     * List all watch list movies in the database
     * @return List of watch list movies in the database
     */
    public static ArrayList<MovieRecord> getWatchList(){
        ArrayList<MovieRecord> watchMovies = (ArrayList<MovieRecord>) MovieRecord.find(
                MovieRecord.class,
                "watched = ?", "true");
        return watchMovies;
    }

    public static void deleteAll(){
        MovieRecord.deleteAll(MovieRecord.class);
    }

    public static void eraseDatabase(Context context){
        SugarContext.terminate();
        SugarContext.init(context);
    }

}
