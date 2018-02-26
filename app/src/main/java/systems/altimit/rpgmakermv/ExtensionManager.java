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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import systems.altimit.clientapi.AbstractExtension;

/**
 * Created by felixjones on 24/02/2018.
 */
public class ExtensionManager {

    public static List<AbstractExtension> getExtensions(Context context) {
        List<AbstractExtension> extensions = new ArrayList<>();
        try {
            loadExtensions(context, extensions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extensions;
    }

    private static void loadExtensions(Context context, List<AbstractExtension> extensions) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        for (String extensionClass : BuildConfig.EXTENSION_CLASSES) {
            extensions.add((AbstractExtension) Class.forName(extensionClass).getConstructor(Context.class).newInstance(context));
        }
    }

}
