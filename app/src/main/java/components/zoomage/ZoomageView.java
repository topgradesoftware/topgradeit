/**
 * Copyright 2016 Jeffrey Sibbold
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package components.zoomage;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import topgrade.parent.com.parentseeks.R;

/**
 * ZoomageView is a pinch-to-zoom extension of {@link ImageView}, providing a smooth
 * user experience and a very natural feel when zooming and translating. It also supports
 * automatic resetting, and allows for exterior bounds restriction to keep the image within
 * visible window.
 * 
 * <p>Performance optimizations include object reuse to reduce GC pressure, gesture handler
 * separation for better maintainability, fling support for inertial scrolling, and state
 * persistence for configuration changes.</p>
 */
@SuppressWarnings("unused")
public class ZoomageView extends AppCompatImageView implements ZoomageGestureHandler.GestureCallback {

    // =============================================================================================
    // Constants
    // =============================================================================================
    
    private static final float MIN_SCALE = 0.6f;
    private static final float MAX_SCALE = 8f;
    private static final float DEFAULT_DOUBLE_TAP_ZOOM = 3f;
    private static final float ZOOM_THRESHOLD = 1.05f;
    private static final float OVERSCROLL_RESISTANCE_THRESHOLD = 50f;
    private static final float OVERSCROLL_RESISTANCE_FACTOR = 0.4f;
    
    // =============================================================================================
    // Fields
    // =============================================================================================
    
    private int resetDuration;
    
    @Nullable
    private ScaleType startScaleType;

    // Reusable matrices to avoid GC pressure
    private final Matrix currentMatrix = new Matrix();
    private final Matrix animationMatrix = new Matrix();
    @NonNull
    private Matrix baseMatrix = new Matrix();

    // Reusable arrays to avoid allocations in touch events
    private final float[] currentMatrixValues = new float[9];
    private final float[] animationValues = new float[9];
    @Nullable
    private float[] baseValues = null;

    private float minScale = MIN_SCALE;
    private float maxScale = MAX_SCALE;

    // The adjusted scale bounds that account for an image's starting scale values
    private float calculatedMinScale = MIN_SCALE;
    private float calculatedMaxScale = MAX_SCALE;

    private final RectF bounds = new RectF();
    private final RectF tempBounds = new RectF();

    private boolean translatable;
    private boolean zoomable;
    private boolean doubleTapToZoom;
    private boolean restrictBounds;
    private boolean animateOnReset;
    private boolean autoCenter;
    private boolean enableOverscrollResistance = true;
    private float doubleTapToZoomScaleFactor;
    @AutoResetMode 
    private int autoResetMode;

    private float currentScaleFactor = 1f;
    private float previousScaleFactor = 1f;
    private boolean isScaleInProgress = false;

    @Nullable
    private ZoomageGestureHandler gestureHandler;
    @Nullable
    private ValueAnimator resetAnimator;
    @Nullable
    private ValueAnimator flingAnimator;

    // Listener callbacks
    @Nullable
    private OnZoomChangeListener onZoomChangeListener;
    @Nullable
    private OnZoomListener onZoomListener;
    @Nullable
    private OnTapListener onTapListener;
    @Nullable
    private OnMatrixChangeListener onMatrixChangeListener;
    
    // Fling tracking
    private int flingStartX = 0;
    private int flingStartY = 0;

    public ZoomageView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public ZoomageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ZoomageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        gestureHandler = new ZoomageGestureHandler(context, this);
        startScaleType = getScaleType();
        
        // Load animation duration from resources
        resetDuration = getResources().getInteger(R.integer.zoomage_reset_duration);

        try (TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.ZoomageView)) {
            zoomable = values.getBoolean(R.styleable.ZoomageView_zoomage_zoomable, true);
            translatable = values.getBoolean(R.styleable.ZoomageView_zoomage_translatable, true);
            animateOnReset = values.getBoolean(R.styleable.ZoomageView_zoomage_animateOnReset, true);
            autoCenter = values.getBoolean(R.styleable.ZoomageView_zoomage_autoCenter, true);
            restrictBounds = values.getBoolean(R.styleable.ZoomageView_zoomage_restrictBounds, false);
            doubleTapToZoom = values.getBoolean(R.styleable.ZoomageView_zoomage_doubleTapToZoom, true);
            minScale = values.getFloat(R.styleable.ZoomageView_zoomage_minScale, MIN_SCALE);
            maxScale = values.getFloat(R.styleable.ZoomageView_zoomage_maxScale, MAX_SCALE);
            doubleTapToZoomScaleFactor = values.getFloat(R.styleable.ZoomageView_zoomage_doubleTapToZoomScaleFactor, DEFAULT_DOUBLE_TAP_ZOOM);
            autoResetMode = AutoResetMode.Parser.fromInt(values.getInt(R.styleable.ZoomageView_zoomage_autoResetMode, AutoResetMode.UNDER));
        }

        verifyScaleRange();
    }

    private void verifyScaleRange() {
        if (minScale >= maxScale) {
            throw new IllegalStateException("minScale must be less than maxScale");
        }

        if (minScale < 0) {
            throw new IllegalStateException("minScale must be greater than 0");
        }

        if (doubleTapToZoomScaleFactor > maxScale) {
            doubleTapToZoomScaleFactor = maxScale;
        }

        if (doubleTapToZoomScaleFactor < minScale) {
            doubleTapToZoomScaleFactor = minScale;
        }
    }

    /**
     * Set the minimum and maximum allowed scale for zooming. {@code minScale} cannot
     * be greater than {@code maxScale} and neither can be 0 or less. This will result
     * in an {@link IllegalStateException}.
     *
     * @param minScale minimum allowed scale
     * @param maxScale maximum allowed scale
     */
    public void setScaleRange(final float minScale, final float maxScale) {
        this.minScale = minScale;
        this.maxScale = maxScale;

        baseValues = null;

        verifyScaleRange();
    }

    /**
     * Set the listener for zoom change events
     * 
     * @param listener The listener to be notified of zoom changes
     */
    public void setOnZoomChangeListener(@Nullable OnZoomChangeListener listener) {
        this.onZoomChangeListener = listener;
    }

    /**
     * Set the listener for tap events
     * 
     * @param listener The listener to be notified of tap events
     */
    public void setOnTapListener(@Nullable OnTapListener listener) {
        this.onTapListener = listener;
    }

    /**
     * Set the listener for zoom start/end events
     * 
     * @param listener The listener to be notified when zooming starts and ends
     */
    public void setOnZoomListener(@Nullable OnZoomListener listener) {
        this.onZoomListener = listener;
    }

    /**
     * Set the listener for matrix change events
     * 
     * @param listener The listener to be notified when the image matrix changes
     */
    public void setOnMatrixChangeListener(@Nullable OnMatrixChangeListener listener) {
        this.onMatrixChangeListener = listener;
    }

    /**
     * Returns whether the image is translatable.
     *
     * @return true if translation of image is allowed, false otherwise
     */
    public boolean isTranslatable() {
        return translatable;
    }

    /**
     * Set the image's translatable state.
     *
     * @param translatable true to enable translation, false to disable it
     */
    public void setTranslatable(final boolean translatable) {
        this.translatable = translatable;
    }

    /**
     * Returns the zoomable state of the image.
     *
     * @return true if pinch-zooming of the image is allowed, false otherwise.
     */
    public boolean isZoomable() {
        return zoomable;
    }

    /**
     * Set the zoomable state of the image.
     *
     * @param zoomable true to enable pinch-zooming of the image, false to disable it
     */
    public void setZoomable(final boolean zoomable) {
        this.zoomable = zoomable;
    }

    /**
     * If restricted bounds are enabled, the image will not be allowed to translate
     * farther inward than the edges of the view's bounds, unless the corresponding
     * dimension (width or height) is smaller than those of the view's frame.
     *
     * @return true if image bounds are restricted to the view's edges, false otherwise
     */
    public boolean getRestrictBounds() {
        return restrictBounds;
    }

    /**
     * Set the restrictBounds status of the image.
     * If restricted bounds are enabled, the image will not be allowed to translate
     * farther inward than the edges of the view's bounds, unless the corresponding
     * dimension (width or height) is smaller than those of the view's frame.
     *
     * @param restrictBounds true if image bounds should be restricted to the view's edges, false otherwise
     */
    public void setRestrictBounds(final boolean restrictBounds) {
        this.restrictBounds = restrictBounds;
    }

    /**
     * Returns status of animateOnReset. This causes the image to smoothly animate back
     * to its start position when reset. Default value is true.
     *
     * @return true if animateOnReset is enabled, false otherwise
     */
    public boolean getAnimateOnReset() {
        return animateOnReset;
    }

    /**
     * Set whether or not the image should animate when resetting.
     *
     * @param animateOnReset true if image should animate when resetting, false to snap
     */
    public void setAnimateOnReset(final boolean animateOnReset) {
        this.animateOnReset = animateOnReset;
    }

    /**
     * Get the current {@link AutoResetMode} mode of the image. Default value is {@link AutoResetMode#UNDER}.
     *
     * @return the current {@link AutoResetMode} mode, one of {@link AutoResetMode#OVER OVER}, {@link AutoResetMode#UNDER UNDER},
     * {@link AutoResetMode#ALWAYS ALWAYS}, or {@link AutoResetMode#NEVER NEVER}
     */
    @AutoResetMode
    public int getAutoResetMode() {
        return autoResetMode;
    }

    /**
     * Set the {@link AutoResetMode} mode for the image.
     *
     * @param autoReset the desired mode, one of {@link AutoResetMode#OVER OVER}, {@link AutoResetMode#UNDER UNDER},
     *                  {@link AutoResetMode#ALWAYS ALWAYS}, or {@link AutoResetMode#NEVER NEVER}
     */
    public void setAutoResetMode(@AutoResetMode final int autoReset) {
        this.autoResetMode = autoReset;
    }

    /**
     * Whether or not the image should automatically center itself when it's dragged partially or
     * fully out of view.
     *
     * @return true if image should center itself automatically, false if it should not
     */
    public boolean getAutoCenter() {
        return autoCenter;
    }

    /**
     * Set whether or not the image should automatically center itself when it's dragged
     * partially or fully out of view.
     *
     * @param autoCenter true if image should center itself automatically, false if it should not
     */
    public void setAutoCenter(final boolean autoCenter) {
        this.autoCenter = autoCenter;
    }

    /**
     * Gets double tap to zoom state.
     *
     * @return whether double tap to zoom is enabled
     */
    public boolean getDoubleTapToZoom() {
        return doubleTapToZoom;
    }

    /**
     * Sets double tap to zoom state.
     *
     * @param doubleTapToZoom true if double tap to zoom should be enabled
     */
    public void setDoubleTapToZoom(final boolean doubleTapToZoom) {
        this.doubleTapToZoom = doubleTapToZoom;
    }

    /**
     * Gets the double tap to zoom scale factor.
     *
     * @return double tap to zoom scale factor
     */
    public float getDoubleTapToZoomScaleFactor() {
        return doubleTapToZoomScaleFactor;
    }

    /**
     * Sets the double tap to zoom scale factor. Can be a maximum of max scale.
     *
     * @param doubleTapToZoomScaleFactor the scale factor you want to zoom to when double tap occurs
     */
    public void setDoubleTapToZoomScaleFactor(final float doubleTapToZoomScaleFactor) {
        this.doubleTapToZoomScaleFactor = doubleTapToZoomScaleFactor;
        verifyScaleRange();
    }

    /**
     * Get the current scale factor of the image, in relation to its starting size.
     *
     * @return the current scale factor
     */
    public float getCurrentScaleFactor() {
        return currentScaleFactor;
    }

    /**
     * Check whether the image is currently zoomed in beyond the threshold.
     *
     * @return true if the image is zoomed in, false otherwise
     */
    public boolean isZoomed() {
        return currentScaleFactor > ZOOM_THRESHOLD;
    }

    /**
     * Programmatically zoom to a specific scale factor at the given focus point.
     *
     * @param scaleFactor the target scale factor
     * @param focusX the x coordinate of the zoom focus point
     * @param focusY the y coordinate of the zoom focus point
     * @param animate whether to animate the zoom transition
     */
    public void zoomTo(final float scaleFactor, final float focusX, final float focusY, final boolean animate) {
        if (getDrawable() == null || baseValues == null) {
            return;
        }

        final float targetScale = baseValues[Matrix.MSCALE_X] * scaleFactor;
        final float clampedScale = Math.max(calculatedMinScale, Math.min(calculatedMaxScale, targetScale));

        animationMatrix.set(getImageMatrix());
        final float currentScale = currentMatrixValues[Matrix.MSCALE_X];
        final float scaleBy = clampedScale / currentScale;
        animationMatrix.postScale(scaleBy, scaleBy, focusX, focusY);

        clampToBounds(animationMatrix);

        if (animate) {
            animateScaleAndTranslationToMatrix(animationMatrix, resetDuration);
        } else {
            setImageMatrix(animationMatrix);
            updateScaleFromMatrix();
        }
    }

    /**
     * Get whether overscroll resistance is enabled.
     *
     * @return true if overscroll resistance is enabled, false otherwise
     */
    public boolean getEnableOverscrollResistance() {
        return enableOverscrollResistance;
    }

    /**
     * Set whether to enable elastic overscroll resistance at image edges.
     *
     * @param enable true to enable overscroll resistance, false to disable
     */
    public void setEnableOverscrollResistance(final boolean enable) {
        this.enableOverscrollResistance = enable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setScaleType(@Nullable ScaleType scaleType) {
        if (scaleType != null) {
            super.setScaleType(scaleType);
            startScaleType = scaleType;
            baseValues = null;
        }
    }

    /**
     * Set enabled state of the view. Note that this will reset the image's
     * {@link android.widget.ImageView.ScaleType} to its pre-zoom state.
     *
     * @param enabled enabled state
     */
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            setScaleType(startScaleType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImageResource(final int resId) {
        super.setImageResource(resId);
        setScaleType(startScaleType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        setScaleType(startScaleType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImageBitmap(@Nullable Bitmap bm) {
        super.setImageBitmap(bm);
        setScaleType(startScaleType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImageURI(@Nullable Uri uri) {
        super.setImageURI(uri);
        setScaleType(startScaleType);
    }

    /**
     * Update the bounds of the displayed image based on the current matrix.
     * Uses cached currentMatrixValues to avoid redundant getValues() calls.
     */
    private void updateBounds() {
        final Drawable drawable = getDrawable();
        if (drawable != null) {
            bounds.set(currentMatrixValues[Matrix.MTRANS_X],
                    currentMatrixValues[Matrix.MTRANS_Y],
                    drawable.getIntrinsicWidth() * currentMatrixValues[Matrix.MSCALE_X] + currentMatrixValues[Matrix.MTRANS_X],
                    drawable.getIntrinsicHeight() * currentMatrixValues[Matrix.MSCALE_Y] + currentMatrixValues[Matrix.MTRANS_Y]);
        }
    }

    /**
     * Get the width of the displayed image.
     *
     * @return the current width of the image as displayed (not the width of the {@link ImageView} itself.
     */
    private float getCurrentDisplayedWidth() {
        final Drawable drawable = getDrawable();
        if (drawable != null) {
            return drawable.getIntrinsicWidth() * currentMatrixValues[Matrix.MSCALE_X];
        } else {
            return 0;
        }
    }

    /**
     * Get the height of the displayed image.
     *
     * @return the current height of the image as displayed (not the height of the {@link ImageView} itself.
     */
    private float getCurrentDisplayedHeight() {
        final Drawable drawable = getDrawable();
        if (drawable != null) {
            return drawable.getIntrinsicHeight() * currentMatrixValues[Matrix.MSCALE_Y];
        } else {
            return 0;
        }
    }

    /**
     * Remember our starting values so we can animate our image back to its original position.
     */
    private void setStartValues() {
        baseValues = new float[9];
        baseMatrix = new Matrix(getImageMatrix());
        baseMatrix.getValues(baseValues);
        calculatedMinScale = minScale * baseValues[Matrix.MSCALE_X];
        calculatedMaxScale = maxScale * baseValues[Matrix.MSCALE_X];
    }

    /**
     * Update the current scale factor from the matrix values.
     */
    private void updateScaleFromMatrix() {
        if (baseValues != null) {
            currentScaleFactor = currentMatrixValues[Matrix.MSCALE_X] / baseValues[Matrix.MSCALE_X];
        }
    }

    /**
     * Notify matrix change listeners.
     */
    private void notifyMatrixChange() {
        if (onMatrixChangeListener != null) {
            onMatrixChangeListener.onMatrixChange(this, getImageMatrix());
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (getDrawable() == null) {
            return super.onTouchEvent(event);
        }

        if (!isClickable() && isEnabled() && (zoomable || translatable)) {
            if (getScaleType() != ScaleType.MATRIX) {
                super.setScaleType(ScaleType.MATRIX);
            }

            if (baseValues == null) {
                setStartValues();
            }

            // Get the current state of the image matrix and its values
            // Call getValues() only once per event to reduce overhead
            currentMatrix.set(getImageMatrix());
            currentMatrix.getValues(currentMatrixValues);
            updateBounds();

            // Request parent not to intercept touch early to prevent issues with ScrollView parents
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }

            final boolean handled = gestureHandler != null && gestureHandler.onTouchEvent(event);

            if (event.getActionMasked() == MotionEvent.ACTION_UP ||
                event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                resetImage();
                
                // Call performClick for accessibility when ACTION_UP is detected
                if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    performClick();
                }
                
                // Allow parent to intercept touch if we're not zoomed
                if (getParent() != null && gestureHandler != null) {
                    getParent().requestDisallowInterceptTouchEvent(
                        gestureHandler.shouldDisallowParentTouch() || isAnimating()
                    );
                }
            }

            return handled;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private boolean isAnimating() {
        return (resetAnimator != null && resetAnimator.isRunning()) ||
               (flingAnimator != null && flingAnimator.isRunning());
    }

    /**
     * Reset the image based on the specified {@link AutoResetMode} mode.
     */
    private void resetImage() {
        if (baseValues == null) {
            return;
        }
        
        switch (autoResetMode) {
            case AutoResetMode.UNDER:
                if (currentMatrixValues[Matrix.MSCALE_X] <= baseValues[Matrix.MSCALE_X]) {
                    reset();
                } else {
                    center();
                }
                break;
            case AutoResetMode.OVER:
                if (currentMatrixValues[Matrix.MSCALE_X] >= baseValues[Matrix.MSCALE_X]) {
                    reset();
                } else {
                    center();
                }
                break;
            case AutoResetMode.ALWAYS:
                reset();
                break;
            case AutoResetMode.NEVER:
                center();
        }
    }

    /**
     * This helps to keep the image on-screen by animating the translation to the nearest
     * edge, both vertically and horizontally.
     */
    private void center() {
        if (autoCenter) {
            // Clamp to bounds using unified utility
            animationMatrix.set(currentMatrix);
            clampToBounds(animationMatrix);
            
            // Compare matrices efficiently by checking scale/translation deltas
            if (shouldAnimateToMatrix(currentMatrix, animationMatrix)) {
                animateScaleAndTranslationToMatrix(animationMatrix, resetDuration);
            }
        }
    }

    /**
     * Check if animation is needed by comparing matrix values.
     * More efficient than Matrix.equals() which is expensive.
     */
    private boolean shouldAnimateToMatrix(@NonNull Matrix from, @NonNull Matrix to) {
        final float[] fromValues = new float[9];
        final float[] toValues = new float[9];
        from.getValues(fromValues);
        to.getValues(toValues);
        
        final float scaleDelta = Math.abs(toValues[Matrix.MSCALE_X] - fromValues[Matrix.MSCALE_X]);
        final float transXDelta = Math.abs(toValues[Matrix.MTRANS_X] - fromValues[Matrix.MTRANS_X]);
        final float transYDelta = Math.abs(toValues[Matrix.MTRANS_Y] - fromValues[Matrix.MTRANS_Y]);
        
        return scaleDelta > 0.001f || transXDelta > 0.5f || transYDelta > 0.5f;
    }

    /**
     * Unified matrix bounds clamping utility.
     * This method ensures the image stays within reasonable bounds.
     *
     * @param targetMatrix the matrix to clamp
     */
    private void clampToBounds(@NonNull Matrix targetMatrix) {
        targetMatrix.getValues(animationValues);
        updateTempBounds(animationValues);
        
        float deltaX = 0;
        float deltaY = 0;

        if (tempBounds.width() >= getWidth()) {
            if (tempBounds.left > 0) {
                deltaX = -tempBounds.left;
            } else if (tempBounds.right < getWidth()) {
                deltaX = getWidth() - tempBounds.right;
            }
        } else {
            deltaX = (getWidth() - tempBounds.width()) / 2 - tempBounds.left;
        }

        if (tempBounds.height() >= getHeight()) {
            if (tempBounds.top > 0) {
                deltaY = -tempBounds.top;
            } else if (tempBounds.bottom < getHeight()) {
                deltaY = getHeight() - tempBounds.bottom;
            }
        } else {
            deltaY = (getHeight() - tempBounds.height()) / 2 - tempBounds.top;
        }

        // Apply elastic overscroll resistance for a more polished UX
        if (enableOverscrollResistance) {
            deltaX = applyOverscrollResistance(deltaX);
            deltaY = applyOverscrollResistance(deltaY);
        }

        if (deltaX != 0 || deltaY != 0) {
            targetMatrix.postTranslate(deltaX, deltaY);
        }
    }

    /**
     * Apply elastic overscroll resistance to a translation delta.
     * This gives a subtle bounce feel when reaching image edges.
     *
     * @param delta the translation delta
     * @return the delta with resistance applied
     */
    private float applyOverscrollResistance(final float delta) {
        final float absDelta = Math.abs(delta);
        if (absDelta > 0 && absDelta < OVERSCROLL_RESISTANCE_THRESHOLD) {
            return delta * OVERSCROLL_RESISTANCE_FACTOR;
        }
        return delta;
    }

    /**
     * Update temp bounds based on provided matrix values
     */
    private void updateTempBounds(@NonNull float[] values) {
        final Drawable drawable = getDrawable();
        if (drawable != null) {
            tempBounds.set(values[Matrix.MTRANS_X],
                    values[Matrix.MTRANS_Y],
                    drawable.getIntrinsicWidth() * values[Matrix.MSCALE_X] + values[Matrix.MTRANS_X],
                    drawable.getIntrinsicHeight() * values[Matrix.MSCALE_Y] + values[Matrix.MTRANS_Y]);
        }
    }

    /**
     * Reset image back to its original size. Will snap back to original size
     * if animation on reset is disabled via {@link #setAnimateOnReset(boolean)}.
     */
    public void reset() {
        reset(animateOnReset);
    }

    /**
     * Reset image back to its starting size. If {@code animate} is false, image
     * will snap back to its original size.
     *
     * @param animate animate the image back to its starting size
     */
    public void reset(final boolean animate) {
        if (animate) {
            animateToStartMatrix();
        } else {
            setImageMatrix(baseMatrix);
            notifyZoomChange(false);
        }
    }

    /**
     * Animate the matrix back to its original position after the user stopped interacting with it.
     */
    private void animateToStartMatrix() {
        animateScaleAndTranslationToMatrix(baseMatrix, resetDuration);
    }

    /**
     * Animate the scale and translation of the current matrix to the target matrix.
     *
     * @param targetMatrix the target matrix to animate values to
     * @param duration the animation duration in milliseconds
     */
    private void animateScaleAndTranslationToMatrix(@NonNull final Matrix targetMatrix, final int duration) {
        // Cancel any existing animation to avoid conflicts
        if (resetAnimator != null && resetAnimator.isRunning()) {
            resetAnimator.cancel();
        }

        // Get target values using existing animationValues array
        final float[] targetValues = new float[9];
        targetMatrix.getValues(targetValues);

        // Get start values - use existing animationMatrix to avoid allocation
        animationMatrix.set(getImageMatrix());
        final float[] startValues = new float[9];
        animationMatrix.getValues(startValues);

        // Calculate differences in current and target values
        final float xsdiff = targetValues[Matrix.MSCALE_X] - startValues[Matrix.MSCALE_X];
        final float ysdiff = targetValues[Matrix.MSCALE_Y] - startValues[Matrix.MSCALE_Y];
        final float xtdiff = targetValues[Matrix.MTRANS_X] - startValues[Matrix.MTRANS_X];
        final float ytdiff = targetValues[Matrix.MTRANS_Y] - startValues[Matrix.MTRANS_Y];

        // Reusable Matrix and array for animation updates (cached outside listener)
        final Matrix activeMatrix = new Matrix();
        final float[] activeValues = new float[9];

        resetAnimator = ValueAnimator.ofFloat(0, 1f);
        resetAnimator.addUpdateListener(animation -> {
            final float val = (Float) animation.getAnimatedValue();
            
            // Build interpolated matrix values
            System.arraycopy(startValues, 0, activeValues, 0, 9);
            activeValues[Matrix.MTRANS_X] += xtdiff * val;
            activeValues[Matrix.MTRANS_Y] += ytdiff * val;
            activeValues[Matrix.MSCALE_X] += xsdiff * val;
            activeValues[Matrix.MSCALE_Y] += ysdiff * val;
            
            activeMatrix.setValues(activeValues);
            setImageMatrix(activeMatrix);
            
            // Update current scale factor during animation
            if (baseValues != null) {
                currentScaleFactor = activeValues[Matrix.MSCALE_X] / baseValues[Matrix.MSCALE_X];
            }
            
            notifyMatrixChange();
        });

        resetAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                setImageMatrix(targetMatrix);
                updateScaleFromMatrix();
                notifyZoomChange(false);
                notifyMatrixChange();
            }
        });

        resetAnimator.setDuration(duration);
        resetAnimator.start();
    }

    /**
     * Get the x distance to translate the current image.
     *
     * @param toX   the current x location of touch focus
     * @param fromX the last x location of touch focus
     * @return the distance to move the image,
     * will restrict the translation to keep the image on screen.
     */
    private float getXDistance(final float toX, final float fromX) {
        float xdistance = toX - fromX;

        if (restrictBounds) {
            xdistance = getRestrictedXDistance(xdistance);
        }

        // Prevents image from translating an infinite distance offscreen
        if (bounds.right + xdistance < 0) {
            xdistance = -bounds.right;
        } else if (bounds.left + xdistance > getWidth()) {
            xdistance = getWidth() - bounds.left;
        }

        return xdistance;
    }

    /**
     * Get the horizontal distance to translate the current image, but restrict
     * it to the outer bounds of the {@link ImageView}. If the current
     * image is smaller than the bounds, keep it within the current bounds.
     * If it is larger, prevent its edges from translating farther inward
     * from the outer edge.
     *
     * @param xdistance the current desired horizontal distance to translate
     * @return the actual horizontal distance to translate with bounds restrictions
     */
    private float getRestrictedXDistance(final float xdistance) {
        float restrictedXDistance = xdistance;

        if (getCurrentDisplayedWidth() >= getWidth()) {
            if (bounds.left <= 0 && bounds.left + xdistance > 0 && !isScaleInProgress()) {
                restrictedXDistance = -bounds.left;
            } else if (bounds.right >= getWidth() && bounds.right + xdistance < getWidth() && !isScaleInProgress()) {
                restrictedXDistance = getWidth() - bounds.right;
            }
        } else if (!isScaleInProgress()) {
            if (bounds.left >= 0 && bounds.left + xdistance < 0) {
                restrictedXDistance = -bounds.left;
            } else if (bounds.right <= getWidth() && bounds.right + xdistance > getWidth()) {
                restrictedXDistance = getWidth() - bounds.right;
            }
        }

        return restrictedXDistance;
    }

    /**
     * Get the y distance to translate the current image.
     *
     * @param toY   the current y location of touch focus
     * @param fromY the last y location of touch focus
     * @return the distance to move the image,
     * will restrict the translation to keep the image on screen.
     */
    private float getYDistance(final float toY, final float fromY) {
        float ydistance = toY - fromY;

        if (restrictBounds) {
            ydistance = getRestrictedYDistance(ydistance);
        }

        // Prevents image from translating an infinite distance offscreen
        if (bounds.bottom + ydistance < 0) {
            ydistance = -bounds.bottom;
        } else if (bounds.top + ydistance > getHeight()) {
            ydistance = getHeight() - bounds.top;
        }

        return ydistance;
    }

    /**
     * Get the vertical distance to translate the current image, but restrict
     * it to the outer bounds of the {@link ImageView}. If the current
     * image is smaller than the bounds, keep it within the current bounds.
     * If it is larger, prevent its edges from translating farther inward
     * from the outer edge.
     *
     * @param ydistance the current desired vertical distance to translate
     * @return the actual vertical distance to translate with bounds restrictions
     */
    private float getRestrictedYDistance(final float ydistance) {
        float restrictedYDistance = ydistance;

        if (getCurrentDisplayedHeight() >= getHeight()) {
            if (bounds.top <= 0 && bounds.top + ydistance > 0 && !isScaleInProgress()) {
                restrictedYDistance = -bounds.top;
            } else if (bounds.bottom >= getHeight() && bounds.bottom + ydistance < getHeight() && !isScaleInProgress()) {
                restrictedYDistance = getHeight() - bounds.bottom;
            }
        } else if (!isScaleInProgress()) {
            if (bounds.top >= 0 && bounds.top + ydistance < 0) {
                restrictedYDistance = -bounds.top;
            } else if (bounds.bottom <= getHeight() && bounds.bottom + ydistance > getHeight()) {
                restrictedYDistance = getHeight() - bounds.bottom;
            }
        }

        return restrictedYDistance;
    }

    /**
     * Notify listeners of zoom change
     */
    private void notifyZoomChange(final boolean fromUser) {
        if (onZoomChangeListener != null && currentScaleFactor != previousScaleFactor) {
            onZoomChangeListener.onZoomChange(this, currentScaleFactor, fromUser);
            previousScaleFactor = currentScaleFactor;
        }
    }

    // =============================================================================================
    // GestureCallback implementation
    // =============================================================================================

    @Override
    public boolean isDoubleTapToZoomEnabled() {
        return doubleTapToZoom;
    }

    @Override
    public float getCalculatedMinScale() {
        return calculatedMinScale;
    }

    @Override
    public float getCalculatedMaxScale() {
        return calculatedMaxScale;
    }

    @Override
    public float[] getMatrixValues() {
        return currentMatrixValues;
    }

    @Override
    public void onScale(final float scaleBy, final float focusX, final float focusY) {
        currentMatrix.postScale(scaleBy, scaleBy, focusX, focusY);
        setImageMatrix(currentMatrix);
        
        // Update matrix values and current scale factor
        currentMatrix.getValues(currentMatrixValues);
        updateScaleFromMatrix();
        notifyZoomChange(true);
        notifyMatrixChange();
    }

    @Override
    public void onTranslate(final float dx, final float dy) {
        final float xdistance = getXDistance(dx + currentMatrixValues[Matrix.MTRANS_X], currentMatrixValues[Matrix.MTRANS_X]);
        final float ydistance = getYDistance(dy + currentMatrixValues[Matrix.MTRANS_Y], currentMatrixValues[Matrix.MTRANS_Y]);
        
        currentMatrix.postTranslate(xdistance, ydistance);
        setImageMatrix(currentMatrix);
        
        // Update matrix values after translation
        currentMatrix.getValues(currentMatrixValues);
        updateBounds();
        notifyMatrixChange();
    }

    @Override
    public void onDoubleTapZoom() {
        if (getDrawable() == null || baseValues == null) {
            return;
        }
        
        // Toggle between start scale and zoom scale for natural UX like Instagram/Google Photos
        final float currentScale = currentMatrixValues[Matrix.MSCALE_X];
        final float targetScale;
        
        if (currentScale > baseValues[Matrix.MSCALE_X] * 1.1f) {
            // If zoomed in, zoom out to start scale
            targetScale = baseValues[Matrix.MSCALE_X];
        } else {
            // If at start scale, zoom in to double tap zoom factor
            targetScale = baseValues[Matrix.MSCALE_X] * doubleTapToZoomScaleFactor;
        }
        
        // Use animationMatrix to avoid allocation
        animationMatrix.set(currentMatrix);
        final float scaleFactor = targetScale / currentScale;
        
        // Focus on center if gesture handler is available
        final float focusX = getWidth() / 2f;
        final float focusY = getHeight() / 2f;
        
        animationMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
        
        // Clamp the result to bounds
        clampToBounds(animationMatrix);
        
        animateScaleAndTranslationToMatrix(animationMatrix, resetDuration);
        
        if (onTapListener != null) {
            onTapListener.onDoubleTap(this, null);
        }
    }

    @Override
    public void onFling(final float velocityX, final float velocityY) {
        // Cancel any existing fling
        if (flingAnimator != null && flingAnimator.isRunning()) {
            flingAnimator.cancel();
        }
        
        // Start fling animation
        if (gestureHandler != null) {
            flingStartX = 0;
            flingStartY = 0;
            
            final int duration = getResources().getInteger(R.integer.zoomage_fling_duration);
            flingAnimator = ValueAnimator.ofFloat(0, 1f);
            flingAnimator.setDuration(duration);
            flingAnimator.addUpdateListener(animation -> {
                if (gestureHandler != null && gestureHandler.computeFling()) {
                    final int currX = gestureHandler.getFlingCurrX();
                    final int currY = gestureHandler.getFlingCurrY();
                    final float dx = currX - flingStartX;
                    final float dy = currY - flingStartY;
                    
                    onTranslate(dx, dy);
                    
                    flingStartX = currX;
                    flingStartY = currY;
                } else {
                    animation.cancel();
                }
            });
            flingAnimator.addListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    if (gestureHandler != null) {
                        gestureHandler.resetFling();
                    }
                }
            });
            flingAnimator.start();
        }
    }

    @Override
    public void onSingleTapConfirmed() {
        if (onTapListener != null) {
            onTapListener.onSingleTapConfirmed(this, null);
        }
    }

    @Override
    public void onScaleBegin() {
        isScaleInProgress = true;
        if (onZoomListener != null) {
            onZoomListener.onZoomStart(this);
        }
    }

    @Override
    public void onScaleEnd() {
        isScaleInProgress = false;
        if (onZoomListener != null) {
            onZoomListener.onZoomEnd(this);
        }
    }

    @Override
    public boolean isScaleInProgress() {
        return isScaleInProgress;
    }

    @Override
    public int getViewWidth() {
        return getWidth();
    }

    @Override
    public int getViewHeight() {
        return getHeight();
    }

    @Override
    public RectF getCurrentBounds() {
        return bounds;
    }

    // =============================================================================================
    // Lifecycle
    // =============================================================================================

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Cancel ongoing animations to prevent leaks
        if (resetAnimator != null && resetAnimator.isRunning()) {
            resetAnimator.cancel();
            resetAnimator = null;
        }
        if (flingAnimator != null && flingAnimator.isRunning()) {
            flingAnimator.cancel();
            flingAnimator = null;
        }
    }

    // =============================================================================================
    // State persistence
    // =============================================================================================

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (superState == null) {
            return null;
        }
        final SavedState savedState = new SavedState(superState);
        
        savedState.currentMatrix = new Matrix(getImageMatrix());
        savedState.baseMatrix = new Matrix(baseMatrix);
        savedState.currentScaleFactor = currentScaleFactor;
        
        if (baseValues != null) {
            savedState.baseValues = baseValues.clone();
        }
        
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Parcelable state) {
        if (!(state instanceof SavedState savedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        
        super.onRestoreInstanceState(savedState.getSuperState());
        
        if (savedState.currentMatrix != null) {
            setImageMatrix(savedState.currentMatrix);
        }
        
        if (savedState.baseMatrix != null) {
            baseMatrix = new Matrix(savedState.baseMatrix);
        }
        
        if (savedState.baseValues != null) {
            baseValues = savedState.baseValues.clone();
            calculatedMinScale = minScale * baseValues[Matrix.MSCALE_X];
            calculatedMaxScale = maxScale * baseValues[Matrix.MSCALE_X];
        }
        
        currentScaleFactor = savedState.currentScaleFactor;
    }

    /**
     * Saved state class for persisting zoom and translation state across configuration changes
     */
    protected static class SavedState extends BaseSavedState {
        @Nullable
        Matrix currentMatrix;
        @Nullable
        Matrix baseMatrix;
        @Nullable
        float[] baseValues;
        float currentScaleFactor;

        SavedState(@NonNull Parcelable superState) {
            super(superState);
        }

        private SavedState(@NonNull Parcel in) {
            super(in);
            
            final float[] currentMatrixValues = new float[9];
            in.readFloatArray(currentMatrixValues);
            currentMatrix = new Matrix();
            currentMatrix.setValues(currentMatrixValues);
            
            final float[] baseMatrixValues = new float[9];
            in.readFloatArray(baseMatrixValues);
            baseMatrix = new Matrix();
            baseMatrix.setValues(baseMatrixValues);
            
            baseValues = new float[9];
            in.readFloatArray(baseValues);
            
            currentScaleFactor = in.readFloat();
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            
            final float[] currentMatrixValues = new float[9];
            if (currentMatrix != null) {
                currentMatrix.getValues(currentMatrixValues);
            }
            out.writeFloatArray(currentMatrixValues);
            
            final float[] baseMatrixValues = new float[9];
            if (baseMatrix != null) {
                baseMatrix.getValues(baseMatrixValues);
            }
            out.writeFloatArray(baseMatrixValues);
            
            out.writeFloatArray(baseValues != null ? baseValues : new float[9]);
            
            out.writeFloat(currentScaleFactor);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = 
                new Parcelable.Creator<>() {
            @NonNull
            @Override
            public SavedState createFromParcel(@NonNull Parcel in) {
                return new SavedState(in);
            }

            @NonNull
            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * Simple animator listener base class to avoid implementing all methods
     */
    private static class SimpleAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(@NonNull Animator animation) {
        }

        @Override
        public void onAnimationEnd(@NonNull Animator animation) {
        }

        @Override
        public void onAnimationCancel(@NonNull Animator animation) {
        }

        @Override
        public void onAnimationRepeat(@NonNull Animator animation) {
        }
    }
}
