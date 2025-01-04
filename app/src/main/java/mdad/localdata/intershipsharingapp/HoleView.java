package mdad.localdata.intershipsharingapp;
import static android.view.View.LAYER_TYPE_HARDWARE;

import static androidx.core.view.ViewCompat.setLayerType;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class HoleView extends FrameLayout {
    private static final float RADIUS =350;

    private Paint mBackgroundPaint;
    private float mCx = -1;
    private float mCy = -1;

    private int mTutorialColor = Color.parseColor("#D20E0F02");

    public HoleView(Context context) {
        super(context);
        init();
    }

    public HoleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HoleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HoleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // Set default hole position to the center
        mCx = getWidth() / 7;
        mCy = getHeight() / 7;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCx = event.getX();
        mCy = event.getY();
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mTutorialColor);
        if (mCx >= 0 && mCy >= 0) {
            canvas.drawCircle(mCx, mCy, RADIUS, mBackgroundPaint);
        }
    }
    public Bitmap getCroppedImage(Bitmap sourceImage) {
        if (sourceImage == null) {
            return null;
        }

        // Scale the bitmap to match the size of the HoleView
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(sourceImage, getWidth(), getHeight(), true);

        // Create a bitmap for the cropped image
        Bitmap croppedBitmap = Bitmap.createBitmap((int) RADIUS * 2, (int) RADIUS * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedBitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Create the circular path
        android.graphics.Path path = new android.graphics.Path();
        path.addCircle(RADIUS, RADIUS, RADIUS, android.graphics.Path.Direction.CCW);

        // Clip the canvas to the circular path
        canvas.clipPath(path);

        // Draw the scaled image inside the clipped area
        float left = mCx - RADIUS;
        float top = mCy - RADIUS;
        canvas.drawBitmap(scaledBitmap, -left, -top, paint);

        return croppedBitmap;
    }
}