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
import systems.altimit.libgoogleplay.handlers.PlayersHandler;

/**
 * Created by mgjus on 3/7/2018.
 */
public class GooglePlay extends AbstractExtension {
    private static final String INTERFACE_NAME = "__google_play";

    private static final int RC_SIGN_IN = 9001;

    private Activity mParentActivity;

    private Map<String, Object> mInterfaces = new HashMap<>();

    private GoogleSignInClient mGoogleSignInClient;

    private AchievementsHandler mAchievementsHandler;
    private LeaderboardsHandler mLeaderboardsHandler;
    private EventsHandler mEventsHandler;
    private PlayersHandler mPlayersHandler;

    public GooglePlay(@NonNull Context context) {
        super(context);
        mParentActivity = ((Activity) context);

        mGoogleSignInClient = GoogleSignIn.getClient(mParentActivity,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                        .build());

        mAchievementsHandler = new AchievementsHandler(mParentActivity);
        mLeaderboardsHandler = new LeaderboardsHandler(mParentActivity);
        mEventsHandler = new EventsHandler(mParentActivity);
        mPlayersHandler = new PlayersHandler(mParentActivity);

        mInterfaces.put(INTERFACE_NAME, this);
        mInterfaces.put(INTERFACE_NAME, mAchievementsHandler);
        mInterfaces.put(INTERFACE_NAME, mLeaderboardsHandler);

        /*
        * keeping this here until implementation is finished
        *
        * mInterfaces.put(INTERFACE_NAME, mEventsHandler);
        * mInterfaces.put(INTERFACE_NAME, mParentActivity);
        *
        */
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
            } else {
                int statusCode = result.getStatus().getStatusCode();

                onDisconnected();

                handleErrorStatusCodes(statusCode);
            }
        }
    }

    @Override
    public void onResume() {
        startSilentSignIn();
    }

    @JavascriptInterface
    public void startSilentSignIn() {
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(mParentActivity,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            GoogleSignInAccount signInAccount = task.getResult();
                            onConnected(signInAccount);
                        } else {
                            ApiException exception = ((ApiException) task.getException());

                            if ((exception != null ? exception.getStatusCode() : 0)
                                    == CommonStatusCodes.SIGN_IN_REQUIRED) {
                                startInteractiveSignIn();
                            }
                        }
                    }
                });
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
    }

    private void handleErrorStatusCodes(int statusCode) {
        String message;

        // TODO: implement status code handling
        switch (statusCode) {
            default:
                message = "An unknown error has occurred...";
        }

        new AlertDialog.Builder(mParentActivity).setMessage(message)
                .setNeutralButton(android.R.string.ok, null).show();
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        mAchievementsHandler.setClient(Games.getAchievementsClient(mParentActivity,
                googleSignInAccount));
        mLeaderboardsHandler.setClient(Games.getLeaderboardsClient(mParentActivity,
                googleSignInAccount));
        mEventsHandler.setClient(Games.getEventsClient(mParentActivity, googleSignInAccount));
        mPlayersHandler.setClient(Games.getPlayersClient(mParentActivity, googleSignInAccount));
    }

    private void onDisconnected() {
        mAchievementsHandler.setClient(null);
        mLeaderboardsHandler.setClient(null);
        mEventsHandler.setClient(null);
        mPlayersHandler.setClient(null);
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(mParentActivity) != null;
    }
}
