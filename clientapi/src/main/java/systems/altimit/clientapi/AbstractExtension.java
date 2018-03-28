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

package systems.altimit.clientapi;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.Map;

/**
 * Created by felixjones on 26/02/2018.
 */
public abstract class AbstractExtension {

    public AbstractExtension(@NonNull Context context) {}

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Do nothing by default
    }

    public void onResume() {
        // Do noting by default
    }

    public void onPause() {
        // Do noting by default
    }

    public void onStart() {
        // Do noting by default
    }

    public void onStop() {
        // Do noting by default
    }

    public void onDestroy() {
        // Do nothing by default
    }

    public void onRestart() {
        // Do nothing by default
    }

    public abstract Map<String, Object> getJavascriptInterfaces();

    public abstract String[] getJavascriptSources();

}
