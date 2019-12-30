package com.huisou.library.user_protocol;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


import com.huisou.library.R;

public class SimpleWebActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_web);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(0xaaffffff);
        }
        TextView title = (TextView) findViewById(R.id.title);
        Intent intent = getIntent();
        title.setText(intent.getStringExtra("title"));
        WebView webView = (WebView) findViewById(R.id.web);
        initWeb(webView);
        webView.loadUrl(intent.getStringExtra("url"));
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initWeb(WebView webBase) {
        WebSettings webSettings = webBase.getSettings();

        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSavePassword(true);
        webSettings.setDomStorageEnabled(true);
        webBase.setSaveEnabled(true);


        webBase.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
    }
}
