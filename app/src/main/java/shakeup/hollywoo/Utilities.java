package shakeup.hollywoo;

import android.util.DisplayMetrics;

/**
 * Created by Jayson Dela Cruz on 9/28/2016.
 */

public class Utilities {

    public static int getDPI(int size, DisplayMetrics metrics){
        return (size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }

}
