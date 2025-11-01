package topgrade.parent.com.parentseeks.Shared.Utils;

import android.view.View;
import android.widget.ProgressBar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.widget.NestedScrollView;

public class LoadingStateManager {
    
    private ProgressBar progressBar;
    private View contentView;
    private View errorView;
    
    public LoadingStateManager(ProgressBar progressBar, View contentView) {
        this.progressBar = progressBar;
        this.contentView = contentView;
    }
    
    public LoadingStateManager(ProgressBar progressBar, View contentView, View errorView) {
        this.progressBar = progressBar;
        this.contentView = contentView;
        this.errorView = errorView;
    }
    
    /**
     * Show loading state with enhanced visibility
     */
    public void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront(); // Ensure it's on top
        }
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show content state (loading complete)
     */
    public void showContent() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show error state - but keep loading instead of showing error
     */
    public void showError() {
        // DISABLED: Don't show error, just keep loading
        showLoading(); // Continue showing loading instead of error
    }
    
    /**
     * Show loading with skeleton effect
     */
    public void showSkeletonLoading() {
        showLoading();
        // Add skeleton animation if needed
    }
    
    /**
     * Show partial loading (for pagination)
     */
    public void showPartialLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        // Keep content visible for pagination
    }
}
