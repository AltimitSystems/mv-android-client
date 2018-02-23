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
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

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

import static com.google.android.gms.common.api.CommonStatusCodes.API_NOT_CONNECTED;
import static com.google.android.gms.common.api.CommonStatusCodes.DEVELOPER_ERROR;
import static com.google.android.gms.common.api.CommonStatusCodes.INTERNAL_ERROR;
import static com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR;
import static com.google.android.gms.common.api.CommonStatusCodes.TIMEOUT;

/**
 * Created by tehguy on 2/22/2018.
 */

@SuppressWarnings("DefaultFileTemplate")
class GooglePlayHandler {

    /* Client used to sign in with Google APIs */
    private GoogleSignInClient mGoogleSignInClient;

    /* Client vars */
    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private EventsClient mEventsClient;
    private PlayersClient mPlayersClient;

    /* Request codes used when invoking external activities */
    private static final int RC_UNUSED = 5001;
    private static final int RC_ACHIEVEMENT_UI = 9003;
    static final int RC_SIGN_IN = 9001;

    private static final String TAG = GooglePlayHandler.class.getSimpleName();

    /* Activity that called this class */
    private Activity mParentActivity;

    /* Achievements/scores waiting to be pushed to the cloud since the user hasn't signed in yet */
    private final AchievementOutbox mOutbox = new AchievementOutbox();

    /**
     * Initializes Google API stuff
     *
     * @param activity reference to the parent/calling activity
     */
    GooglePlayHandler(Activity activity) {
        mParentActivity = activity;

        /* Create client for Google Services sign in */
        mGoogleSignInClient = GoogleSignIn.getClient(mParentActivity,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        Log.d(TAG, " ===PlayHandler initialized===");
    }

    /**
     * Should only be called via parent activity's onResume function or upon initialization
     */

    void onActivityResult(Intent intent) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);

        if (result.isSuccess()) {
            GoogleSignInAccount signInResult = result.getSignInAccount();
            onConnected(signInResult);
        }
        else {
            int responseCode = result.getStatus().getStatusCode();
            String message;

            switch (responseCode) {
                case DEVELOPER_ERROR:
                    message = "Developer Error! Contact the developer of this app and ask them"
                            + "to look at their API stuff.";
                    break;
                case INTERNAL_ERROR:
                    message = "Some internal error happened when signing in; retrying should"
                            + "fix things.";
                    break;
                case NETWORK_ERROR:
                    message = "Some network error happened when signing in; retrying should"
                            + "fix things.";
                    break;
                case TIMEOUT:
                    message = "Timed out! Response from the server took too long, try again later";
                    break;
                case API_NOT_CONNECTED:
                    message = "Google Play's API failed to connect! Perhaps your device isn't"
                            + "supported...";
                    break;
                default:
                    message = "Some unknown error occurred; response code was: " + responseCode;
                    break;
            }

            onDisconnected();

            new AlertDialog.Builder(mParentActivity).setMessage(message)
                    .setNeutralButton(android.R.string.ok, null).show();
        }
    }

    void startInteractiveSignIn() {
        Log.d(TAG, " interactive sign in requested...");
        mParentActivity.startActivityForResult(mGoogleSignInClient.getSignInIntent(),
                RC_SIGN_IN);
    }

    void signInSilently() {
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(mParentActivity,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            GoogleSignInAccount signInAccount = task.getResult();
                            onConnected(signInAccount);
                        }
                        else {
                            startInteractiveSignIn();
                        }
                    }
                });
    }

    private void signOut() {
        if (!isSignedIn()) {
            return;
        }

        mGoogleSignInClient.signOut().addOnCompleteListener(mParentActivity,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        boolean success = task.isSuccessful();
                        Log.d(TAG, "signOut(): " + (success ? "success" : "failed"));
                        onDisconnected();
                    }
                });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "API connected");

        mAchievementsClient = Games.getAchievementsClient(mParentActivity, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(mParentActivity, googleSignInAccount);
        mEventsClient = Games.getEventsClient(mParentActivity, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(mParentActivity, googleSignInAccount);

        if (!mOutbox.isEmpty()) {
            Toast.makeText(mParentActivity, "Your progress will be uploaded",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void onDisconnected() {
        Log.d(TAG, "API disconnected");

        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mEventsClient = null;
        mPlayersClient = null;
    }

    @JavascriptInterface
    public void unlockAchievement(String achievementIdAsString) {
        if (mAchievementsClient != null) {
            mAchievementsClient.unlock(achievementIdAsString);
        }
    }

    @JavascriptInterface
    public void incrementAchievementByAmount(String achievementIdAsString, int amount) {
        if (mAchievementsClient != null) {
            mAchievementsClient.increment(achievementIdAsString, amount);
        }
    }

    @JavascriptInterface
    public void showAchievementWindow() {
        Log.d(TAG, "attempting to open achievement window...");
        if (mAchievementsClient != null) {
            mAchievementsClient.getAchievementsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            mParentActivity.startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                        }
                    });
        }
        else {
            Log.d(TAG, "mAchievementsClient was null");
        }
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(mParentActivity) != null;
    }

    private class AchievementOutbox {
        /*
        * add boolean flags for achievements or ints for scores if needed
        */

        boolean isEmpty() { return true; }
    }
}
