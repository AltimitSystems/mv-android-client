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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;

import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.EventBuffer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mgjus on 3/7/2018.
 */
public class EventsHandler extends AbstractHandler<EventsClient> {
    public static final String INTERFACE_NAME = "__google_play_events";

    private Map<String, EventShell> mEventsCache;
    private Map<String, Long> mOfflineEventCache;

    private Gson gson;

    public EventsHandler(Activity activity) {
        super(activity);

        mEventsCache = new HashMap<>();
        mOfflineEventCache = new HashMap<>();

        gson = new GsonBuilder().serializeNulls().create();
    }

    @JavascriptInterface
    public String incrementEvent(String eventId, long amountToIncrement) {
        if (mClient != null) {
            mClient.increment(eventId, ((int) amountToIncrement));
        }

        if (!mEventsCache.isEmpty()) {
            EventShell shell = mEventsCache.get(eventId);

            if (shell != null) {
                shell.val += amountToIncrement;

                mEventsCache.put(eventId, shell);

                if (mOfflineEventCache.get(eventId) != null) {
                    mOfflineEventCache.put(eventId,
                            (mOfflineEventCache.get(eventId) + amountToIncrement));
                } else {
                    mOfflineEventCache.put(eventId, amountToIncrement);
                }

                return gson.toJson(shell);
            }
        }

        return null;
    }

    @JavascriptInterface
    public String getAllEventDataAsJSON() {
        return (!mEventsCache.isEmpty()) ? gson.toJson(mEventsCache.values().toArray()) : null;
    }

    public void cacheEvents(boolean forceReload) {
        mClient.load(forceReload)
                .addOnCompleteListener(new OnCompleteListener<AnnotatedData<EventBuffer>>() {
                    @Override
                    public void onComplete(@NonNull Task<AnnotatedData<EventBuffer>> task) {
                        if (task.isSuccessful()) {
                            EventBuffer eventBuffer = task.getResult().get();

                            int buffSize = eventBuffer.getCount();

                            for (int i = 0; i < buffSize; i++) {
                                Event event = eventBuffer.get(i).freeze();
                                EventShell shell = new EventShell(event);

                                mEventsCache.put(event.getEventId(), shell);
                            }

                            eventBuffer.release();
                        }
                    }
                });
    }

    public void incrementCachedEvents() {
        if (!mOfflineEventCache.isEmpty() && (mClient != null)) {
            for (Map.Entry<String, Long> entry : mOfflineEventCache.entrySet()) {
                mClient.increment(entry.getKey(), entry.getValue().intValue());
            }

            mOfflineEventCache.clear();
        }
    }

    private class EventShell {
        String id;
        String name;
        String desc;
        String formatedVal;
        Uri imageUri;
        long val;

        EventShell(Event event) {
            id = event.getEventId();
            name = event.getName();
            desc = event.getDescription();
            formatedVal = event.getFormattedValue();
            imageUri = event.getIconImageUri();
            val = event.getValue();
        }
    }
}
