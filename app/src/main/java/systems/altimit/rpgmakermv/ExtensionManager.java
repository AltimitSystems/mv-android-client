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

package systems.altimit.rpgmakermv;

import android.content.Context;
import android.content.Intent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by felixjones on 24/02/2018.
 */
public class ExtensionManager {

    public static List<Extension> getExtensions(Context context) {
        List<Extension> extensions = new ArrayList<>();

        try {
            loadExtensions(context, extensions);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return extensions;
    }

    private static void loadExtensions(Context context, List<Extension> extensions) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        for (String extension : BuildConfig.EXTENSIONS) {
            extensions.add(new Extension(Class.forName(extension + ".Extension").getConstructor(Context.class).newInstance(context)));
        }
    }

    /**
     *
     */
    public static class Extension {

        private Map<String, Object> mJavascriptInterfaces;
        private String[] mJavascriptSources;
        private int[] mRequestCodes;
        private Object mResponder;

        private Extension(Object instance) {
            mJavascriptInterfaces = new HashMap<>();
            mJavascriptSources = new String[] {};
            mRequestCodes = new int[] {};
            mResponder = instance;

            try {
                loadJavascriptInterfaces(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                loadJavascriptSources(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                loadActivityRequestCodes(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Map<String, Object> getJavascriptInterfaces() {
            return mJavascriptInterfaces;
        }

        public String[] getJavascriptSources() {
            return mJavascriptSources;
        }

        public boolean respondsToRequestCode(int activityRequestCode) {
            if (mRequestCodes == null) {
                return false;
            }
            for (int code : mRequestCodes) {
                if (code == activityRequestCode) {
                    return true;
                }
            }
            return false;
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            try {
                Method onActivityResult = mResponder.getClass().getMethod("onActivityResult", Integer.TYPE, Integer.TYPE, Intent.class);
                onActivityResult.invoke(mResponder, requestCode, resultCode, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void loadJavascriptInterfaces(Object instance) throws InvocationTargetException, IllegalAccessException {
            try {
                Method getJavascriptInterfaces = instance.getClass().getMethod("getJavascriptInterfaces");
                //noinspection unchecked
                mJavascriptInterfaces = (Map<String, Object>) getJavascriptInterfaces.invoke(instance);
            } catch (NoSuchMethodException e) {
                // Ignore NoSuchMethodException
            }
        }

        private void loadJavascriptSources(Object instance) throws InvocationTargetException, IllegalAccessException {
            try {
                Method getJavascriptSources = instance.getClass().getMethod("getJavascriptSources");
                //noinspection unchecked
                mJavascriptSources = (String[]) getJavascriptSources.invoke(instance);
            } catch (NoSuchMethodException e) {
                // Ignore NoSuchMethodException
            }
        }

        private void loadActivityRequestCodes(Object instance) throws InvocationTargetException, IllegalAccessException {
            try {
                Method getRequestCodes = instance.getClass().getMethod("getRequestCodes");
                //noinspection unchecked
                mRequestCodes = (int[]) getRequestCodes.invoke(instance);
            } catch (NoSuchMethodException e) {
                // Ignore NoSuchMethodException
            }
        }

    }

}
