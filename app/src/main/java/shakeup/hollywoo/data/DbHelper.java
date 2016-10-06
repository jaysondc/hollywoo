package shakeup.hollywoo.data;

import android.content.Context;

import com.orm.SugarContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jayson Dela Cruz on 10/4/2016.
 */

public class DbHelper {

    /**
     * This method is called whenever we encounter a movie. If the movie is already in our
     * database it returns that row. If it doesn't exist then the record is created
     * and returned.
     * @param movieId The MovieDB id of the movie.
     * @return A single movie entity from the database.
     */
    public static MovieRecord getMovie(Long movieId, String imgUrl){

        ArrayList<MovieRecord> movieRecord = (ArrayList<MovieRecord>) MovieRecord.find(
                MovieRecord.class,
                "MOVIE_ID = ?", Long.toString(movieId));

        List<MovieRecord> allRecords = MovieRecord.listAll(MovieRecord.class);

        //Log.i("DbHelper", "Num of movie records: " + allRecords.size());

        if (movieRecord.isEmpty()){
            // Create movie and add it to database
            MovieRecord myMovie = new MovieRecord(movieId, false, false, imgUrl);
            myMovie.save();
            return myMovie;
        } else {
            // Return the movie
            return movieRecord.get(0);
        }
    }

    public static MovieRecord getMovie(Long movieId){
        ArrayList<MovieRecord> movieRecord = (ArrayList<MovieRecord>) MovieRecord.find(
                MovieRecord.class,
                "MOVIE_ID = ?", Long.toString(movieId));

        List<MovieRecord> allRecords = MovieRecord.listAll(MovieRecord.class);

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

    public static void deleteAll(){
        MovieRecord.deleteAll(MovieRecord.class);
    }

    public static void eraseDatabase(Context context){
        SugarContext.terminate();
        SugarContext.init(context);
    }

}
