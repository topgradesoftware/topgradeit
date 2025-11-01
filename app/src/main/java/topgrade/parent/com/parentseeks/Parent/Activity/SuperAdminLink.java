package topgrade.parent.com.parentseeks.Parent.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;

public class SuperAdminLink extends AppCompatActivity {
    private Context context;
    private WebView wb;
    ProgressBar progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin_link);

        // Apply unified parent theme for super admin link page
        ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
        ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for super admin
        ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for super admin
        ParentThemeHelper.setFooterVisibility(this, true); // Show footer
        ParentThemeHelper.setHeaderTitle(this, "Super Admin");

        context = SuperAdminLink.this;


        wb = (WebView) findViewById(R.id.my_web);
        progress_bar = findViewById(R.id.progress_bar);

        progress_bar.setVisibility(View.VISIBLE);


        wb.getSettings().setJavaScriptEnabled(true);
        wb.getSettings().setLoadWithOverviewMode(true);
        wb.getSettings().setUseWideViewPort(true);
        wb.getSettings().setBuiltInZoomControls(true);
        wb.getSettings().setPluginState(WebSettings.PluginState.ON);
        wb.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        wb.getSettings().setBuiltInZoomControls(false);
        wb.setWebViewClient(new MyWebViewClient());
        wb.loadUrl("https://topgradeit.com/superadmin");

        progress_bar.setVisibility(View.GONE);

    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progress_bar.setVisibility(View.VISIBLE);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progress_bar.setVisibility(View.GONE);

        }
    }
}
