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

import org.xwalk.core.JavascriptInterface;

/**
 * Created by felixjones on 12/05/2017.
 */
public class PlayerHelper {

    public static Player create(Context context) {
        return new XWalkPlayerView(context).getPlayer();
    }

    /**
     *
     */
    public static abstract class Interface {

        protected abstract void onStart();
        protected abstract void onPrepare(boolean webgl, boolean webaudio, boolean showfps);

        @JavascriptInterface
        public void start() {
            onStart();
        }

        @JavascriptInterface
        public void prepare(boolean webgl, boolean webaudio, boolean showfps) {
            onPrepare(webgl, webaudio, showfps);
        }

    }

}