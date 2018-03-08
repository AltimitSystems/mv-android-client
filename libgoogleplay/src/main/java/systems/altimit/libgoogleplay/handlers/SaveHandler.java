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

import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by mgjus on 3/8/2018.
 */
public class SaveHandler extends AbstractHandler<SnapshotsClient> {
    private static final int RC_SAVED_GAMES = 9009;

    public SaveHandler(Activity activity) {
        super(activity);
    }

    public void showSavedGamesView() {
        if (mClient != null) {
            int maxNumSavesToShow = 5;
            Task<Intent> intentTask = mClient.getSelectSnapshotIntent("See My Saves",
                    false, true, maxNumSavesToShow);

            intentTask.addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    mParentActivity.startActivityForResult(intent, RC_SAVED_GAMES);
                }
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

    }
}
