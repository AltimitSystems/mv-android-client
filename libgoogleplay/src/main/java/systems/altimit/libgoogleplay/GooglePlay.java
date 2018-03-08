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
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

import systems.altimit.clientapi.AbstractExtension;

/**
 * Created by mgjus on 3/7/2018.
 */
public class GooglePlay extends AbstractExtension {
    private static final String INTERFACE_NAME = "__google_play";

    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_ACHIEVEMENT_UI = 9003;

    private Activity mActivity = null;

    private Map<String, Object> mInterfaces = new HashMap<>();

    private GoogleSignInClient mGoogleSignInClient;

    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private EventsClient mEventsClient;
    private PlayersClient mPlayersClient;

    public GooglePlay(@NonNull Context context) {
        super(context);
        mActivity = ((Activity) context);

        mGoogleSignInClient = GoogleSignIn.getClient(mActivity,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        mInterfaces.put(INTERFACE_NAME, this);
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
                String message = result.getStatus().getStatusMessage();

                // TODO: replace with descriptive errors for possible status codes
                if (message == null || message.isEmpty()) {
                    message = "Other sign in error";
                }

                onDisconnected();

                new AlertDialog.Builder(mActivity).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    @JavascriptInterface
    public void startSilentSignIn() {
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(mActivity,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            GoogleSignInAccount signInAccount = task.getResult();
                            onConnected(signInAccount);
                        } else {
                            startInteractiveSignIn();
                        }
                    }
                });
    }

    @JavascriptInterface
    public void startInteractiveSignIn() {
        mActivity.startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @JavascriptInterface
    public void signOut() {
        if (!isSignedIn()) {
            return;
        }

        mGoogleSignInClient.signOut().addOnCompleteListener(mActivity,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        onDisconnected();
                    }
                });
    }

    @JavascriptInterface
    public void showAchievementView() {
        if (mAchievementsClient != null) {
            mAchievementsClient.getAchievementsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            mActivity.startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                        }
                    });
        }
    }

    @JavascriptInterface
    public void unlockAchievement(String achievementId) {
        if (mAchievementsClient != null) {
            mAchievementsClient.unlock(achievementId);
        }
    }

    @JavascriptInterface
    public void incrementAchievementStep(String achievementId, int amountToIncrement) {
        if (mAchievementsClient != null) {
            mAchievementsClient.increment(achievementId, amountToIncrement);
        }
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        mAchievementsClient = Games.getAchievementsClient(mActivity, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(mActivity, googleSignInAccount);
        mEventsClient = Games.getEventsClient(mActivity, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(mActivity, googleSignInAccount);
    }

    private void onDisconnected() {
        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mEventsClient = null;
        mPlayersClient = null;
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(mActivity) != null;
    }
}
