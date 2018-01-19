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

import android.net.Uri;
import android.webkit.JavascriptInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by felixjones on 19/01/2018.
 */
public class AndroidInterface {

    private Player mPlayer;

    public AndroidInterface(Player player) {
        mPlayer = player;
    }

    @JavascriptInterface
    public String mainModuleFilename() {
        return mPlayer.getContext().getFilesDir().getAbsolutePath();
    }

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

    @JavascriptInterface
    public String fsExistsSync(String path) {
        return new File(path).exists() ? "true" : "false";
    }

    @JavascriptInterface
    public void fsMkdirSync(String path) {
        new File(path).mkdirs();
    }

    @JavascriptInterface
    public void fsWriteFileSync(String file, String data) {
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(new File(file)), "UTF-8");
            osw.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @JavascriptInterface
    public String fsReadFileSync(String path) {
        StringBuilder text = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(path)));
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

    @JavascriptInterface
    public void fsUnlinkSync(String path) {
        new File(path).delete();
    }

}
