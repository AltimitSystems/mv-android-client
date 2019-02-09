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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

/**
 * Created by felixjones on 12/05/2017.
 */
public class XWalkPlayerView extends XWalkView {

    private XWalkPlayer mPlayer;

    public XWalkPlayerView(Context context) {
        super(context);
        init(context);
    }

    public XWalkPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context context) {
        mPlayer = new XWalkPlayer(this);

        setBackgroundColor(Color.BLACK);

        enableJavascript();

        XWalkSettings webSettings = getSettings();
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setSupportMultipleWindows(true);

        setResourceClient(new ResourceClient(this));
        setUIClient(new UIClient(this));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            ((Activity) getContext()).onBackPressed();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void enableJavascript() {
        XWalkSettings webSettings = getSettings();
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
    private class UIClient extends XWalkUIClient {

        private UIClient(XWalkView view) {
            super(view);
        }

        public boolean onCreateWindowRequested(XWalkView view, XWalkUIClient.InitiateBy initiator, ValueCallback<XWalkView> callback) {
            final XWalkView dumbWV = new XWalkView(view.getContext());
            dumbWV.setVisibility(View.INVISIBLE);
            view.addView(dumbWV);
            dumbWV.setResourceClient(new XWalkResourceClient (dumbWV) {
                @Override
                public void onLoadStarted(XWalkView view, String url) {
                    ((ViewGroup) dumbWV.getParent()).removeView(view);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    view.getContext().startActivity(browserIntent);
                }
            });
            callback.onReceiveValue(dumbWV);
            return true;
        }

    }

    /**
     *
     */
    private class ResourceClient extends XWalkResourceClient {

        private ResourceClient(XWalkView view) {
            super(view);
        }

    }

    /**
     *
     */
    private static final class XWalkPlayer implements Player {

        private XWalkView mXWalkView;

        private XWalkPlayer(XWalkView xWalkView) {
            mXWalkView = xWalkView;
        }

        @Override
        public void setKeepScreenOn() {
            mXWalkView.setKeepScreenOn(true);
        }

        @Override
        public View getView() {
            return mXWalkView;
        }

        @Override
        public void loadUrl(String url) {
            mXWalkView.loadUrl(url);
        }

        @Override
        public void addJavascriptInterface(Object object, String name) {
            mXWalkView.addJavascriptInterface(object, name);
        }

        @Override
        public Context getContext() {
            return mXWalkView.getContext();
        }

        @Override
        public void loadData(String data) {
            mXWalkView.loadData(data, "text/html", "base64");
        }

        @Override
        public void evaluateJavascript(String script) {
            mXWalkView.evaluateJavascript(script, null);
        }

        @Override
        public void post(Runnable runnable) {
            mXWalkView.post(runnable);
        }

        @Override
        public void removeJavascriptInterface(String name) {
            mXWalkView.removeJavascriptInterface(name);
        }

        @Override
        public void pauseTimers() {
            mXWalkView.pauseTimers();
        }

        @Override
        public void onHide() {
            mXWalkView.onHide();
        }

        @Override
        public void resumeTimers() {
            mXWalkView.resumeTimers();
        }

        @Override
        public void onShow() {
            mXWalkView.onShow();
        }

        @Override
        public void onDestroy() {
            mXWalkView.onDestroy();
        }

    }

}