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

package systems.altimit.libandroidapi.modules;

import android.net.Uri;
import android.webkit.JavascriptInterface;

/**
 * Created by felixjones on 24/02/2018.
 */
public class AndroidPath {

    public static final String INTERFACE_NAME = "__android_api_path";

    @JavascriptInterface
    public String pathBasename(String path, String ext) {
        String basename = new Uri.Builder().appendEncodedPath(path).build().getLastPathSegment();
        int extensionIndex = basename.lastIndexOf(ext);
        return extensionIndex > -1 ? basename.substring(0, extensionIndex) : basename;
    }

    @JavascriptInterface
    public String pathDirname(String path) {
        return new Uri.Builder().appendEncodedPath(path).build().getPath();
    }

    @JavascriptInterface
    public String pathJoin(String... paths) {
        Uri.Builder builder = new Uri.Builder();
        for (String path : paths) {
            builder.appendEncodedPath(path);
        }
        return builder.build().toString();
    }

}
