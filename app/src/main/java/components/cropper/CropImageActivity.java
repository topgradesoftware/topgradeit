package components.cropper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import topgrade.parent.com.parentseeks.R;

/**
 * Built-in activity for image cropping.
 * Use {@link CropImage#activity(Uri)} to create a builder to start this activity.
 */
public class CropImageActivity extends AppCompatActivity
    implements CropImageView.OnSetImageUriCompleteListener,
        CropImageView.OnCropImageCompleteListener {

  private static final String TAG = "CropImageActivity";

  /** The crop image view library widget used in the activity */
  private CropImageView mCropImageView;

  /** Persist URI image to crop if specific permissions are required */
  private Uri mCropImageUri;

  /** the options that were set for the crop image */
  private CropImageOptions mOptions;

  /** Modern activity result launcher for image picking */
  private ActivityResultLauncher<Intent> pickImageLauncher;

  /** Modern permission launcher for camera */
  private ActivityResultLauncher<String> cameraPermissionLauncher;

  /** Modern permission launcher for storage / media */
  private ActivityResultLauncher<String> storagePermissionLauncher;

  @Override
  @SuppressLint("NewApi")
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Register modern activity result launchers BEFORE setContentView
    registerActivityResultLaunchers();

    setContentView(R.layout.crop_image_activity);
    mCropImageView = findViewById(R.id.cropImageView);

    // Safely read bundle and options (protect against malformed Intents)
    Bundle bundle = getIntent() != null ? getIntent().getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE) : null;
    if (bundle != null) {
      try {
        mCropImageUri = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, Uri.class);
      } catch (Exception e) {
        Log.w(TAG, "Failed to read source uri from bundle", e);
        mCropImageUri = null;
      }
      try {
        mOptions = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, CropImageOptions.class);
      } catch (Exception e) {
        Log.w(TAG, "Failed to read options from bundle", e);
        mOptions = null;
      }
    } else {
      mCropImageUri = null;
      mOptions = null;
    }

    // Ensure options is not null to avoid repeated null checks later
    if (mOptions == null) {
      mOptions = new CropImageOptions();
    }

    if (savedInstanceState == null) {
      // If no source uri provided, start picker (but check camera permission first)
      if (mCropImageUri == null || Uri.EMPTY.equals(mCropImageUri)) {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
          cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
          CropImage.startPickImageActivity(this, pickImageLauncher);
        }
      } else if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {
        // request storage permission (legacy) — launcher will handle response
        storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
      } else {
        // safe to load
        mCropImageView.setImageUriAsync(mCropImageUri);
      }
    }

    // ActionBar / title
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      CharSequence title =
          (mOptions != null && mOptions.activityTitle != null && !mOptions.activityTitle.isEmpty())
              ? mOptions.activityTitle
              : getResources().getString(R.string.crop_image_activity_title);
      actionBar.setTitle(title);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (mCropImageView != null) {
      mCropImageView.setOnSetImageUriCompleteListener(this);
      mCropImageView.setOnCropImageCompleteListener(this);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mCropImageView != null) {
      mCropImageView.setOnSetImageUriCompleteListener(null);
      mCropImageView.setOnCropImageCompleteListener(null);
    }
  }

  /**
   * Register modern ActivityResultLaunchers for image picking and permissions.
   * Must be called before setContentView in onCreate.
   */
  private void registerActivityResultLaunchers() {
    // Modern image picker launcher
    pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result == null) {
            setResultCancel();
            return;
          }
          final int resultCode = result.getResultCode();
          if (resultCode == Activity.RESULT_CANCELED) {
            // User cancelled the picker
            setResultCancel();
          } else if (resultCode == Activity.RESULT_OK) {
            try {
              mCropImageUri = CropImage.getPickImageResultUri(this, result.getData());
            } catch (Exception e) {
              Log.w(TAG, "Failed to parse picked image uri", e);
              mCropImageUri = null;
            }

            if (mCropImageUri == null) {
              Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
              setResultCancel();
              return;
            }

            // Check if we need storage permissions (legacy)
            if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {
              storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
              mCropImageView.setImageUriAsync(mCropImageUri);
            }
          }
        }
    );

    // Modern camera permission launcher
    cameraPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        isGranted -> {
          // Regardless of grant result, show the picker (picker will offer camera if available)
          // If permission was denied, image capture intent may fail — picker handles that.
          if (!isFinishing()) {
            CropImage.startPickImageActivity(this, pickImageLauncher);
          }
        }
    );

    // Modern storage permission launcher
    storagePermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        isGranted -> {
          if (isGranted && mCropImageUri != null) {
            mCropImageView.setImageUriAsync(mCropImageUri);
          } else {
            Toast.makeText(this, R.string.crop_image_activity_no_permissions, Toast.LENGTH_LONG).show();
            setResultCancel();
          }
        }
    );
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.crop_image_menu, menu);

    if (!mOptions.isAllowRotation) {
      menu.removeItem(R.id.crop_image_menu_rotate_left);
      menu.removeItem(R.id.crop_image_menu_rotate_right);
    } else if (mOptions.isAllowCounterRotation) {
      MenuItem left = menu.findItem(R.id.crop_image_menu_rotate_left);
      if (left != null) left.setVisible(true);
    }

    if (!mOptions.isAllowFlipping) {
      menu.removeItem(R.id.crop_image_menu_flip);
    }

    if (mOptions.cropMenuCropButtonTitle != null) {
      MenuItem cropItem = menu.findItem(R.id.crop_image_menu_crop);
      if (cropItem != null) cropItem.setTitle(mOptions.cropMenuCropButtonTitle);
    }

    Drawable cropIcon = null;
    try {
      if (mOptions.cropMenuCropButtonIcon != 0) {
        cropIcon = ContextCompat.getDrawable(this, mOptions.cropMenuCropButtonIcon);
        MenuItem cropItem = menu.findItem(R.id.crop_image_menu_crop);
        if (cropItem != null) cropItem.setIcon(cropIcon);
      }
    } catch (Exception e) {
      Log.w(TAG, "Failed to read menu crop drawable", e);
    }

    if (mOptions.activityMenuIconColor != 0) {
      updateMenuItemIconColor(menu, R.id.crop_image_menu_rotate_left, mOptions.activityMenuIconColor);
      updateMenuItemIconColor(menu, R.id.crop_image_menu_rotate_right, mOptions.activityMenuIconColor);
      updateMenuItemIconColor(menu, R.id.crop_image_menu_flip_horizontally, mOptions.activityMenuIconColor);
      updateMenuItemIconColor(menu, R.id.crop_image_menu_flip_vertically, mOptions.activityMenuIconColor);
      if (cropIcon != null) {
        updateMenuItemIconColor(menu, R.id.crop_image_menu_crop, mOptions.activityMenuIconColor);
      }
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    final int id = item.getItemId();
    if (id == R.id.crop_image_menu_crop) {
      cropImage();
      return true;
    } else if (id == R.id.crop_image_menu_rotate_left) {
      rotateImage(-mOptions.rotationDegrees);
      return true;
    } else if (id == R.id.crop_image_menu_rotate_right) {
      rotateImage(mOptions.rotationDegrees);
      return true;
    } else if (id == R.id.crop_image_menu_flip_horizontally) {
      mCropImageView.flipImageHorizontally();
      return true;
    } else if (id == R.id.crop_image_menu_flip_vertically) {
      mCropImageView.flipImageVertically();
      return true;
    } else if (id == android.R.id.home) {
      setResultCancel();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void onBackPressed() {
    // Ensure cancel result is set for callers
    setResultCancel();
    super.onBackPressed();
  }

  /**
   * @deprecated Use registerActivityResultLaunchers() with modern ActivityResultLauncher instead.
   * Kept for backward compatibility only.
   */
  @Deprecated
  @SuppressLint("NewApi")
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    // Legacy kept intentionally empty — modern launcher handles results.
  }

  /**
   * @deprecated Use modern permission launchers instead.
   * Kept for backward compatibility only.
   */
  @Deprecated
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    // Legacy kept intentionally empty — modern launchers handle permissions.
  }

  @Override
  public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
    if (error == null) {
      if (mOptions.initialCropWindowRectangle != null) {
        try {
          mCropImageView.setCropRect(mOptions.initialCropWindowRectangle);
        } catch (Exception e) {
          Log.w(TAG, "Failed to set initial crop rect", e);
        }
      }
      if (mOptions.initialRotation > -1) {
        mCropImageView.setRotatedDegrees(mOptions.initialRotation);
      }
    } else {
      Log.w(TAG, "Error setting image URI", error);
      setResult(null, error, 1);
    }
  }

  @Override
  public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
    setResult(result.getUri(), result.getError(), result.getSampleSize());
  }

  // region: Private methods

  /** Execute crop image and save the result to output uri. */
  protected void cropImage() {
    if (mOptions.isNoOutputImage) {
      setResult(null, null, 1);
    } else {
      Uri outputUri;
      try {
        outputUri = getOutputUri();
      } catch (Exception e) {
        Log.e(TAG, "Failed to get output uri", e);
        setResult(null, e, 1);
        return;
      }
      mCropImageView.saveCroppedImageAsync(
          outputUri,
          mOptions.outputCompressFormat,
          mOptions.outputCompressQuality,
          mOptions.outputRequestWidth,
          mOptions.outputRequestHeight,
          mOptions.outputRequestSizeOptions);
    }
  }

  /** Rotate the image in the crop image view. */
  protected void rotateImage(int degrees) {
    if (mCropImageView != null) mCropImageView.rotateImage(degrees);
  }

  /**
   * Get Android uri to save the cropped image into.
   */
  protected Uri getOutputUri() {
    Uri outputUri = mOptions.outputUri;
    if (outputUri == null || Uri.EMPTY.equals(outputUri)) {
      try {
        String ext =
            mOptions.outputCompressFormat == Bitmap.CompressFormat.JPEG
                ? ".jpg"
                : mOptions.outputCompressFormat == Bitmap.CompressFormat.PNG ? ".png" : ".webp";
        File tmp = File.createTempFile("cropped", ext, getCacheDir());
        outputUri = Uri.fromFile(tmp);
      } catch (IOException e) {
        throw new RuntimeException("Failed to create temp file for output image", e);
      }
    }
    return outputUri;
  }

  /** Result with cropped image data or error if failed. */
  protected void setResult(Uri uri, Exception error, int sampleSize) {
    int resultCode = (error == null) ? RESULT_OK : CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
    setResult(resultCode, getResultIntent(uri, error, sampleSize));
    finish();
  }

  /** Cancel of cropping activity. */
  protected void setResultCancel() {
    // Use explicit cancel result so the caller can detect cancellation
    setResult(RESULT_CANCELED);
  }

  /** Get intent instance to be used for the result of this activity. */
  protected Intent getResultIntent(Uri uri, Exception error, int sampleSize) {
    CropImage.ActivityResult result =
        new CropImage.ActivityResult(
            mCropImageView != null ? mCropImageView.getImageUri() : null,
            uri,
            error,
            (mCropImageView != null) ? mCropImageView.getCropPoints() : null,
            (mCropImageView != null) ? mCropImageView.getCropRect() : null,
            (mCropImageView != null) ? mCropImageView.getRotatedDegrees() : 0,
            (mCropImageView != null) ? mCropImageView.getWholeImageRect() : null,
            sampleSize);
    Intent intent = new Intent();
    if (getIntent() != null) intent.putExtras(getIntent());
    intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result);
    return intent;
  }

  /** Update the color of a specific menu item to the given color. */
  private void updateMenuItemIconColor(Menu menu, int itemId, int color) {
    MenuItem menuItem = menu.findItem(itemId);
    if (menuItem != null) {
      Drawable menuItemIcon = menuItem.getIcon();
      if (menuItemIcon != null) {
        try {
          menuItemIcon.mutate();
          menuItemIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
          menuItem.setIcon(menuItemIcon);
        } catch (Exception e) {
          Log.w(TAG, "Failed to update menu item color", e);
        }
      }
    }
  }
  // endregion
}