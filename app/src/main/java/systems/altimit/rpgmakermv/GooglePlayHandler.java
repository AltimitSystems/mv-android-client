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
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

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
    void signInSilently() {
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(mParentActivity,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            onConnected(task.getResult());
                        }
                        else {
                            onDisconnected();
                        }
                    }
                });
    }

    void startInteractiveSignIn() {
        Log.d(TAG, " interactive sign in requested...");
        mParentActivity.startActivityForResult(mGoogleSignInClient.getSignInIntent(),
                GooglePlayHandler.RC_SIGN_IN);
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

    void onConnected(GoogleSignInAccount googleSignInAccount) {
        mAchievementsClient = Games.getAchievementsClient(mParentActivity, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(mParentActivity, googleSignInAccount);
        mEventsClient = Games.getEventsClient(mParentActivity, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(mParentActivity, googleSignInAccount);

        if (!mOutbox.isEmpty()) {
            Toast.makeText(mParentActivity, "Your progress will be uploaded",
                    Toast.LENGTH_LONG).show();
        }
    }

    void onDisconnected() {
        mAchievementsClient = null;
        mLeaderboardsClient = null;
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
