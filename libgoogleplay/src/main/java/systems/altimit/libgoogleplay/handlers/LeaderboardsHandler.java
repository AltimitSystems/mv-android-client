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

package systems.altimit.libgoogleplay.handlers;

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by mgjus on 3/7/2018.
 */
public class LeaderboardsHandler extends AbstractHandler<LeaderboardsClient> {
    public static final String INTERFACE_NAME = "__google_play_leaderboards";

    private static final int RC_LEADERBOARD_UI = 9004;

    public LeaderboardsHandler(Activity activity) {
        super(activity);
    }

    @JavascriptInterface
    public void showLeaderboardView(String boardId) {
        if (mClient != null) {
            mClient.getLeaderboardIntent(boardId)
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            mParentActivity.startActivityForResult(intent, RC_LEADERBOARD_UI);
                        }
                    });
        }
    }

    @JavascriptInterface
    public void addScoreToLeaderboard(String boardId, int score) {
        if (mClient != null) {
            mClient.submitScore(boardId, score);
        }
    }
}
