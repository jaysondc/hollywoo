package shakeup.hollywoo.data;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Jayson Dela Cruz on 10/3/2016.
 */

public class MovieRecord extends SugarRecord {
    @Unique
    public long movieId;
    public boolean favorite;
    public boolean watched;
    public String imageUrl;

    public MovieRecord(){
    }

    public MovieRecord(Long movieId, boolean favorite, boolean watched, String imageUrl){
        this.movieId = movieId;
        this.favorite = favorite;
        this.watched = watched;
        this.imageUrl = imageUrl;
    }

    public MovieRecord(Long movieId, boolean favorite, boolean watched){
        this.movieId = movieId;
        this.favorite = favorite;
        this.watched = watched;
        this.imageUrl = "http://quartersnacks.com/wp-content/plugins/wd-instagram-feed/images/missing.png;";
    }
}
