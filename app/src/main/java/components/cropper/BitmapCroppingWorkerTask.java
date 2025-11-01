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

/** Task to crop bitmap asynchronously without blocking UI thread. */
final class BitmapCroppingWorkerTask {

  /** Shared executor for background cropping tasks */
  private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
      Math.max(2, Runtime.getRuntime().availableProcessors() / 2)
  );

  /** Handler for posting back to main thread */
  private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

  /** Reference to the CropImageView (prevents leaks) */
  private final WeakReference<CropImageView> cropImageViewRef;

  private final Context context;
  private final Bitmap bitmap;
  private final Uri uri;
  private final float[] cropPoints;
  private final int degreesRotated;
  private final boolean fixAspectRatio;
  private final int aspectRatioX;
  private final int aspectRatioY;
  private final int reqWidth;
  private final int reqHeight;
  private final boolean flipHorizontally;
  private final boolean flipVertically;
  private final CropImageView.RequestSizeOptions reqSizeOptions;
  private final Uri saveUri;
  private final Bitmap.CompressFormat saveCompressFormat;
  private final int saveCompressQuality;
  private final int orgWidth;
  private final int orgHeight;

  /** Future to allow proper cancellation */
  private Future<?> runningTask;
  private volatile boolean cancelled = false;

  // region Constructors

  BitmapCroppingWorkerTask(
      CropImageView cropImageView,
      Bitmap bitmap,
      float[] cropPoints,
      int degreesRotated,
      boolean fixAspectRatio,
      int aspectRatioX,
      int aspectRatioY,
      int reqWidth,
      int reqHeight,
      boolean flipHorizontally,
      boolean flipVertically,
      CropImageView.RequestSizeOptions options,
      Uri saveUri,
      Bitmap.CompressFormat saveCompressFormat,
      int saveCompressQuality) {

    this.cropImageViewRef = new WeakReference<>(cropImageView);
    this.context = cropImageView.getContext().getApplicationContext();
    this.bitmap = bitmap;
    this.uri = null;
    this.cropPoints = cropPoints;
    this.degreesRotated = degreesRotated;
    this.fixAspectRatio = fixAspectRatio;
    this.aspectRatioX = aspectRatioX;
    this.aspectRatioY = aspectRatioY;
    this.reqWidth = reqWidth;
    this.reqHeight = reqHeight;
    this.flipHorizontally = flipHorizontally;
    this.flipVertically = flipVertically;
    this.reqSizeOptions = options;
    this.saveUri = saveUri;
    this.saveCompressFormat = saveCompressFormat;
    this.saveCompressQuality = saveCompressQuality;
    this.orgWidth = 0;
    this.orgHeight = 0;
  }

  BitmapCroppingWorkerTask(
      CropImageView cropImageView,
      Uri uri,
      float[] cropPoints,
      int degreesRotated,
      int orgWidth,
      int orgHeight,
      boolean fixAspectRatio,
      int aspectRatioX,
      int aspectRatioY,
      int reqWidth,
      int reqHeight,
      boolean flipHorizontally,
      boolean flipVertically,
      CropImageView.RequestSizeOptions options,
      Uri saveUri,
      Bitmap.CompressFormat saveCompressFormat,
      int saveCompressQuality) {

    this.cropImageViewRef = new WeakReference<>(cropImageView);
    this.context = cropImageView.getContext().getApplicationContext();
    this.bitmap = null;
    this.uri = uri;
    this.cropPoints = cropPoints;
    this.degreesRotated = degreesRotated;
    this.fixAspectRatio = fixAspectRatio;
    this.aspectRatioX = aspectRatioX;
    this.aspectRatioY = aspectRatioY;
    this.reqWidth = reqWidth;
    this.reqHeight = reqHeight;
    this.flipHorizontally = flipHorizontally;
    this.flipVertically = flipVertically;
    this.reqSizeOptions = options;
    this.saveUri = saveUri;
    this.saveCompressFormat = saveCompressFormat;
    this.saveCompressQuality = saveCompressQuality;
    this.orgWidth = orgWidth;
    this.orgHeight = orgHeight;
  }

  // endregion

  public Uri getUri() {
    return uri;
  }

  /** Execute cropping asynchronously */
  public void execute() {
    runningTask = EXECUTOR.submit(this::doInBackground);
  }

  /** Cancel the current task */
  public void cancel() {
    cancelled = true;
    if (runningTask != null) runningTask.cancel(true);
  }

  public boolean isCancelled() {
    return cancelled;
  }

  /** Background cropping logic */
  private void doInBackground() {
    try {
      if (cancelled) return;

      BitmapUtils.BitmapSampled resultSample;
      if (uri != null) {
        resultSample = BitmapUtils.cropBitmap(
            context, uri, cropPoints, degreesRotated, orgWidth, orgHeight,
            fixAspectRatio, aspectRatioX, aspectRatioY, reqWidth, reqHeight,
            flipHorizontally, flipVertically
        );
      } else if (bitmap != null) {
        resultSample = BitmapUtils.cropBitmapObjectHandleOOM(
            bitmap, cropPoints, degreesRotated, fixAspectRatio,
            aspectRatioX, aspectRatioY, flipHorizontally, flipVertically
        );
      } else {
        postResult(new Result(new IllegalArgumentException("No bitmap or URI to crop"), true));
        return;
      }

      if (cancelled) return;

      if (resultSample.bitmap() == null) {
        postResult(new Result(new IllegalStateException("Cropping failed"), true));
        return;
      }

      if (saveUri == null) {
        postResult(new Result(resultSample.bitmap(), resultSample.sampleSize()));
      } else {
        Uri saveResult = BitmapUtils.writeTempStateStoreBitmap(context, resultSample.bitmap(), saveUri);
        postResult(new Result(saveResult, resultSample.sampleSize()));
      }

    } catch (Exception e) {
      postResult(new Result(e, true));
    }
  }

  private void postResult(Result result) {
    MAIN_HANDLER.post(() -> {
      if (!cancelled) {
        CropImageView view = cropImageViewRef.get();
        if (view != null) view.onImageCroppingAsyncComplete(result);
      }
    });
  }

  /** Result model */
  final class Result {
    public final Bitmap bitmap;
    public final Uri uri;
    public final Exception error;
    public final boolean isSave;
    public final int sampleSize;
    public final int degreesRotated;

    Result(Bitmap bitmap, int sampleSize) {
      this.bitmap = bitmap;
      this.uri = null;
      this.error = null;
      this.isSave = false;
      this.sampleSize = sampleSize;
      this.degreesRotated = BitmapCroppingWorkerTask.this.degreesRotated;
    }

    Result(Uri uri, int sampleSize) {
      this.bitmap = null;
      this.uri = uri;
      this.error = null;
      this.isSave = true;
      this.sampleSize = sampleSize;
      this.degreesRotated = BitmapCroppingWorkerTask.this.degreesRotated;
    }

    Result(Exception error, boolean isSave) {
      this.bitmap = null;
      this.uri = null;
      this.error = error;
      this.isSave = isSave;
      this.sampleSize = 1;
      this.degreesRotated = BitmapCroppingWorkerTask.this.degreesRotated;
    }
  }
}
