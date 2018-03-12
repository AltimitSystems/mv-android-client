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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;

import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mgjus on 3/7/2018.
 */
public class AchievementsHandler extends AbstractHandler<AchievementsClient> {
    public static final String INTERFACE_NAME = "__google_play_achievements";

    private static final int RC_ACHIEVEMENT_UI = 9003;

    private Map<String, AchievementShell> mAchievementCache;
    private List<String> mUnlockQueue;

    private Gson gson;

    public AchievementsHandler(Activity activity) {
        super(activity);
        mAchievementCache = new HashMap<>();
        mUnlockQueue = new ArrayList<>();

        gson = new GsonBuilder().serializeNulls().create();
    }

    @JavascriptInterface
    public void showAchievementView() {
        if (mClient != null) {
            mClient.getAchievementsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            mParentActivity.startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                        }
                    });
        }
    }

    @JavascriptInterface
    public void unlockAchievement(String achievementId) {
        if (mClient != null) {
            mClient.unlock(achievementId);
        } else if (!mAchievementCache.isEmpty()) {
            AchievementShell shell = mAchievementCache.get(achievementId);

            if (shell != null) {
                if (!shell.isUnlocked()) {
                    shell.state = Achievement.STATE_UNLOCKED;

                    mUnlockQueue.add(shell.id);

                    mAchievementCache.put(achievementId, shell);
                }
            }
        }
    }

    @JavascriptInterface
    public void incrementAchievementStep(String achievementId, int amountToIncrement) {
        if (mClient != null) {
            mClient.increment(achievementId, amountToIncrement);
        } else if (!mAchievementCache.isEmpty()) {
            AchievementShell shell = mAchievementCache.get(achievementId);

            if (shell != null) {
                if (shell.isIncremental() && !shell.isUnlocked()) {
                    shell.currentSteps += amountToIncrement;

                    mAchievementCache.put(achievementId, shell);

                    if (shell.currentSteps >= shell.stepsToUnlock) {
                        unlockAchievement(achievementId);
                    }
                }
            }
        }
    }

    @JavascriptInterface
    public String getAllAchievementDataAsJSON() {
        return (!mAchievementCache.isEmpty()) ? gson.toJson(mAchievementCache.values().toArray())
                : null;
    }

    @JavascriptInterface
    public String getAchievementDataAsJSON(String achievementId) {
        AchievementShell achievement = mAchievementCache.get(achievementId);

        return (achievement != null) ? gson.toJson(achievement) : null;
    }

    public void cacheAchievements(boolean forceReload) {
        mClient.load(forceReload)
                .addOnCompleteListener(new OnCompleteListener<AnnotatedData<AchievementBuffer>>() {
            @Override
            public void onComplete(@NonNull Task<AnnotatedData<AchievementBuffer>> task) {
                AchievementBuffer achievementBuffer = task.getResult().get();

                int bufferSize = achievementBuffer.getCount();

                for (int i = 0; i < bufferSize; i++) {
                    Achievement achievement = achievementBuffer.get(i).freeze();
                    AchievementShell shell = new AchievementShell(achievement);

                    mAchievementCache.put(shell.id, shell);
                }
                achievementBuffer.release();
            }
        });
    }

    public void unlockCachedAchievements() {
        if (!mUnlockQueue.isEmpty()) {
            for (String achievementId : mUnlockQueue) {
                unlockAchievement(achievementId);
            }
        }
    }

    private class AchievementShell {
        String id;
        String name;
        String desc;
        Uri    revealedImageUri;
        Uri    unlockedImageUri;
        int    state;
        int    type;
        int    currentSteps = 0;
        int    stepsToUnlock = 0;

        AchievementShell(Achievement achievement) {
            id = achievement.getAchievementId();
            name = achievement.getName();
            desc = achievement.getDescription();
            revealedImageUri = achievement.getRevealedImageUri();
            unlockedImageUri = achievement.getUnlockedImageUri();
            state = achievement.getState();
            type = achievement.getType();

            if (type == Achievement.TYPE_INCREMENTAL) {
                currentSteps = achievement.getCurrentSteps();
                stepsToUnlock = achievement.getTotalSteps();
            }
        }

        boolean isUnlocked() {
            return (type == Achievement.STATE_UNLOCKED);
        }

        boolean isRevealed() {
            return (type == Achievement.STATE_REVEALED);
        }

        boolean isIncremental() {
            return (type == Achievement.TYPE_INCREMENTAL);
        }
    }
}
