/*
 * Copyright (c) 2017-2019 Altimit Community Contributors
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

import android.content.Context;
import android.view.View;

/**
 * Created by felixjones on 12/05/2017.
 */
public interface Player {

    void setKeepScreenOn();
    View getView();
    void loadUrl(String url);
    void addJavascriptInterface(Object object, String name);
    Context getContext();
    void loadData(String data);
    void evaluateJavascript(String script);
    void post(Runnable runnable);
    void removeJavascriptInterface(String name);
    void pauseTimers();
    void onHide();
    void resumeTimers();
    void onShow();
    void onDestroy();

}