// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tzu, "The Art of War"

package components.cropper;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

import androidx.annotation.NonNull;

/**
 * Smooth animation for transitioning between two crop states:
 * handles image matrix (zoom/translate) and crop window bounds.
 */
final class CropImageAnimation extends Animation implements Animation.AnimationListener {

    // region Fields

    private final ImageView imageView;
    private final CropOverlayView overlayView;

    private final float[] startBoundPoints = new float[8];
    private final float[] endBoundPoints = new float[8];
    private final RectF startCropRect = new RectF();
    private final RectF endCropRect = new RectF();

    private final float[] startMatrixValues = new float[9];
    private final float[] endMatrixValues = new float[9];
    private final float[] animMatrixValues = new float[9];
    private final float[] animBoundPoints = new float[8];
    private final RectF animRect = new RectF();

    private static final long DEFAULT_DURATION_MS = 300L;

    // endregion

    CropImageAnimation(@NonNull ImageView imageView, @NonNull CropOverlayView overlayView) {
        this.imageView = imageView;
        this.overlayView = overlayView;

        setDuration(DEFAULT_DURATION_MS);
        setInterpolator(new AccelerateDecelerateInterpolator());
        setFillAfter(true);
        setAnimationListener(this);
    }

    /** Capture the starting state before animation begins. */
    void setStartState(@NonNull float[] boundPoints, @NonNull Matrix imageMatrix) {
        reset();
        System.arraycopy(boundPoints, 0, startBoundPoints, 0, startBoundPoints.length);
        startCropRect.set(overlayView.getCropWindowRect());
        imageMatrix.getValues(startMatrixValues);
    }

    /** Capture the end state after transformations are calculated. */
    void setEndState(@NonNull float[] boundPoints, @NonNull Matrix imageMatrix) {
        System.arraycopy(boundPoints, 0, endBoundPoints, 0, endBoundPoints.length);
        endCropRect.set(overlayView.getCropWindowRect());
        imageMatrix.getValues(endMatrixValues);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, @NonNull Transformation t) {
        // Interpolate crop window
        animRect.left = lerp(startCropRect.left, endCropRect.left, interpolatedTime);
        animRect.top = lerp(startCropRect.top, endCropRect.top, interpolatedTime);
        animRect.right = lerp(startCropRect.right, endCropRect.right, interpolatedTime);
        animRect.bottom = lerp(startCropRect.bottom, endCropRect.bottom, interpolatedTime);
        overlayView.setCropWindowRect(animRect);

        // Interpolate bounds
        for (int i = 0; i < animBoundPoints.length; i++) {
            animBoundPoints[i] = lerp(startBoundPoints[i], endBoundPoints[i], interpolatedTime);
        }
        overlayView.setBounds(animBoundPoints, imageView.getWidth(), imageView.getHeight());

        // Interpolate matrix
        for (int i = 0; i < animMatrixValues.length; i++) {
            animMatrixValues[i] = lerp(startMatrixValues[i], endMatrixValues[i], interpolatedTime);
        }
        Matrix matrix = imageView.getImageMatrix();
        matrix.setValues(animMatrixValues);
        imageView.setImageMatrix(matrix);

        // Refresh
        imageView.invalidate();
        overlayView.invalidate();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // No-op
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        // Ensure final matrix is applied and animation cleared
        imageView.clearAnimation();
        imageView.invalidate();
        overlayView.invalidate();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // No-op
    }

    // region Helper Methods

    /** Linear interpolation helper (safe float math). */
    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    // endregion
}