/*
 * Copyright (c) 2018 Altimit Community Contributors
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

package systems.altimit.libgoogleplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

import systems.altimit.clientapi.AbstractExtension;
import systems.altimit.libgoogleplay.handlers.AchievementsHandler;
import systems.altimit.libgoogleplay.handlers.EventsHandler;
import systems.altimit.libgoogleplay.handlers.LeaderboardsHandler;

/**
 * Created by mgjus on 3/7/2018.
 */
public class GooglePlay extends AbstractExtension {
    private static final String INTERFACE_NAME = "__google_play_main";

    private static final int RC_SIGN_IN = 9001;

    private Activity mParentActivity;

    private Map<String, Object> mInterfaces = new HashMap<>();

    private GoogleSignInClient mGoogleSignInClient;

    private AchievementsHandler mAchievementsHandler;
    private LeaderboardsHandler mLeaderboardsHandler;
    private EventsHandler mEventsHandler;

    private boolean autoSignInEnabled;
    private boolean manualSignOut = false;

    public GooglePlay(@NonNull Context context) {
        super(context);
        mParentActivity = ((Activity) context);

        String val = mParentActivity.getString(R.string.app_id);

        if (val.startsWith("YOUR_")) {
            String message = "The APP_ID in ids.xml for this app has not been set, " +
                    "Google Play Services will not be initialized";

            new AlertDialog.Builder(mParentActivity).setMessage(message)
                    .setNeutralButton(android.R.string.ok, null).create().show();
        }

        autoSignInEnabled = BuildConfig.ALLOW_AUTO_SIGNIN;

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(mParentActivity, googleSignInOptions);

        mAchievementsHandler = new AchievementsHandler(mParentActivity);
        mLeaderboardsHandler = new LeaderboardsHandler(mParentActivity);
        mEventsHandler = new EventsHandler(mParentActivity);

        mInterfaces.put(INTERFACE_NAME, this);
        mInterfaces.put(AchievementsHandler.INTERFACE_NAME, mAchievementsHandler);
        mInterfaces.put(LeaderboardsHandler.INTERFACE_NAME, mLeaderboardsHandler);
        mInterfaces.put(EventsHandler.INTERFACE_NAME, mEventsHandler);
    }

    @Override
    public Map<String, Object> getJavascriptInterfaces() {
        return mInterfaces;
    }

    @Override
    public String[] getJavascriptSources() {
        return new String[0];
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                GoogleSignInAccount signInAccount = result.getSignInAccount();
                onConnected(signInAccount);

                manualSignOut = false;
            } else {
                int statusCode = result.getStatus().getStatusCode();

                onDisconnected();

                handleErrorStatusCodes(statusCode);
            }
        }
    }

    @Override
    public void onResume() {
        if (!manualSignOut && autoSignInEnabled) {
            startSilentSignIn();
        }
    }

    @Override
    public void onStart() {
        if (!manualSignOut && autoSignInEnabled) {
            startInteractiveSignIn();
        }
    }

    @JavascriptInterface
    public void startInteractiveSignIn() {
        mParentActivity.startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @JavascriptInterface
    public void signOut() {
        if (!isSignedIn()) {
            return;
        }

        mGoogleSignInClient.signOut().addOnCompleteListener(mParentActivity,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        onDisconnected();
                    }
                });

        manualSignOut = true;
    }

    @JavascriptInterface
    public boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(mParentActivity) != null;
    }

    private void handleErrorStatusCodes(int statusCode) {
        String message;

        switch (statusCode) {
            case CommonStatusCodes.API_NOT_CONNECTED:
                message = mParentActivity.getString(R.string.api_not_connected);
                break;
            case CommonStatusCodes.CANCELED:
                message = mParentActivity.getString(R.string.api_cancelled);
                break;
            case CommonStatusCodes.DEVELOPER_ERROR:
                message = mParentActivity.getString(R.string.api_misconfigured);
                break;
            case CommonStatusCodes.ERROR:
                message = mParentActivity.getString(R.string.api_error);
                break;
            case CommonStatusCodes.INTERNAL_ERROR: case CommonStatusCodes.NETWORK_ERROR:
                message = mParentActivity.getString(R.string.api_internal_network_error);
                break;
            case CommonStatusCodes.INVALID_ACCOUNT:
                message = mParentActivity.getString(R.string.api_invalid_account);
                break;
            case CommonStatusCodes.TIMEOUT:
                message = mParentActivity.getString(R.string.api_timeout);
                break;
                default:
                    message = mParentActivity.getString(R.string.api_unspecified);
                    break;
        }

        new AlertDialog.Builder(mParentActivity).setMessage(message)
                .setNeutralButton(android.R.string.ok, null).show();
    }

    private void startSilentSignIn() {
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(mParentActivity,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            GoogleSignInAccount signInAccount = task.getResult();
                            onConnected(signInAccount);
                        } else {
                            ApiException exception = ((ApiException) task.getException());

                            if (exception != null) {
                                if (exception.getStatusCode()
                                        == CommonStatusCodes.SIGN_IN_REQUIRED) {
                                    startInteractiveSignIn();
                                } else {
                                    handleErrorStatusCodes(exception.getStatusCode());
                                }
                            } else {
                                Log.e(INTERFACE_NAME, "SIGN-IN GOT SOMEWHERE IT SHOULDN'T " +
                                        "HAVE WHICH IS REALLY NOT GOOD");
                            }
                        }
                    }
                });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        mAchievementsHandler.setClient(Games.getAchievementsClient(mParentActivity,
                googleSignInAccount));
        mLeaderboardsHandler.setClient(Games.getLeaderboardsClient(mParentActivity,
                googleSignInAccount));
        mEventsHandler.setClient(Games.getEventsClient(mParentActivity, googleSignInAccount));

        mAchievementsHandler.unlockCachedAchievements();
        mAchievementsHandler.cacheAchievements();
    }

    private void onDisconnected() {
        mAchievementsHandler.setClient(null);
        mLeaderboardsHandler.setClient(null);
        mEventsHandler.setClient(null);
    }
}
