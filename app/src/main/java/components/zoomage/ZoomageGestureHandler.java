/**
 * Copyright 2016 Jeffrey Sibbold
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package components.zoomage;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.OverScroller;

import androidx.annotation.NonNull;

/**
 * Helper class to handle gesture detection and processing for ZoomageView.
 * This separation reduces complexity in the main view class and makes testing easier.
 */
class ZoomageGestureHandler {

    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;
    private final OverScroller flingScroller;
    
    // Reusable objects to avoid GC pressure
    private final Matrix tempMatrix = new Matrix();
    private final float[] tempValues = new float[9];
    private final PointF lastFocusPoint = new PointF(0, 0);
    
    private boolean doubleTapDetected = false;
    private boolean singleTapDetected = false;
    private float scaleBy = 1f;
    private float startScale = 1f;
    
    private int previousPointerCount = 1;
    private int currentPointerCount = 0;
    
    private final GestureCallback callback;

    /**
     * Interface for callbacks from gesture handler to the view
     */
    interface GestureCallback {
        boolean isZoomable();
        boolean isTranslatable();
        boolean isDoubleTapToZoomEnabled();
        float getCurrentScaleFactor();
        float getCalculatedMinScale();
        float getCalculatedMaxScale();
        float[] getMatrixValues();
        void onScale(float scaleBy, float focusX, float focusY);
        void onTranslate(float dx, float dy);
        void onDoubleTapZoom();
        void onFling(float velocityX, float velocityY);
        void onSingleTapConfirmed();
        void onScaleBegin();
        void onScaleEnd();
        boolean isScaleInProgress();
        int getViewWidth();
        int getViewHeight();
        RectF getCurrentBounds();
    }

    ZoomageGestureHandler(@NonNull Context context, @NonNull GestureCallback callback) {
        this.callback = callback;
        this.scaleDetector = new ScaleGestureDetector(context, scaleListener);
        this.gestureDetector = new GestureDetector(context, tapListener);
        this.flingScroller = new OverScroller(context);
        androidx.core.view.ScaleGestureDetectorCompat.setQuickScaleEnabled(scaleDetector, false);
    }

    boolean onTouchEvent(@NonNull MotionEvent event) {
        currentPointerCount = event.getPointerCount();
        
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        if (callback.isDoubleTapToZoomEnabled() && doubleTapDetected) {
            doubleTapDetected = false;
            singleTapDetected = false;
            callback.onDoubleTapZoom();
            return true;
        }

        if (!singleTapDetected) {
            handleMovement(event);
        }

        previousPointerCount = currentPointerCount;
        return true;
    }

    private void handleMovement(@NonNull MotionEvent event) {
        final int action = event.getActionMasked();
        
        if (action == MotionEvent.ACTION_DOWN || currentPointerCount != previousPointerCount) {
            lastFocusPoint.set(scaleDetector.getFocusX(), scaleDetector.getFocusY());
            flingScroller.forceFinished(true);
        } else if (action == MotionEvent.ACTION_MOVE) {
            final float focusX = scaleDetector.getFocusX();
            final float focusY = scaleDetector.getFocusY();

            if (allowTranslate()) {
                final float dx = focusX - lastFocusPoint.x;
                final float dy = focusY - lastFocusPoint.y;
                callback.onTranslate(dx, dy);
            }

            if (allowZoom()) {
                callback.onScale(scaleBy, focusX, focusY);
            }

            lastFocusPoint.set(focusX, focusY);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            scaleBy = 1f;
        }
    }

    boolean computeFling() {
        if (flingScroller.computeScrollOffset()) {
            return true;
        }
        return false;
    }

    int getFlingCurrX() {
        return flingScroller.getCurrX();
    }

    int getFlingCurrY() {
        return flingScroller.getCurrY();
    }

    void resetFling() {
        flingScroller.forceFinished(true);
    }

    private boolean allowTranslate() {
        return callback.isTranslatable() && callback.getCurrentScaleFactor() > 1.0f;
    }

    private boolean allowZoom() {
        return callback.isZoomable();
    }

    boolean shouldDisallowParentTouch() {
        return currentPointerCount > 1 || callback.getCurrentScaleFactor() > 1.0f;
    }

    int getCurrentPointerCount() {
        return currentPointerCount;
    }

    void reset() {
        scaleBy = 1f;
        doubleTapDetected = false;
        singleTapDetected = false;
        flingScroller.forceFinished(true);
    }

    private final ScaleGestureDetector.OnScaleGestureListener scaleListener = 
            new ScaleGestureDetector.OnScaleGestureListener() {
        
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            final float[] matrixValues = callback.getMatrixValues();
            
            // Calculate value we should scale by
            scaleBy = (startScale * detector.getScaleFactor()) / matrixValues[Matrix.MSCALE_X];

            // What the scaling should end up at after the transformation
            final float projectedScale = scaleBy * matrixValues[Matrix.MSCALE_X];

            // Clamp to the min/max if it's going over
            if (projectedScale < callback.getCalculatedMinScale()) {
                scaleBy = callback.getCalculatedMinScale() / matrixValues[Matrix.MSCALE_X];
            } else if (projectedScale > callback.getCalculatedMaxScale()) {
                scaleBy = callback.getCalculatedMaxScale() / matrixValues[Matrix.MSCALE_X];
            }

            return false;
        }

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            startScale = callback.getMatrixValues()[Matrix.MSCALE_X];
            callback.onScaleBegin();
            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            scaleBy = 1f;
            callback.onScaleEnd();
        }
    };

    private final GestureDetector.SimpleOnGestureListener tapListener = 
            new GestureDetector.SimpleOnGestureListener() {
        
        @Override
        public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                doubleTapDetected = true;
            }
            singleTapDetected = false;
            return false;
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            singleTapDetected = true;
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            singleTapDetected = false;
            callback.onSingleTapConfirmed();
            return false;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, 
                               float velocityX, float velocityY) {
            if (callback.isTranslatable() && callback.getCurrentScaleFactor() > 1.0f) {
                final RectF bounds = callback.getCurrentBounds();
                final int viewWidth = callback.getViewWidth();
                final int viewHeight = callback.getViewHeight();
                
                // Calculate scroll bounds
                final int startX = 0;
                final int startY = 0;
                final int minX, maxX, minY, maxY;
                
                if (bounds.width() > viewWidth) {
                    minX = (int) (viewWidth - bounds.width());
                    maxX = 0;
                } else {
                    minX = 0;
                    maxX = 0;
                }
                
                if (bounds.height() > viewHeight) {
                    minY = (int) (viewHeight - bounds.height());
                    maxY = 0;
                } else {
                    minY = 0;
                    maxY = 0;
                }
                
                flingScroller.fling(
                    startX, startY,
                    (int) velocityX, (int) velocityY,
                    minX, maxX,
                    minY, maxY,
                    viewWidth / 2, viewHeight / 2
                );
                
                callback.onFling(velocityX, velocityY);
                return true;
            }
            return false;
        }
    };
}

