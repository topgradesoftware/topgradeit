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

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Listener for tap events on ZoomageView
 */
public interface OnTapListener {
    /**
     * Called when a single tap is confirmed
     * @param view The ZoomageView that was tapped
     * @param event The motion event
     */
    void onSingleTapConfirmed(@NonNull ZoomageView view, @Nullable MotionEvent event);
    
    /**
     * Called when a double tap is detected
     * @param view The ZoomageView that was double tapped
     * @param event The motion event
     */
    void onDoubleTap(@NonNull ZoomageView view, @Nullable MotionEvent event);
}

