/*
 * Copyright (c) 2017-2018 Altimit Community Contributors
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
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import org.mozilla.gecko.PrefsHelper;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by felixjones on 07/10/2018.
 */
public class WebPlayerView extends GeckoView {

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
        super(context, attrs);
        init(context);
    }

    public WebPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        PrefsHelper.setPref("javascript.enabled", true);

        GeckoSession session = new GeckoSession();
        session.open(GeckoRuntime.create(context));
        setSession(session);

        mPlayer = new WebPlayer(this);

        setBackgroundColor(Color.BLACK);
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
    private static final class WebPlayer implements Player {

        private WebPlayerView mWebView;
        private Queue<Runnable> mOnPageFinishedActions;

        private WebPlayer(WebPlayerView webView) {
            mWebView = webView;
            mOnPageFinishedActions = new LinkedList<>();
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
        public void loadUrl(String url, Runnable onLoad) {
            mOnPageFinishedActions.add(onLoad);
            mWebView.getSession().loadUri(url);
            onPageFinished();
        }

        @Override
        @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
        public void addJavascriptInterface(Object object, String name) {
            // TODO
        }

        @Override
        public Context getContext() {
            return mWebView.getContext();
        }

        @Override
        public void loadData(String data) {
            mWebView.getSession().loadData(data.getBytes(), "text/html");
            onPageFinished();
        }

        @Override
        public void evaluateJavascript(String script) {
            mWebView.getSession().loadUri("javascript:" + script);
            onPageFinished();
        }

        @Override
        public void post(Runnable runnable) {
            mWebView.post(runnable);
        }

        @Override
        public void removeJavascriptInterface(String name) {
            // TODO
        }

        @Override
        public void pauseTimers() {
        }

        @Override
        public void onHide() {
        }

        @Override
        public void resumeTimers() {
        }

        @Override
        public void onShow() {
        }

        @Override
        public void onDestroy() {
        }

        void onPageFinished() {
            while (!mOnPageFinishedActions.isEmpty()) {
                mOnPageFinishedActions.remove().run();
            }
        }

    }

}