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

import androidx.annotation.NonNull;

/**
 * Listener for zoom start and end events in ZoomageView.
 * Useful for tracking when a user begins or finishes a pinch-zoom gesture.
 */
public interface OnZoomListener {
    /**
     * Called when a zoom gesture starts (pinch gesture begins)
     * 
     * @param view The ZoomageView where zooming started
     */
    void onZoomStart(@NonNull ZoomageView view);
    
    /**
     * Called when a zoom gesture ends (pinch gesture finished)
     * 
     * @param view The ZoomageView where zooming ended
     */
    void onZoomEnd(@NonNull ZoomageView view);
}

