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

package systems.altimit.libandroidapi;

import android.content.Context;
import android.content.res.Resources;
import android.webkit.JavascriptInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import systems.altimit.libandroidapi.modules.AndroidFS;
import systems.altimit.libandroidapi.modules.AndroidPath;

/**
 * Created by felixjones on 24/02/2018.
 */
public class Extension {

    private static final String INTERFACE_NAME = "__android_api";

    private Map<String, Object> mInterfaces;
    private List<String> mSources;

    private String mModuleFilename;

    public Extension(Context context) {
        mInterfaces = new HashMap<>();
        mSources = new ArrayList<>();

        mInterfaces.put(INTERFACE_NAME, this);
        mInterfaces.put(AndroidPath.INTERFACE_NAME, new AndroidPath());
        mInterfaces.put(AndroidFS.INTERFACE_NAME, new AndroidFS());

        Resources resources = context.getResources();
        mSources.add(Util.readInputStream(resources.openRawResource(R.raw.android_require)));
        mSources.add(Util.readInputStream(resources.openRawResource(R.raw.android_path)));
        mSources.add(Util.readInputStream(resources.openRawResource(R.raw.android_fs)));

        mModuleFilename = context.getFilesDir().getAbsolutePath();
    }

    @JavascriptInterface
    public String mainModuleFilename() {
        return mModuleFilename;
    }

    public Map<String, Object> getJavascriptInterfaces() {
        return mInterfaces;
    }

    public List<String> getJavascriptSources() {
        return mSources;
    }

}
