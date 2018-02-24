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
import android.content.Intent;
import android.content.res.Resources;
import android.webkit.JavascriptInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import systems.altimit.libandroidapi.modules.AndroidFS;
import systems.altimit.libandroidapi.modules.AndroidPath;

/**
 * Created by felixjones on 24/02/2018.
 */
public class Extension {

    private static final String INTERFACE_NAME = "__android_api";

    private Map<String, Object> mInterfaces;
    private String[] mSources;

    private String mModuleFilename;

    public Extension(Context context) {
        mInterfaces = new HashMap<>();
        mInterfaces.put(INTERFACE_NAME, this);
        mInterfaces.put(AndroidPath.INTERFACE_NAME, new AndroidPath());
        mInterfaces.put(AndroidFS.INTERFACE_NAME, new AndroidFS());

        Resources resources = context.getResources();
        mSources = new String[] {
            readInputStream(resources.openRawResource(R.raw.android_require)),
            readInputStream(resources.openRawResource(R.raw.android_path)),
            readInputStream(resources.openRawResource(R.raw.android_fs))
        };

        mModuleFilename = context.getFilesDir().getAbsolutePath();
    }

    @JavascriptInterface
    public String mainModuleFilename() {
        return mModuleFilename;
    }

    public Map<String, Object> getJavascriptInterfaces() {
        return mInterfaces;
    }

    public String[] getJavascriptSources() {
        return mSources;
    }

    public int[] getRequestCodes() {
        return null; // Does not respond to request codes
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Does not respond to request codes
    }

    private static String readInputStream(InputStream inputStream) {
        StringBuilder text = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return text.toString();
    }

}
