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
import android.graphics.Bitmap;
import android.webkit.JavascriptInterface;

import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by mgjus on 3/8/2018.
 */
public class SaveHandler extends AbstractHandler<SnapshotsClient> {
    private static final int RC_LIST_SAVED_GAMES = 9002;
    private static final int RC_SELECT_SNAPSHOT = 9003;
    private static final int RC_SAVE_SNAPSHOT = 9004;
    private static final int RC_LOAD_SNAPSHOT = 9005;
    private static final int RC_SAVED_GAMES = 9009;

    private SaveDataShell mSaveShell;
    private String mLoadedJSONString;

    private boolean mSnapshotPrepped = false;

    public SaveHandler(Activity activity) {
        super(activity);

        mSaveShell = new SaveDataShell();
    }

    @JavascriptInterface
    public void prepSaveGame(byte[] saveData, Bitmap image, String description) {
        mSaveShell.set(saveData, image, description);

        mSnapshotPrepped = true;
    }

    public void showSavedGamesView() {
        if (mClient != null) {
            int maxNumSavesToShow = 5;
            Task<Intent> intentTask = mClient.getSelectSnapshotIntent("See My Saves",
                    true, true, maxNumSavesToShow);

            intentTask.addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    mParentActivity.startActivityForResult(intent, RC_SAVED_GAMES);
                }
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RC_SAVED_GAMES) {
            if (intent != null) {
                if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                    SnapshotMetadata snapshotMetadata =
                            intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA);

                    // send data for load
                } else if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)) {

                }
            }
        }
    }

    private Task<SnapshotMetadata> writeSnapshot(Snapshot snapshot) {
        snapshot.getSnapshotContents().writeBytes(mSaveShell.data);

        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setCoverImage(mSaveShell.coverImage)
                .setDescription(mSaveShell.desc)
                .build();

        mSaveShell.clear();
        mSnapshotPrepped = false;

        return mClient.commitAndClose(snapshot, metadataChange);
    }

    private class SaveDataShell {
        byte[] data;
        Bitmap coverImage;
        String desc;

        SaveDataShell() {

        }

        void set(byte[] indata, Bitmap inimage, String indesc) {
            data = indata;
            coverImage = inimage;
            desc = indesc;
        }

        void clear() {
            data = null;
            coverImage = null;
            desc = null;
        }
    }
}
