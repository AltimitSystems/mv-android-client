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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.View;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import systems.altimit.clientapi.AbstractExtension;

/**
 * Created by felixjones on 28/04/2017.
 */
public class WebPlayerActivity extends Activity {

    private static final String TOUCH_INPUT_ON_CANCEL = "TouchInput._onCancel();";

    private Player mPlayer;
    private List<AbstractExtension> mExtensions;
    private AlertDialog mQuitDialog;
    private int mSystemUiVisibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.BACK_BUTTON_QUITS) {
            createQuitDialog();
        }

        mSystemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSystemUiVisibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            mSystemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            mSystemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            mSystemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mSystemUiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
        }

        mPlayer = PlayerHelper.create(this);

        mExtensions = new ArrayList<>();
        try {
            for (String extensionClass : BuildConfig.EXTENSION_CLASSES) {
                mExtensions.add((AbstractExtension) Class.forName(extensionClass).getConstructor(Context.class).newInstance(this));
            }
        } catch (Exception e) {
            e.printStackTrace(); // A bad extension will fail and print a stack-trace
        }

        mPlayer.setKeepScreenOn();
        setContentView(mPlayer.getView());

        List<String> extensionSources = new ArrayList<>();
        for (AbstractExtension extension : mExtensions) {
            for (Map.Entry<String, Object> entry : extension.getJavascriptInterfaces().entrySet()) {
                mPlayer.addJavascriptInterface(entry.getValue(), entry.getKey());
            }
            extensionSources.addAll(Arrays.asList(extension.getJavascriptSources()));
        }

        if (!addBootstrapInterface(mPlayer, extensionSources)) {
            Uri.Builder projectURIBuilder = Uri.fromFile(new File(getString(R.string.mv_project_index))).buildUpon();
            Bootstrapper.appendQuery(projectURIBuilder, getString(R.string.query_noaudio));
            if (BuildConfig.SHOW_FPS) {
                Bootstrapper.appendQuery(projectURIBuilder, getString(R.string.query_showfps));
            }
            mPlayer.loadUrl(projectURIBuilder.build().toString(), new SourceListEvaluator(mPlayer, extensionSources));
        }
    }

    @Override
    public void onBackPressed() {
        if (BuildConfig.BACK_BUTTON_QUITS) {
            if (mQuitDialog != null) {
                mQuitDialog.show();
            } else {
                super.onBackPressed();
            }
        } else {
            mPlayer.evaluateJavascript(TOUCH_INPUT_ON_CANCEL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.pauseTimers();
            mPlayer.onHide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(mSystemUiVisibility);
        if (mPlayer != null) {
            mPlayer.resumeTimers();
            mPlayer.onShow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.onDestroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (AbstractExtension extension : mExtensions) {
            extension.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void createQuitDialog() {
        String appName = getString(R.string.app_name);
        String[] quitLines = getResources().getStringArray(R.array.quit_message);
        StringBuilder quitMessage = new StringBuilder();
        for (int ii = 0; ii < quitLines.length; ii++) {
            quitMessage.append(quitLines[ii].replace("$1", appName));
            if (ii < quitLines.length - 1) {
                quitMessage.append("\n");
            }
        }

        if (quitMessage.length() > 0) {
            mQuitDialog = new AlertDialog.Builder(this)
                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            getWindow().getDecorView().setSystemUiVisibility(mSystemUiVisibility);
                        }
                    })
                    .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WebPlayerActivity.super.onBackPressed();
                        }
                    })
                    .setMessage(quitMessage.toString())
                    .create();
        }
    }

    private static boolean addBootstrapInterface(Player player, List<String> sources) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            new Bootstrapper(player, sources);
            return true;
        }
        return false;
    }

    /**
     *
     */
    private static final class SourceListEvaluator implements Runnable {

        private Player mPlayer;
        private List<String> mSources;

        private SourceListEvaluator(Player player, List<String> sourceList) {
            mPlayer = player;
            mSources = sourceList;
        }

        @Override
        public void run() {
            for (String source : mSources) {
                mPlayer.evaluateJavascript(source);
            }
        }
    }

    /**
     *
     */
    private static final class Bootstrapper extends PlayerHelper.Interface implements Runnable {

        private static Uri.Builder appendQuery(Uri.Builder builder, String query) {
            Uri current = builder.build();
            String oldQuery = current.getEncodedQuery();
            if (oldQuery != null && oldQuery.length() > 0) {
                query = oldQuery + "&" + query;
            }
            return builder.encodedQuery(query);
        }

        private static final String INTERFACE = "boot";
        private static final String PREPARE_FUNC = "prepare( webgl(), webaudio(), false )";

        private Player mPlayer;
        private List<String> mExtensionSources;
        private Uri.Builder mURIBuilder;

        private Bootstrapper(Player player, List<String> sourceList) {
            Context context = player.getContext();
            player.addJavascriptInterface(this, Bootstrapper.INTERFACE);

            mPlayer = player;
            mExtensionSources = sourceList;
            mURIBuilder = Uri.fromFile(new File(context.getString(R.string.mv_project_index))).buildUpon();
            mPlayer.loadData(new String(Base64.decode(context.getString(R.string.webview_default_page), Base64.DEFAULT), Charset.forName("UTF-8")));
        }

        @Override
        protected void onStart() {
            Context context = mPlayer.getContext();
            final String code = new String(Base64.decode(context.getString(R.string.webview_detection_source), Base64.DEFAULT), Charset.forName("UTF-8")) + INTERFACE + "." + PREPARE_FUNC + ";";
            mPlayer.post(new Runnable() {
                @Override
                public void run() {
                    mPlayer.evaluateJavascript(code);
                }
            });
        }

        @Override
        protected void onPrepare(boolean webgl, boolean webaudio, boolean showfps) {
            Context context = mPlayer.getContext();
            if (webgl) {
                mURIBuilder = appendQuery(mURIBuilder, context.getString(R.string.query_webgl));
            }
            if (!webaudio) {
                mURIBuilder = appendQuery(mURIBuilder, context.getString(R.string.query_noaudio));
            }
            if (showfps || BuildConfig.SHOW_FPS) {
                mURIBuilder = appendQuery(mURIBuilder, context.getString(R.string.query_showfps));
            }
            mPlayer.post(this);
        }

        @Override
        public void run() {
            mPlayer.removeJavascriptInterface(INTERFACE);
            mPlayer.loadUrl(mURIBuilder.build().toString(), new SourceListEvaluator(mPlayer, mExtensionSources));
        }

    }

}