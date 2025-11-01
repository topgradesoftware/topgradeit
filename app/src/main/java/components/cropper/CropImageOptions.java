// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth;
// inexhaustible as the great rivers.
// When they come to an end;
// they begin again;
// like the days and months;
// they die and are reborn;
// like the four seasons."
//
// - Sun Tzu, "The Art of War"

package components.cropper;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * All the possible options that can be set to customize crop image.
 * Initialized with default values and supports fluent configuration.
 */
public class CropImageOptions implements Parcelable {

    // region Constants

    private static final int MAX_CROP_LIMIT = 100000;
    private static final int DEFAULT_MAX_ZOOM = 4;
    private static final int DEFAULT_ROTATION_DEGREES = 90;
    private static final int DEFAULT_OUTPUT_QUALITY = 90;
    private static final float DEFAULT_PADDING_RATIO = 0.1f;

    // endregion

    public static final Creator<CropImageOptions> CREATOR =
            new Creator<>() {
                @Override
                public CropImageOptions createFromParcel(Parcel in) {
                    return new CropImageOptions(in);
                }

                @Override
                public CropImageOptions[] newArray(int size) {
                    return new CropImageOptions[size];
                }
            };

    // region Fields with Nullability Annotations

    /** The shape of the cropping window. */
    public @NonNull CropImageView.CropShape cropShape;

    /**
     * An edge of the crop window will snap to the corresponding edge of a specified bounding box when
     * the crop window edge is less than or equal to this distance (in pixels) away from the bounding
     * box edge. (in pixels)
     */
    public float snapRadius;

    /**
     * The radius of the touchable area around the handle. (in pixels)
     * We are basing this value off of the recommended 48dp Rhythm.
     * See: <a href="http://developer.android.com/design/style/metrics-grids.html#48dp-rhythm">48dp Rhythm</a>
     */
    public float touchRadius;

    /** whether the guidelines should be on, off, or only showing when resizing. */
    public @NonNull CropImageView.Guidelines guidelines;

    /** The initial scale type of the image in the crop image view */
    public @NonNull CropImageView.ScaleType scaleType;

    /**
     * if to show crop overlay UI what contains the crop window UI surrounded by background over the
     * cropping image.
     * default: true, may disable for animation or frame transition.
     */
    public boolean isShowCropOverlay;

    /**
     * if to show progress bar when image async loading/cropping is in progress.
     * default: true, disable to provide custom progress bar UI.
     */
    public boolean isShowProgressBar;

    /**
     * if auto-zoom functionality is enabled.
     * default: true.
     */
    public boolean isAutoZoomEnabled;

    /** if multi-touch should be enabled on the crop box default: false */
    public boolean isMultiTouchEnabled;

    /** The max zoom allowed during cropping. */
    public int maxZoom;

    /**
     * The initial crop window padding from image borders in percentage of the cropping image
     * dimensions.
     */
    public float initialCropWindowPaddingRatio;

    /** whether the width to height aspect ratio should be maintained or free to change. */
    public boolean isFixAspectRatio;

    /** the X value of the aspect ratio. */
    public int aspectRatioX;

    /** the Y value of the aspect ratio. */
    public int aspectRatioY;

    /** the thickness of the guidelines lines in pixels. (in pixels) */
    public float borderLineThickness;

    /** the color of the guidelines lines */
    public int borderLineColor;

    /** thickness of the corner line. (in pixels) */
    public float borderCornerThickness;

    /** the offset of corner line from crop window border. (in pixels) */
    public float borderCornerOffset;

    /** the length of the corner line away from the corner. (in pixels) */
    public float borderCornerLength;

    /** the color of the corner line */
    public int borderCornerColor;

    /** the thickness of the guidelines lines. (in pixels) */
    public float guidelinesThickness;

    /** the color of the guidelines lines */
    public int guidelinesColor;

    /**
     * the color of the overlay background around the crop window cover the image parts not in the
     * crop window.
     */
    public int backgroundColor;

    /** the min width the crop window is allowed to be. (in pixels) */
    public int minCropWindowWidth;

    /** the min height the crop window is allowed to be. (in pixels) */
    public int minCropWindowHeight;

    /**
     * the min width the resulting cropping image is allowed to be, affects the cropping window
     * limits. (in pixels)
     */
    public int minCropResultWidth;

    /**
     * the min height the resulting cropping image is allowed to be, affects the cropping window
     * limits. (in pixels)
     */
    public int minCropResultHeight;

    /**
     * the max width the resulting cropping image is allowed to be, affects the cropping window
     * limits. (in pixels)
     */
    public int maxCropResultWidth;

    /**
     * the max height the resulting cropping image is allowed to be, affects the cropping window
     * limits. (in pixels)
     */
    public int maxCropResultHeight;

    /** the title of the {@link CropImageActivity} */
    public @Nullable CharSequence activityTitle;

    /** the color to use for action bar items icons */
    public int activityMenuIconColor;

    /** the Android Uri to save the cropped image to */
    public @Nullable Uri outputUri;

    /** the compression format to use when writing the image */
    public @NonNull Bitmap.CompressFormat outputCompressFormat;

    /** the quality (if applicable) to use when writing the image (0 - 100) */
    public int outputCompressQuality;

    /** the width to resize the cropped image to (see options) */
    public int outputRequestWidth;

    /** the height to resize the cropped image to (see options) */
    public int outputRequestHeight;

    /** the resize method to use on the cropped bitmap (see options documentation) */
    public @NonNull CropImageView.RequestSizeOptions outputRequestSizeOptions;

    /** if the result of crop image activity should not save the cropped image bitmap */
    public boolean isNoOutputImage;

    /** the initial rectangle to set on the cropping image after loading */
    public @Nullable Rect initialCropWindowRectangle;

    /** the initial rotation to set on the cropping image after loading (0-360 degrees clockwise) */
    public int initialRotation;

    /** if to allow (all) rotation during cropping (activity) */
    public boolean isAllowRotation;

    /** if to allow (all) flipping during cropping (activity) */
    public boolean isAllowFlipping;

    /** if to allow counter-clockwise rotation during cropping (activity) */
    public boolean isAllowCounterRotation;

    /** the amount of degrees to rotate clockwise or counter-clockwise */
    public int rotationDegrees;

    /** whether the image should be flipped horizontally */
    public boolean isFlipHorizontally;

    /** whether the image should be flipped vertically */
    public boolean isFlipVertically;

    /** optional, the text of the crop menu crop button */
    public @Nullable CharSequence cropMenuCropButtonTitle;

    /** optional image resource to be used for crop menu crop icon instead of text */
    public int cropMenuCropButtonIcon;

    // endregion

    /** Init options with defaults. */
    public CropImageOptions() {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();

        cropShape = CropImageView.CropShape.RECTANGLE;
        snapRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, dm);
        touchRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, dm);
        guidelines = CropImageView.Guidelines.ON_TOUCH;
        scaleType = CropImageView.ScaleType.FIT_CENTER;
        isShowCropOverlay = true;
        isShowProgressBar = true;
        isAutoZoomEnabled = true;
        isMultiTouchEnabled = false;
        maxZoom = DEFAULT_MAX_ZOOM;
        initialCropWindowPaddingRatio = DEFAULT_PADDING_RATIO;

        isFixAspectRatio = false;
        aspectRatioX = 1;
        aspectRatioY = 1;

        borderLineThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, dm);
        borderLineColor = Color.argb(170, 255, 255, 255);
        borderCornerThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm);
        borderCornerOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, dm);
        borderCornerLength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, dm);
        borderCornerColor = Color.WHITE;

        guidelinesThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, dm);
        guidelinesColor = Color.argb(170, 255, 255, 255);
        backgroundColor = Color.argb(119, 0, 0, 0);

        minCropWindowWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, dm);
        minCropWindowHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, dm);
        minCropResultWidth = 40;
        minCropResultHeight = 40;
        maxCropResultWidth = MAX_CROP_LIMIT;
        maxCropResultHeight = MAX_CROP_LIMIT;

        activityTitle = "";
        activityMenuIconColor = 0;

        outputUri = Uri.EMPTY;
        outputCompressFormat = Bitmap.CompressFormat.JPEG;
        outputCompressQuality = DEFAULT_OUTPUT_QUALITY;
        outputRequestWidth = 0;
        outputRequestHeight = 0;
        outputRequestSizeOptions = CropImageView.RequestSizeOptions.NONE;
        isNoOutputImage = false;

        initialCropWindowRectangle = null;
        initialRotation = -1;
        isAllowRotation = true;
        isAllowFlipping = true;
        isAllowCounterRotation = false;
        rotationDegrees = DEFAULT_ROTATION_DEGREES;
        isFlipHorizontally = false;
        isFlipVertically = false;
        cropMenuCropButtonTitle = null;

        cropMenuCropButtonIcon = 0;
    }

    /** Create object from parcel. */
    @SuppressLint("ParcelClassLoader")
    protected CropImageOptions(Parcel in) {
        cropShape = CropImageView.CropShape.values()[in.readInt()];
        snapRadius = in.readFloat();
        touchRadius = in.readFloat();
        guidelines = CropImageView.Guidelines.values()[in.readInt()];
        scaleType = CropImageView.ScaleType.values()[in.readInt()];
        isShowCropOverlay = in.readByte() != 0;
        isShowProgressBar = in.readByte() != 0;
        isAutoZoomEnabled = in.readByte() != 0;
        isMultiTouchEnabled = in.readByte() != 0;
        maxZoom = in.readInt();
        initialCropWindowPaddingRatio = in.readFloat();
        isFixAspectRatio = in.readByte() != 0;
        aspectRatioX = in.readInt();
        aspectRatioY = in.readInt();
        borderLineThickness = in.readFloat();
        borderLineColor = in.readInt();
        borderCornerThickness = in.readFloat();
        borderCornerOffset = in.readFloat();
        borderCornerLength = in.readFloat();
        borderCornerColor = in.readInt();
        guidelinesThickness = in.readFloat();
        guidelinesColor = in.readInt();
        backgroundColor = in.readInt();
        minCropWindowWidth = in.readInt();
        minCropWindowHeight = in.readInt();
        minCropResultWidth = in.readInt();
        minCropResultHeight = in.readInt();
        maxCropResultWidth = in.readInt();
        maxCropResultHeight = in.readInt();
        activityTitle = in.readString();
        activityMenuIconColor = in.readInt();
        
        // Use consistent classloader approach for better compatibility
        outputUri = in.readParcelable(Object.class.getClassLoader());
        
        outputCompressFormat = Bitmap.CompressFormat.valueOf(in.readString());
        outputCompressQuality = in.readInt();
        outputRequestWidth = in.readInt();
        outputRequestHeight = in.readInt();
        outputRequestSizeOptions = CropImageView.RequestSizeOptions.values()[in.readInt()];
        isNoOutputImage = in.readInt() != 0;
        
        initialCropWindowRectangle = in.readParcelable(Object.class.getClassLoader());
        initialRotation = in.readInt();
        isAllowRotation = in.readByte() != 0;
        isAllowFlipping = in.readByte() != 0;
        isAllowCounterRotation = in.readByte() != 0;
        rotationDegrees = in.readInt();
        isFlipHorizontally = in.readByte() != 0;
        isFlipVertically = in.readByte() != 0;
        cropMenuCropButtonTitle = in.readString();
        cropMenuCropButtonIcon = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(cropShape.ordinal());
        dest.writeFloat(snapRadius);
        dest.writeFloat(touchRadius);
        dest.writeInt(guidelines.ordinal());
        dest.writeInt(scaleType.ordinal());
        dest.writeByte((byte) (isShowCropOverlay ? 1 : 0));
        dest.writeByte((byte) (isShowProgressBar ? 1 : 0));
        dest.writeByte((byte) (isAutoZoomEnabled ? 1 : 0));
        dest.writeByte((byte) (isMultiTouchEnabled ? 1 : 0));
        dest.writeInt(maxZoom);
        dest.writeFloat(initialCropWindowPaddingRatio);
        dest.writeByte((byte) (isFixAspectRatio ? 1 : 0));
        dest.writeInt(aspectRatioX);
        dest.writeInt(aspectRatioY);
        dest.writeFloat(borderLineThickness);
        dest.writeInt(borderLineColor);
        dest.writeFloat(borderCornerThickness);
        dest.writeFloat(borderCornerOffset);
        dest.writeFloat(borderCornerLength);
        dest.writeInt(borderCornerColor);
        dest.writeFloat(guidelinesThickness);
        dest.writeInt(guidelinesColor);
        dest.writeInt(backgroundColor);
        dest.writeInt(minCropWindowWidth);
        dest.writeInt(minCropWindowHeight);
        dest.writeInt(minCropResultWidth);
        dest.writeInt(minCropResultHeight);
        dest.writeInt(maxCropResultWidth);
        dest.writeInt(maxCropResultHeight);
        dest.writeString(activityTitle != null ? activityTitle.toString() : "");
        dest.writeInt(activityMenuIconColor);
        dest.writeParcelable(outputUri, flags);
        dest.writeString(outputCompressFormat.name());
        dest.writeInt(outputCompressQuality);
        dest.writeInt(outputRequestWidth);
        dest.writeInt(outputRequestHeight);
        dest.writeInt(outputRequestSizeOptions.ordinal());
        dest.writeInt(isNoOutputImage ? 1 : 0);
        dest.writeParcelable(initialCropWindowRectangle, flags);
        dest.writeInt(initialRotation);
        dest.writeByte((byte) (isAllowRotation ? 1 : 0));
        dest.writeByte((byte) (isAllowFlipping ? 1 : 0));
        dest.writeByte((byte) (isAllowCounterRotation ? 1 : 0));
        dest.writeInt(rotationDegrees);
        dest.writeByte((byte) (isFlipHorizontally ? 1 : 0));
        dest.writeByte((byte) (isFlipVertically ? 1 : 0));
        dest.writeString(cropMenuCropButtonTitle != null ? cropMenuCropButtonTitle.toString() : "");
        dest.writeInt(cropMenuCropButtonIcon);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // region Fluent Configuration Methods

    /** Set aspect ratio and enable fixed aspect ratio mode. */
    @SuppressWarnings("unused")
    public @NonNull CropImageOptions setAspectRatio(int x, int y) {
        this.aspectRatioX = x;
        this.aspectRatioY = y;
        this.isFixAspectRatio = true;
        return this;
    }

    /** Set output size for the cropped image. */
    @SuppressWarnings("unused")
    public @NonNull CropImageOptions setOutputSize(int width, int height) {
        this.outputRequestWidth = width;
        this.outputRequestHeight = height;
        return this;
    }

    /** Enable multi-touch functionality. */
    @SuppressWarnings("unused")
    public @NonNull CropImageOptions enableMultiTouch(boolean enabled) {
        this.isMultiTouchEnabled = enabled;
        return this;
    }

    /** Set crop shape. */
    @SuppressWarnings("unused")
    public @NonNull CropImageOptions setCropShape(@NonNull CropImageView.CropShape shape) {
        this.cropShape = shape;
        return this;
    }

    /** Set output URI for saving cropped image. */
    @SuppressWarnings("unused")
    public @NonNull CropImageOptions setOutputUri(@Nullable Uri uri) {
        this.outputUri = uri;
        return this;
    }

    // endregion

    /**
     * Validate all the options are within valid range.
     *
     * @throws IllegalArgumentException if any of the options is not valid
     */
    public void validate() {
        checkPositive("maxZoom", maxZoom);
        checkPositive("touchRadius", touchRadius);
        checkPositive("borderLineThickness", borderLineThickness);
        checkPositive("borderCornerThickness", borderCornerThickness);
        checkPositive("guidelinesThickness", guidelinesThickness);
        checkPositive("minCropWindowHeight", minCropWindowHeight);
        checkPositive("minCropResultWidth", minCropResultWidth);
        checkPositive("minCropResultHeight", minCropResultHeight);
        checkPositive("outputRequestWidth", outputRequestWidth);
        checkPositive("outputRequestHeight", outputRequestHeight);

        if (initialCropWindowPaddingRatio < 0 || initialCropWindowPaddingRatio >= 0.5) {
            throw new IllegalArgumentException(
                    "Cannot set initial crop window padding value to a number < 0 or >= 0.5");
        }
        if (aspectRatioX <= 0) {
            throw new IllegalArgumentException(
                    "Cannot set aspect ratio value to a number less than or equal to 0.");
        }
        if (aspectRatioY <= 0) {
            throw new IllegalArgumentException(
                    "Cannot set aspect ratio value to a number less than or equal to 0.");
        }
        if (maxCropResultWidth < minCropResultWidth) {
            throw new IllegalArgumentException(
                    "Cannot set max crop result width to smaller value than min crop result width");
        }
        if (maxCropResultHeight < minCropResultHeight) {
            throw new IllegalArgumentException(
                    "Cannot set max crop result height to smaller value than min crop result height");
        }
        if (rotationDegrees < 0 || rotationDegrees > 360) {
            throw new IllegalArgumentException(
                    "Cannot set rotation degrees value to a number < 0 or > 360");
        }
    }

    // region Helper Methods

    /** Helper method to check if a value is positive. */
    private void checkPositive(String name, float value) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be >= 0");
        }
    }

    // endregion
}