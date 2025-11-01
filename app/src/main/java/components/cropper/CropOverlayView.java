package components.cropper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * CropOverlayView - optimized and hardened.
 *
 * Responsibilities:
 *  - draw translucent background outside crop area
 *  - draw crop window border, corners and guidelines
 *  - handle touch interactions for moving/resizing crop window
 *
 * Notes:
 *  - Designed for standard View rendering (ImageView + overlay).
 *  - No allocations in onDraw; use cached Paint/Path objects.
 */
public class CropOverlayView extends View {

  private static final String TAG = "CropOverlayView";

  // Gesture detector for two-finger scaling
  private ScaleGestureDetector mScaleDetector;

  // Multi touch toggle
  private boolean mMultiTouchEnabled = false;

  // Crop window handler - manages rect and movement logic
  private final CropWindowHandler mCropWindowHandler = new CropWindowHandler();

  // Listener to notify callers about crop window changes
  private @Nullable CropWindowChangeListener mCropWindowChangeListener;

  // Cached drawing Rect / Path / arrays to avoid allocations
  private final RectF mDrawRect = new RectF();
  private final Path mPath = new Path();
  private final float[] mBoundsPoints = new float[8];
  private final RectF mCalcBounds = new RectF();

  // View size (image container size)
  private int mViewWidth;
  private int mViewHeight;

  // Paint objects (initialized by options)
  private @Nullable Paint mBorderPaint;
  private @Nullable Paint mBorderCornerPaint;
  private @Nullable Paint mGuidelinePaint;
  private @Nullable Paint mBackgroundPaint;

  // Corner & touch / config parameters
  private float mBorderCornerOffset;
  private float mBorderCornerLength;
  private float mInitialCropWindowPaddingRatio;
  private float mTouchRadius;
  private float mSnapRadius;

  // Move handler created on ACTION_DOWN
  private CropWindowMoveHandler mMoveHandler;

  // Aspect / ratio flags
  private boolean mFixAspectRatio;
  private int mAspectRatioX = 1;
  private int mAspectRatioY = 1;
  private float mTargetAspectRatio = 1f;

  // Options
  private CropImageView.Guidelines mGuidelines = CropImageView.Guidelines.ON_TOUCH;
  private CropImageView.CropShape mCropShape = CropImageView.CropShape.RECTANGLE;

  // Optional initial rectangle provided by options
  private final Rect mInitialCropWindowRect = new Rect();

  // Initialized flag
  private boolean initializedCropWindow = false;

  // Keep original layer type when switching to software for special cases (older devices)
  private Integer mOriginalLayerType = null;

  // Simple constructor chain
  public CropOverlayView(Context context) {
    this(context, null);
  }

  public CropOverlayView(Context context, AttributeSet attrs) {
    super(context, attrs);
    // default: keep hardware layer enabled (recommended). Will fallback to software only when needed.
    setLayerType(LAYER_TYPE_HARDWARE, null);
  }

  /** Listener registration */
  public void setCropWindowChangeListener(@Nullable CropWindowChangeListener listener) {
    mCropWindowChangeListener = listener;
  }

  /** Get the current crop rectangle (in view coordinates). */
  @NonNull
  public RectF getCropWindowRect() {
    return mCropWindowHandler.getRectCopy();
  }

  /** Set crop rectangle (in view coordinates). */
  public void setCropWindowRect(@NonNull RectF rect) {
    mCropWindowHandler.setRect(rect);
  }

  /** Fix current crop window rect by rules (min/max/aspect/bounds). */
  public void fixCurrentCropWindowRect() {
    RectF rect = getCropWindowRect();
    fixCropWindowRectByRules(rect);
    mCropWindowHandler.setRect(rect);
  }

  /**
   * Set the initial crop window size and position. This is dependent on the size and position of
   * the image being cropped.
   */
  private void initCropWindow() {
    float leftLimit = Math.max(BitmapUtils.getRectLeft(mBoundsPoints), 0);
    float topLimit = Math.max(BitmapUtils.getRectTop(mBoundsPoints), 0);
    float rightLimit = Math.min(BitmapUtils.getRectRight(mBoundsPoints), getWidth());
    float bottomLimit = Math.min(BitmapUtils.getRectBottom(mBoundsPoints), getHeight());

    if (rightLimit <= leftLimit || bottomLimit <= topLimit) {
      return;
    }

    RectF rect = new RectF();

    // Tells the attribute functions the crop window has already been initialized
    initializedCropWindow = true;

    float horizontalPadding = mInitialCropWindowPaddingRatio * (rightLimit - leftLimit);
    float verticalPadding = mInitialCropWindowPaddingRatio * (bottomLimit - topLimit);

    if (mInitialCropWindowRect.width() > 0 && mInitialCropWindowRect.height() > 0) {
      // Get crop window position relative to the displayed image.
      rect.left = leftLimit + mInitialCropWindowRect.left / mCropWindowHandler.getScaleFactorWidth();
      rect.top = topLimit + mInitialCropWindowRect.top / mCropWindowHandler.getScaleFactorHeight();
      rect.right = rect.left + mInitialCropWindowRect.width() / mCropWindowHandler.getScaleFactorWidth();
      rect.bottom = rect.top + mInitialCropWindowRect.height() / mCropWindowHandler.getScaleFactorHeight();

      // Correct for floating point errors. Crop rect boundaries should not exceed the source Bitmap bounds.
      rect.left = Math.max(leftLimit, rect.left);
      rect.top = Math.max(topLimit, rect.top);
      rect.right = Math.min(rightLimit, rect.right);
      rect.bottom = Math.min(bottomLimit, rect.bottom);

    } else if (mFixAspectRatio && rightLimit > leftLimit && bottomLimit > topLimit) {

      // If the image aspect ratio is wider than the crop aspect ratio,
      // then the image height is the determining initial length. Else, vice-versa.
      float bitmapAspectRatio = (rightLimit - leftLimit) / (bottomLimit - topLimit);
      if (bitmapAspectRatio > mTargetAspectRatio) {

        rect.top = topLimit + verticalPadding;
        rect.bottom = bottomLimit - verticalPadding;

        float centerX = getWidth() / 2f;

        // dirty fix for wrong crop overlay aspect ratio when using fixed aspect ratio
        mTargetAspectRatio = (float) mAspectRatioX / mAspectRatioY;

        // Limits the aspect ratio to no less than 40 wide or 40 tall
        float cropWidth = Math.max(mCropWindowHandler.getMinCropWidth(), rect.height() * mTargetAspectRatio);

        float halfCropWidth = cropWidth / 2f;
        rect.left = centerX - halfCropWidth;
        rect.right = centerX + halfCropWidth;

      } else {

        rect.left = leftLimit + horizontalPadding;
        rect.right = rightLimit - horizontalPadding;

        float centerY = getHeight() / 2f;

        // Limits the aspect ratio to no less than 40 wide or 40 tall
        float cropHeight = Math.max(mCropWindowHandler.getMinCropHeight(), rect.width() / mTargetAspectRatio);

        float halfCropHeight = cropHeight / 2f;
        rect.top = centerY - halfCropHeight;
        rect.bottom = centerY + halfCropHeight;
      }
    } else {
      // Initialize crop window to have 10% padding w/ respect to image.
      rect.left = leftLimit + horizontalPadding;
      rect.top = topLimit + verticalPadding;
      rect.right = rightLimit - horizontalPadding;
      rect.bottom = bottomLimit - verticalPadding;
    }

    fixCropWindowRectByRules(rect);

    mCropWindowHandler.setRect(rect);
  }

  /** Fix the given rect to fit into bitmap rect and follow min, max and aspect ratio rules. */
  private void fixCropWindowRectByRules(RectF rect) {
    if (rect.width() < mCropWindowHandler.getMinCropWidth()) {
      float adj = (mCropWindowHandler.getMinCropWidth() - rect.width()) / 2;
      rect.left -= adj;
      rect.right += adj;
    }
    if (rect.height() < mCropWindowHandler.getMinCropHeight()) {
      float adj = (mCropWindowHandler.getMinCropHeight() - rect.height()) / 2;
      rect.top -= adj;
      rect.bottom += adj;
    }
    if (rect.width() > mCropWindowHandler.getMaxCropWidth()) {
      float adj = (rect.width() - mCropWindowHandler.getMaxCropWidth()) / 2;
      rect.left += adj;
      rect.right -= adj;
    }
    if (rect.height() > mCropWindowHandler.getMaxCropHeight()) {
      float adj = (rect.height() - mCropWindowHandler.getMaxCropHeight()) / 2;
      rect.top += adj;
      rect.bottom -= adj;
    }

    calculateBounds(rect);
    if (mCalcBounds.width() > 0 && mCalcBounds.height() > 0) {
      float leftLimit = Math.max(mCalcBounds.left, 0);
      float topLimit = Math.max(mCalcBounds.top, 0);
      float rightLimit = Math.min(mCalcBounds.right, getWidth());
      float bottomLimit = Math.min(mCalcBounds.bottom, getHeight());
      if (rect.left < leftLimit) {
        rect.left = leftLimit;
      }
      if (rect.top < topLimit) {
        rect.top = topLimit;
      }
      if (rect.right > rightLimit) {
        rect.right = rightLimit;
      }
      if (rect.bottom > bottomLimit) {
        rect.bottom = bottomLimit;
      }
    }
    if (mFixAspectRatio && Math.abs(rect.width() - rect.height() * mTargetAspectRatio) > 0.1) {
      if (rect.width() > rect.height() * mTargetAspectRatio) {
        float adj = Math.abs(rect.height() * mTargetAspectRatio - rect.width()) / 2;
        rect.left += adj;
        rect.right -= adj;
      } else {
        float adj = Math.abs(rect.width() / mTargetAspectRatio - rect.height()) / 2;
        rect.top += adj;
        rect.bottom -= adj;
      }
    }
  }

  /**
   * Inform the overlay about the image bounds relative to the view. This is required for correct
   * crop window initialization and constraints.
   *
   * @param boundsPoints 8 floats representing image bounding polygon (x0,y0...x3,y3)
   * @param viewWidth view width that contains the image
   * @param viewHeight view height that contains the image
   */
  public void setBounds(@Nullable float[] boundsPoints, int viewWidth, int viewHeight) {
    boolean changed = false;
    if (boundsPoints == null) {
      if (!Arrays.equals(mBoundsPoints, new float[8])) { // rare - only allocate for equality check
        Arrays.fill(mBoundsPoints, 0f);
        changed = true;
      }
    } else if (!Arrays.equals(mBoundsPoints, boundsPoints)) {
      System.arraycopy(boundsPoints, 0, mBoundsPoints, 0, Math.min(boundsPoints.length, mBoundsPoints.length));
      changed = true;
    }

    if (mViewWidth != viewWidth || mViewHeight != viewHeight) {
      mViewWidth = viewWidth;
      mViewHeight = viewHeight;
      changed = true;
    }

    if (changed) {
      RectF cropRect = mCropWindowHandler.getRect();
      if (cropRect.width() == 0f || cropRect.height() == 0f) {
        initCropWindow();
      } else {
        // ensure valid bounds after the change
        fixCropWindowRectByRules(cropRect);
        mCropWindowHandler.setRect(cropRect);
      }
      invalidate();
    }
  }

  /** Reset overlay to an uninitialized state (and re-init on next bounds set). */
  public void resetCropOverlayView() {
    if (initializedCropWindow) {
      setCropWindowRect(BitmapUtils.EMPTY_RECT_F);
      initializedCropWindow = false;
      initCropWindow();
      invalidate();
    }
  }

  public CropImageView.CropShape getCropShape() {
    return mCropShape;
  }

  /**
   * Set crop shape (rectangle or oval). For older devices we may need to switch to software layer
   * to allow complex clipping.
   */
  public void setCropShape(@NonNull CropImageView.CropShape cropShape) {
    if (mCropShape != cropShape) {
      mCropShape = cropShape;
      // For older platforms, we may need software layer for complex path clipping (oval + rotate)
      if (mCropShape == CropImageView.CropShape.OVAL) {
        mOriginalLayerType = getLayerType();
        if (mOriginalLayerType != LAYER_TYPE_SOFTWARE) {
          setLayerType(LAYER_TYPE_SOFTWARE, null);
        } else {
          mOriginalLayerType = null;
        }
      } else if (mOriginalLayerType != null) {
        setLayerType(mOriginalLayerType, null);
        mOriginalLayerType = null;
      }
      invalidate();
    }
  }

  public CropImageView.Guidelines getGuidelines() {
    return mGuidelines;
  }

  public void setGuidelines(@NonNull CropImageView.Guidelines guidelines) {
    if (mGuidelines != guidelines) {
      mGuidelines = guidelines;
      if (initializedCropWindow) invalidate();
    }
  }

  public boolean isFixAspectRatio() {
    return mFixAspectRatio;
  }

  public void setFixedAspectRatio(boolean fixAspectRatio) {
    if (mFixAspectRatio != fixAspectRatio) {
      mFixAspectRatio = fixAspectRatio;
      if (initializedCropWindow) {
        initCropWindow();
        invalidate();
      }
    }
  }

  public int getAspectRatioX() {
    return mAspectRatioX;
  }

  public void setAspectRatioX(int aspectRatioX) {
    if (aspectRatioX <= 0) {
      throw new IllegalArgumentException("aspectRatioX must be > 0");
    }
    if (mAspectRatioX != aspectRatioX) {
      mAspectRatioX = aspectRatioX;
      updateTargetAspectRatio();
      if (initializedCropWindow) {
        initCropWindow();
        invalidate();
      }
    }
  }

  public int getAspectRatioY() {
    return mAspectRatioY;
  }

  public void setAspectRatioY(int aspectRatioY) {
    if (aspectRatioY <= 0) {
      throw new IllegalArgumentException("aspectRatioY must be > 0");
    }
    if (mAspectRatioY != aspectRatioY) {
      mAspectRatioY = aspectRatioY;
      updateTargetAspectRatio();
      if (initializedCropWindow) {
        initCropWindow();
        invalidate();
      }
    }
  }

  private void updateTargetAspectRatio() {
    mTargetAspectRatio = (mAspectRatioY != 0) ? ((float) mAspectRatioX) / mAspectRatioY : 1f;
  }

  public void setSnapRadius(float snapRadius) {
    mSnapRadius = snapRadius;
  }

  /** Enable/disable multi-touch scaling */
  public boolean setMultiTouchEnabled(boolean enabled) {
    if (mMultiTouchEnabled != enabled) {
      mMultiTouchEnabled = enabled;
      if (mMultiTouchEnabled && mScaleDetector == null) {
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
      }
      return true;
    }
    return false;
  }

  public void setMinCropResultSize(int minCropResultWidth, int minCropResultHeight) {
    mCropWindowHandler.setMinCropResultSize(minCropResultWidth, minCropResultHeight);
  }

  public void setMaxCropResultSize(int maxCropResultWidth, int maxCropResultHeight) {
    mCropWindowHandler.setMaxCropResultSize(maxCropResultWidth, maxCropResultHeight);
  }

  public void setCropWindowLimits(float maxWidth, float maxHeight, float scaleFactorWidth, float scaleFactorHeight) {
    mCropWindowHandler.setCropWindowLimits(maxWidth, maxHeight, scaleFactorWidth, scaleFactorHeight);
  }

  public Rect getInitialCropWindowRect() {
    return mInitialCropWindowRect;
  }

  public void setInitialCropWindowRect(@Nullable Rect rect) {
    mInitialCropWindowRect.set(Objects.requireNonNullElse(rect, BitmapUtils.EMPTY_RECT));
    if (initializedCropWindow) {
      initCropWindow();
      invalidate();
      callOnCropWindowChanged(false);
    }
  }

  public void resetCropWindowRect() {
    if (initializedCropWindow) {
      initCropWindow();
      invalidate();
      callOnCropWindowChanged(false);
    }
  }

  /**
   * Apply option values (paints, sizes etc). Called once when options are set.
   * This method creates and caches Paint objects.
   */
  public void setInitialAttributeValues(@NonNull CropImageOptions options) {
    mCropWindowHandler.setInitialAttributeValues(options);

    setCropShape(options.cropShape);
    setSnapRadius(options.snapRadius);
    setGuidelines(options.guidelines);
    setFixedAspectRatio(options.isFixAspectRatio);
    setAspectRatioX(options.aspectRatioX);
    setAspectRatioY(options.aspectRatioY);
    setMultiTouchEnabled(options.isMultiTouchEnabled);

    mTouchRadius = options.touchRadius;
    mInitialCropWindowPaddingRatio = options.initialCropWindowPaddingRatio;

    mBorderPaint = getNewPaintOrNull(options.borderLineThickness, options.borderLineColor);
    mBorderCornerOffset = options.borderCornerOffset;
    mBorderCornerLength = options.borderCornerLength;
    mBorderCornerPaint = getNewPaintOrNull(options.borderCornerThickness, options.borderCornerColor);
    mGuidelinePaint = getNewPaintOrNull(options.guidelinesThickness, options.guidelinesColor);
    mBackgroundPaint = getNewPaint(options.backgroundColor);

    // update derived values
    updateTargetAspectRatio();
  }

  // ----- Drawing -----
  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    super.onDraw(canvas);

    // If paints are not initialized yet, skip drawing
    if (mBackgroundPaint == null) return;

    // Draw background shadow outside crop area
    drawBackground(canvas);

    // Draw guidelines if required
    if (mCropWindowHandler.showGuidelines()) {
      if (mGuidelines == CropImageView.Guidelines.ON || (mGuidelines == CropImageView.Guidelines.ON_TOUCH && mMoveHandler != null)) {
        drawGuidelines(canvas);
      }
    }

    drawBorders(canvas);
    drawCorners(canvas);
  }

  /** Draw the translucent background outside the crop rectangle or oval. */
  private void drawBackground(@NonNull Canvas canvas) {
    RectF rect = mCropWindowHandler.getRect();
    // Prevent drawing if rect is invalid
    if (rect.width() <= 0 || rect.height() <= 0) return;

    float left = Math.max(BitmapUtils.getRectLeft(mBoundsPoints), 0f);
    float top = Math.max(BitmapUtils.getRectTop(mBoundsPoints), 0f);
    float right = Math.min(BitmapUtils.getRectRight(mBoundsPoints), (float) getWidth());
    float bottom = Math.min(BitmapUtils.getRectBottom(mBoundsPoints), (float) getHeight());

    // If crop shape is rectangle and not rotated non-straight, draw 4 rects (fast)
    if (mCropShape == CropImageView.CropShape.RECTANGLE && !isNonStraightAngleRotated()) {
      // top
      if (mBackgroundPaint != null) canvas.drawRect(left, top, right, rect.top, mBackgroundPaint);
      // bottom
      if (mBackgroundPaint != null) canvas.drawRect(left, rect.bottom, right, bottom, mBackgroundPaint);
      // left
      if (mBackgroundPaint != null) canvas.drawRect(left, rect.top, rect.left, rect.bottom, mBackgroundPaint);
      // right
      if (mBackgroundPaint != null) canvas.drawRect(rect.right, rect.top, right, rect.bottom, mBackgroundPaint);
      return;
    }

    // Otherwise use path-based clipping to support rotated/oval crop shapes
    mPath.reset();

    if (mCropShape == CropImageView.CropShape.RECTANGLE) {
      // Build polygon path from bounding points
      mPath.moveTo(mBoundsPoints[0], mBoundsPoints[1]);
      mPath.lineTo(mBoundsPoints[2], mBoundsPoints[3]);
      mPath.lineTo(mBoundsPoints[4], mBoundsPoints[5]);
      mPath.lineTo(mBoundsPoints[6], mBoundsPoints[7]);
      mPath.close();

      canvas.save();
      // Clip out the image polygon and then XOR with crop rect to paint background outside crop area
      canvas.clipOutPath(mPath);
      // XOR the crop rect region (so crop area becomes excluded)
      @SuppressWarnings("deprecation")
      boolean xor = canvas.clipRect(rect, Region.Op.XOR);
      if (!xor) {
        // if XOR unsupported, draw full background and rely on overlay border/corners to indicate crop
        if (mBackgroundPaint != null) canvas.drawRect(left, top, right, bottom, mBackgroundPaint);
      } else {
        if (mBackgroundPaint != null) canvas.drawRect(left, top, right, bottom, mBackgroundPaint);
      }
      canvas.restore();
    } else { // OVAL crop shape
      mDrawRect.set(rect.left, rect.top, rect.right, rect.bottom);
      mPath.addOval(mDrawRect, Path.Direction.CW);

      canvas.save();
      canvas.clipOutPath(mPath);
      if (mBackgroundPaint != null) canvas.drawRect(left, top, right, bottom, mBackgroundPaint);
      canvas.restore();
    }
  }

  /** Draws 2 vertical and 2 horizontal guideline lines inside the crop area. */
  private void drawGuidelines(@NonNull Canvas canvas) {
    if (mGuidelinePaint == null || mBorderPaint == null) return;

      RectF rect = mCropWindowHandler.getRect();
    if (rect.width() <= 0 || rect.height() <= 0) return;

    float stroke = mBorderPaint.getStrokeWidth();
    // Use a copy so we don't mutate handler rect unexpectedly
    RectF inner = new RectF(rect.left + stroke, rect.top + stroke, rect.right - stroke, rect.bottom - stroke);

    float oneThirdW = inner.width() / 3f;
    float oneThirdH = inner.height() / 3f;

    if (mCropShape == CropImageView.CropShape.OVAL) {
      // Draw approximated oval guidelines (math preserves ellipse shape)
      float w = inner.width() / 2f - stroke;
      float h = inner.height() / 2f - stroke;
      // vertical guideline positions
      float x1 = inner.left + oneThirdW;
      float x2 = inner.right - oneThirdW;
      float yv = (float) (h * Math.sin(Math.acos((w - oneThirdW) / Math.max(w, 1f))));
      canvas.drawLine(x1, inner.top + h - yv, x1, inner.bottom - h + yv, mGuidelinePaint);
      canvas.drawLine(x2, inner.top + h - yv, x2, inner.bottom - h + yv, mGuidelinePaint);
      // horizontal guideline positions
      float y1 = inner.top + oneThirdH;
      float y2 = inner.bottom - oneThirdH;
      float xv = (float) (w * Math.cos(Math.asin((h - oneThirdH) / Math.max(h, 1f))));
      canvas.drawLine(inner.left + w - xv, y1, inner.right - w + xv, y1, mGuidelinePaint);
      canvas.drawLine(inner.left + w - xv, y2, inner.right - w + xv, y2, mGuidelinePaint);
      } else {
      float x1 = inner.left + oneThirdW;
      float x2 = inner.right - oneThirdW;
      canvas.drawLine(x1, inner.top, x1, inner.bottom, mGuidelinePaint);
      canvas.drawLine(x2, inner.top, x2, inner.bottom, mGuidelinePaint);

      float y1 = inner.top + oneThirdH;
      float y2 = inner.bottom - oneThirdH;
      canvas.drawLine(inner.left, y1, inner.right, y1, mGuidelinePaint);
      canvas.drawLine(inner.left, y2, inner.right, y2, mGuidelinePaint);
    }
  }

  /** Draws a border (rectangle or oval) around the crop window. */
  private void drawBorders(@NonNull Canvas canvas) {
    if (mBorderPaint == null) return;
      RectF rect = mCropWindowHandler.getRect();
    if (rect.width() <= 0 || rect.height() <= 0) return;

    float stroke = mBorderPaint.getStrokeWidth();
    RectF r = new RectF(rect.left + stroke / 2f, rect.top + stroke / 2f, rect.right - stroke / 2f, rect.bottom - stroke / 2f);

    if (mCropShape == CropImageView.CropShape.RECTANGLE) {
      canvas.drawRect(r, mBorderPaint);
    } else {
      canvas.drawOval(r, mBorderPaint);
    }
  }

  /** Draws corner decorations for the crop window (nice rounded corners). */
  private void drawCorners(@NonNull Canvas canvas) {
    if (mBorderCornerPaint == null || mBorderPaint == null) return;

    float borderStroke = mBorderPaint.getStrokeWidth();
    float cornerStroke = mBorderCornerPaint.getStrokeWidth();
    float inset = (cornerStroke / 2f) + (mCropShape == CropImageView.CropShape.RECTANGLE ? mBorderCornerOffset : 0f);

      RectF rect = mCropWindowHandler.getRect();
    if (rect.width() <= 0 || rect.height() <= 0) return;

    RectF cornersRect = new RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset);

    float cornerOffset = (cornerStroke - borderStroke) / 2f;
    float cornerExtension = cornerStroke / 2f + cornerOffset;

    // Top-left
    canvas.drawLine(cornersRect.left - cornerOffset, cornersRect.top - cornerExtension, cornersRect.left - cornerOffset, cornersRect.top + mBorderCornerLength, mBorderCornerPaint);
    canvas.drawLine(cornersRect.left - cornerExtension, cornersRect.top - cornerOffset, cornersRect.left + mBorderCornerLength, cornersRect.top - cornerOffset, mBorderCornerPaint);

    // Top-right
    canvas.drawLine(cornersRect.right + cornerOffset, cornersRect.top - cornerExtension, cornersRect.right + cornerOffset, cornersRect.top + mBorderCornerLength, mBorderCornerPaint);
    canvas.drawLine(cornersRect.right + cornerExtension, cornersRect.top - cornerOffset, cornersRect.right - mBorderCornerLength, cornersRect.top - cornerOffset, mBorderCornerPaint);

    // Bottom-left
    canvas.drawLine(cornersRect.left - cornerOffset, cornersRect.bottom + cornerExtension, cornersRect.left - cornerOffset, cornersRect.bottom - mBorderCornerLength, mBorderCornerPaint);
    canvas.drawLine(cornersRect.left - cornerExtension, cornersRect.bottom + cornerOffset, cornersRect.left + mBorderCornerLength, cornersRect.bottom + cornerOffset, mBorderCornerPaint);

    // Bottom-right
    canvas.drawLine(cornersRect.right + cornerOffset, cornersRect.bottom + cornerExtension, cornersRect.right + cornerOffset, cornersRect.bottom - mBorderCornerLength, mBorderCornerPaint);
    canvas.drawLine(cornersRect.right + cornerExtension, cornersRect.bottom + cornerOffset, cornersRect.right - mBorderCornerLength, cornersRect.bottom + cornerOffset, mBorderCornerPaint);
  }

  // ----- Helpers to create Paints -----
  private static Paint getNewPaint(int color) {
    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    p.setColor(color);
    p.setAntiAlias(true);
    p.setFilterBitmap(true);
    p.setDither(true);
    return p;
  }

  private static Paint getNewPaintOrNull(float thickness, int color) {
    if (thickness <= 0f) return null;
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(color);
    paint.setStrokeWidth(thickness);
    paint.setStyle(Paint.Style.STROKE);
    paint.setAntiAlias(true);
    paint.setFilterBitmap(true);
    paint.setDither(true);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeJoin(Paint.Join.ROUND);
    return paint;
  }

  // ----- Touch handling -----
  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event) {
    if (!isEnabled()) return false;

    final int action = event.getActionMasked();

    // Multi-touch scaling
    if (mMultiTouchEnabled && mScaleDetector != null) {
      try {
        mScaleDetector.onTouchEvent(event);
      } catch (Exception e) {
        Log.w(TAG, "Scale detector exception", e);
      }
      }

    switch (action) {
        case MotionEvent.ACTION_DOWN:
        // Prevent parent intercept for move gestures
        if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
          onActionDown(event.getX(), event.getY());
          return true;

      case MotionEvent.ACTION_POINTER_DOWN:
        // just let the detector handle it
          return true;

        case MotionEvent.ACTION_MOVE:
        // keep parent from intercepting
        if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
          onActionMove(event.getX(), event.getY());
          return true;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
        onActionUp();
        return true;

        default:
      return false;
    }
  }

  private void onActionDown(float x, float y) {
    mMoveHandler = mCropWindowHandler.getMoveHandler(x, y, mTouchRadius, mCropShape);
    if (mMoveHandler != null) {
      invalidate();
    }
  }

  private void onActionUp() {
    if (mMoveHandler != null) {
      mMoveHandler = null;
      callOnCropWindowChanged(false);
      invalidate();
    }
  }

  private void onActionMove(float x, float y) {
    if (mMoveHandler == null) return;

    float snapRadius = mSnapRadius;
    RectF rect = mCropWindowHandler.getRect();

    if (calculateBounds(rect)) {
      // If bounds calculation indicates non-straight rotation, don't snap to image edges
      snapRadius = 0f;
    }

    mMoveHandler.move(rect, x, y, mCalcBounds, mViewWidth, mViewHeight, snapRadius, mFixAspectRatio, mTargetAspectRatio);
    mCropWindowHandler.setRect(rect);
    callOnCropWindowChanged(true);
    invalidate();
  }

  /**
   * Calculate bounds for current crop rect based on image polygon. When the image is rotated to a
   * non-straight angle this will compute a conservative bounding rectangle and return true.
   */
  private boolean calculateBounds(@NonNull RectF rect) {
    float left = BitmapUtils.getRectLeft(mBoundsPoints);
    float top = BitmapUtils.getRectTop(mBoundsPoints);
    float right = BitmapUtils.getRectRight(mBoundsPoints);
    float bottom = BitmapUtils.getRectBottom(mBoundsPoints);

    if (!isNonStraightAngleRotated()) {
      mCalcBounds.set(left, top, right, bottom);
      return false;
    }

    // Fallback safety: ensure denominators not zero and polygon well-formed
    try {
      float x0 = mBoundsPoints[0], y0 = mBoundsPoints[1];
      float x2 = mBoundsPoints[4], y2 = mBoundsPoints[5];
      float x3 = mBoundsPoints[6], y3 = mBoundsPoints[7];

      // rotate ordering fixes (kept original logic but guarded)
      if (mBoundsPoints[7] < mBoundsPoints[1]) {
        if (mBoundsPoints[1] < mBoundsPoints[3]) {
          x0 = mBoundsPoints[6];
          y0 = mBoundsPoints[7];
          x2 = mBoundsPoints[2];
          y2 = mBoundsPoints[3];
          x3 = mBoundsPoints[4];
          y3 = mBoundsPoints[5];
        } else {
          x0 = mBoundsPoints[4];
          y0 = mBoundsPoints[5];
          x2 = mBoundsPoints[0];
          y2 = mBoundsPoints[1];
          x3 = mBoundsPoints[2];
          y3 = mBoundsPoints[3];
        }
      } else if (mBoundsPoints[1] > mBoundsPoints[3]) {
        x0 = mBoundsPoints[2];
        y0 = mBoundsPoints[3];
        x2 = mBoundsPoints[6];
        y2 = mBoundsPoints[7];
        x3 = mBoundsPoints[0];
        y3 = mBoundsPoints[1];
      }

      // line slopes & intercepts
      float a0 = (y3 - y0) / Math.max((x3 - x0), 1e-6f);
      float a1 = -1f / a0;
      float b0 = y0 - a0 * x0;
      float b1 = y0 - a1 * x0;
      float b2 = y2 - a0 * x2;
      float b3 = y2 - a1 * x2;

      float c0 = (rect.centerY() - rect.top) / Math.max((rect.centerX() - rect.left), 1e-6f);
      float c1 = -c0;
      float d0 = rect.top - c0 * rect.left;
      float d1 = rect.top - c1 * rect.right;

      left = Math.max(left, (d0 - b0) / Math.max((a0 - c0), 1e-6f) < rect.right ? (d0 - b0) / (a0 - c0) : left);
      left = Math.max(left, (d0 - b1) / Math.max((a1 - c0), 1e-6f) < rect.right ? (d0 - b1) / (a1 - c0) : left);
      left = Math.max(left, (d1 - b3) / Math.max((a1 - c1), 1e-6f) < rect.right ? (d1 - b3) / (a1 - c1) : left);
      right = Math.min(right, (d1 - b1) / Math.max((a1 - c1), 1e-6f) > rect.left ? (d1 - b1) / (a1 - c1) : right);
      right = Math.min(right, (d1 - b2) / Math.max((a0 - c1), 1e-6f) > rect.left ? (d1 - b2) / (a0 - c1) : right);
      right = Math.min(right, (d0 - b2) / Math.max((a0 - c0), 1e-6f) > rect.left ? (d0 - b2) / (a0 - c0) : right);

      top = Math.max(top, Math.max(a0 * left + b0, a1 * right + b1));
      bottom = Math.min(bottom, Math.min(a1 * left + b3, a0 * right + b2));

      mCalcBounds.left = left;
      mCalcBounds.top = top;
      mCalcBounds.right = right;
      mCalcBounds.bottom = bottom;
      return true;
    } catch (Exception e) {
      Log.w(TAG, "calculateBounds failed, falling back to rect bounds", e);
      mCalcBounds.set(left, top, right, bottom);
      return false;
    }
  }

  /**
   * Check if the image is rotated to a non-straight angle (not 0°, 90°, 180°, 270°).
   * @return true if rotated to non-straight angle, false if straight angle
   */
  private boolean isNonStraightAngleRotated() {
    // If bounding polygon differs from axis-aligned rectangle -> rotated
    return mBoundsPoints[0] != mBoundsPoints[6] || mBoundsPoints[1] != mBoundsPoints[7];
  }

  private void callOnCropWindowChanged(boolean inProgress) {
    try {
      if (mCropWindowChangeListener != null) {
        mCropWindowChangeListener.onCropWindowChanged(inProgress);
      }
    } catch (Exception e) {
      Log.e(TAG, "Exception in crop window changed callback", e);
    }
  }

  // ----- Listener interfaces -----
  public interface CropWindowChangeListener {
    void onCropWindowChanged(boolean inProgress);
  }

  // ----- Scale listener (two-finger pinch) -----
  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
      RectF rect = mCropWindowHandler.getRect();
      if (rect == null) return false;

      float x = detector.getFocusX();
      float y = detector.getFocusY();
      float dY = detector.getCurrentSpanY() / 2f;
      float dX = detector.getCurrentSpanX() / 2f;

      float newTop = y - dY;
      float newLeft = x - dX;
      float newRight = x + dX;
      float newBottom = y + dY;

      // Respect limits:
      if (newLeft < newRight && newTop <= newBottom &&
          newLeft >= 0f && newRight <= mCropWindowHandler.getMaxCropWidth() &&
          newTop >= 0f && newBottom <= mCropWindowHandler.getMaxCropHeight()) {

        rect.set(newLeft, newTop, newRight, newBottom);
        mCropWindowHandler.setRect(rect);
        invalidate();
      }
      return true;
    }
  }
}
