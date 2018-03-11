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
import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;

import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mgjus on 3/7/2018.
 */
public class AchievementsHandler extends AbstractHandler<AchievementsClient> {
    private static final int RC_ACHIEVEMENT_UI = 9003;

    private Map<String, Boolean> mAchievementCache;
    private Map<String, IncrementalAchievementShell> mIncrementalCache;

    private List<String> mAchievementsToUnlock;

    public AchievementsHandler(Activity activity) {
        super(activity);
        mAchievementCache = new HashMap<>();
        mIncrementalCache = new HashMap<>();

        mAchievementsToUnlock = new ArrayList<>();
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
            mAchievementCache.put(achievementId, true);
        } else if (!mAchievementCache.isEmpty()) {
            if (!mAchievementCache.get(achievementId)) {
                mAchievementCache.put(achievementId, true);
                mAchievementsToUnlock.add(achievementId);
            }
        }
    }

    @JavascriptInterface
    public void incrementAchievementStep(String achievementId, int amountToIncrement) {
        if (mClient != null) {
            mClient.increment(achievementId, amountToIncrement);
        } else if (!mAchievementCache.isEmpty()) {
            if (!mAchievementCache.get(achievementId)) {
                IncrementalAchievementShell shell = mIncrementalCache.get(achievementId);

                shell.currentSteps += amountToIncrement;

                if (shell.currentSteps >= shell.stepsToUnlock) {
                    mAchievementCache.put(achievementId, true);
                    mAchievementsToUnlock.add(achievementId);
                }
            }
        }
    }

    public void cacheAchievements() {
        mClient.load(true)
                .addOnCompleteListener(new OnCompleteListener<AnnotatedData<AchievementBuffer>>() {
            @Override
            public void onComplete(@NonNull Task<AnnotatedData<AchievementBuffer>> task) {
                AchievementBuffer achievementBuffer = task.getResult().get();

                List<Achievement> mAchievementList = new ArrayList<>();

                int bufferSize = achievementBuffer.getCount();

                for (int i = 0; i < bufferSize; i++) {
                    Achievement achievement = achievementBuffer.get(i);

                    mAchievementList.add(achievement);
                }

                achievementBuffer.release();
                sortAchievements(mAchievementList);
            }
        });
    }

    public void unlockCachedAchievements() {
        if ((mClient != null) && !mAchievementsToUnlock.isEmpty()) {
            for (String id : mAchievementsToUnlock) {
                mClient.unlock(id);
            }

            mAchievementsToUnlock.clear();
        }
    }

    private void sortAchievements(List<Achievement> achievementList) {
        for (Achievement achievement : achievementList) {
            String id = achievement.getAchievementId();
            boolean unlocked = (achievement.getState() == Achievement.STATE_UNLOCKED);

            mAchievementCache.put(id, unlocked);

            if (achievement.getType() == Achievement.TYPE_INCREMENTAL) {
                int steps = achievement.getCurrentSteps();
                int total = achievement.getTotalSteps();

                IncrementalAchievementShell shell = new IncrementalAchievementShell();
                shell.currentSteps = steps;
                shell.stepsToUnlock = total;

                mIncrementalCache.put(id, shell);
            }
        }
    }

    private class IncrementalAchievementShell {
        int currentSteps = 0;
        int stepsToUnlock = 0;
    }
}
