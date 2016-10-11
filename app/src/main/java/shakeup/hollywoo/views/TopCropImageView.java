package shakeup.hollywoo.views;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * ImageView to display top-crop scale of an image view.
 * http://stackoverflow.com/questions/29783358/how-set-imageview-scaletype-to-topcrop
 *
 * @author Chris Arriola
 */
public class TopCropImageView extends ImageView {

    public TopCropImageView(Context context) {
        super(context);
        setScaleType(ScaleType.MATRIX);
    }

    public TopCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
    }

    public TopCropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        if(getDrawable() != null){
            final Matrix matrix = getImageMatrix();

            float scale;
            final int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            final int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            final int drawableWidth = getDrawable().getIntrinsicWidth();
            final int drawableHeight = getDrawable().getIntrinsicHeight();

            if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
                scale = (float) viewHeight / (float) drawableHeight;
            } else {
                scale = (float) viewWidth / (float) drawableWidth;
            }

            matrix.setScale(scale, scale);
            setImageMatrix(matrix);
        }

        return super.setFrame(l, t, r, b);
    }
}