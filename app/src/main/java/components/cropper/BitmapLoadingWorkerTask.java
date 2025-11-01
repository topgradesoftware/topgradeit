// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package components.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Task to load bitmap asynchronously from the UI thread.
 * Designed to be lifecycle-safe and memory-leak resistant.
 */
final class BitmapLoadingWorkerTask {

  /** Shared executor for background decoding (limit CPU usage) */
  private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
      Math.max(2, Runtime.getRuntime().availableProcessors() / 2)
  );

  /** Handler for posting back to the main thread */
  private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

  /** Weak reference to the CropImageView (avoids leaks) */
  private final WeakReference<CropImageView> cropImageViewRef;

  /** Context (application-level for safety) */
  private final Context appContext;

  /** The Android URI of the image to load */
  private final Uri imageUri;

  /** Track if task was cancelled */
  private volatile boolean cancelled = false;

  /** Running background job */
  private Future<?> runningTask;

  // region: Constructor

  public BitmapLoadingWorkerTask(CropImageView cropImageView, Uri uri) {
    this.cropImageViewRef = new WeakReference<>(cropImageView);
    this.appContext = cropImageView.getContext().getApplicationContext();
    this.imageUri = uri;
  }

  // endregion

  /** The Android URI that this task is currently loading. */
  public Uri getUri() {
    return imageUri;
  }

  /** Start the loading task in background */
  public void execute() {
    runningTask = EXECUTOR.submit(this::doInBackground);
  }

  /** Cancel the task */
  public void cancel() {
    cancelled = true;
    if (runningTask != null) runningTask.cancel(true);
  }

  /** Check if task is cancelled */
  public boolean isCancelled() {
    return cancelled;
  }

  /** Background image decoding logic */
  private void doInBackground() {
    try {
      if (cancelled) return;

      CropImageView cropImageView = cropImageViewRef.get();
      int loadedWidth = (cropImageView != null) ? cropImageView.getLoadedImageWidth() : 0;
      int loadedHeight = (cropImageView != null) ? cropImageView.getLoadedImageHeight() : 0;

      BitmapUtils.BitmapSampled bitmapSampled =
          BitmapUtils.decodeSampledBitmap(appContext, imageUri, loadedWidth, loadedHeight);

      if (cancelled) return;

      postResult(new Result(bitmapSampled.bitmap(), bitmapSampled.sampleSize()));

    } catch (Exception e) {
      postResult(new Result(e, 1));
    }
  }

  /** Post result back to main thread safely */
  private void postResult(Result result) {
    MAIN_HANDLER.post(() -> {
      if (!cancelled) {
        CropImageView cropImageView = cropImageViewRef.get();
        if (cropImageView != null) {
          cropImageView.onSetImageUriAsyncComplete(result);
        }
      }
    });
  }

  // region: Inner class: Result

  /** The result of BitmapLoadingWorkerTask async operation. */
  final class Result {

    /** The loaded bitmap */
    public final Bitmap bitmap;

    /** The error that occurred during async bitmap loading. */
    public final Exception error;

    /** The sample size used for loading */
    public final int sampleSize;

    /** The loaded image Uri */
    public final Uri uri;

    /** The degrees the image was rotated (usually 0 for loading) */
    public final int degreesRotated;

    /** The sample size used to load the image (for compatibility) */
    public final int loadSampleSize;

    Result(Bitmap bitmap, int sampleSize) {
      this.bitmap = bitmap;
      this.error = null;
      this.sampleSize = sampleSize;
      this.uri = imageUri;
      this.degreesRotated = 0;
      this.loadSampleSize = sampleSize;
    }

    Result(Exception error, int sampleSize) {
      this.bitmap = null;
      this.error = error;
      this.sampleSize = sampleSize;
      this.uri = imageUri;
      this.degreesRotated = 0;
      this.loadSampleSize = sampleSize;
    }
  }

  // endregion
}
