/*
 * Copyright (c) 2017-2019 Altimit Community Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or imp
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package systems.altimit.rpgmakermv;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by felixjones on 28/04/2017.
 */
public class WebPlayerView extends WebView {

    private WebPlayer mPlayer;

    public WebPlayerView(Context context) {
        super(context);
        init(context);
    }

    public WebPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WebPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mPlayer = new WebPlayer(this);

        setBackgroundColor(Color.BLACK);

        enableJavascript();

        WebSettings webSettings = getSettings();
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDatabasePath(context.getDir("database", Context.MODE_PRIVATE).getPath());
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setSupportMultipleWindows(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                webSettings.setMediaPlaybackRequiresUserGesture(false);
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        }

        setWebChromeClient(new ChromeClient());
        setWebViewClient(new ViewClient());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void enableJavascript() {
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
    }

    @Override
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return false;
    }

    @Override
    public void scrollTo(int x, int y) {}

    @Override
    public void computeScroll() {}

    public Player getPlayer() {
        return mPlayer;
    }

    /**
     *
     */
    private class ChromeClient extends WebChromeClient {

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage){
            if ("Scripts may close only the windows that were opened by it.".equals(consoleMessage.message())) {
                if (mPlayer.getContext() instanceof WebPlayerActivity) {
                    ((WebPlayerActivity) mPlayer.getContext()).finish();
                }
            }
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView dumbWV = new WebView(view.getContext());
            dumbWV.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    view.getContext().startActivity(browserIntent);
                }
            });
            ((WebView.WebViewTransport) resultMsg.obj).setWebView(dumbWV);
            resultMsg.sendToTarget();
            return true;
        }

    }

    /**
     *
     */
    private class ViewClient extends WebViewClient {

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            view.setBackgroundColor(Color.WHITE);
        }

        @SuppressWarnings("deprecation")
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            view.setBackgroundColor(Color.WHITE);
        }

    }

    /**
     *
     */
    private static final class WebPlayer implements Player {

        private WebPlayerView mWebView;

        private WebPlayer(WebPlayerView webView) {
            mWebView = webView;
        }

        @Override
        public void setKeepScreenOn() {
            mWebView.setKeepScreenOn(true);
        }

        @Override
        public View getView() {
            return mWebView;
        }

        @Override
        public void loadUrl(String url) {
            mWebView.loadUrl(url);
        }

        @Override
        @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
        public void addJavascriptInterface(Object object, String name) {
            mWebView.addJavascriptInterface(object, name);
        }

        @Override
        public Context getContext() {
            return mWebView.getContext();
        }

        @Override
        public void loadData(String data) {
            mWebView.loadData(data, "text/html", "base64");
        }

        @Override
        public void evaluateJavascript(String script) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.evaluateJavascript(script, null);
            } else {
                mWebView.loadUrl("javascript:" + script);
            }
        }

        @Override
        public void post(Runnable runnable) {
            mWebView.post(runnable);
        }

        @Override
        public void removeJavascriptInterface(String name) {
            mWebView.removeJavascriptInterface(name);
        }

        @Override
        public void pauseTimers() {
            mWebView.pauseTimers();
        }

        @Override
        public void onHide() {
            mWebView.onPause();
        }

        @Override
        public void resumeTimers() {
            mWebView.resumeTimers();
        }

        @Override
        public void onShow() {
            mWebView.onResume();
        }

        @Override
        public void onDestroy() {
            mWebView.destroy();
        }

    }

}
