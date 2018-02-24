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

import android.webkit.JavascriptInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by felixjones on 24/02/2018.
 */
public class AndroidFS {

    public static final String INTERFACE_NAME = "__android_api_fs";

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
