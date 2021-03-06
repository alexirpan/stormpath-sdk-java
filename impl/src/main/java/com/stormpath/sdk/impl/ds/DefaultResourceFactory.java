/*
 * Copyright 2012 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.sdk.impl.ds;

import com.stormpath.sdk.impl.util.ClassUtils;
import com.stormpath.sdk.resource.Resource;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since 0.1
 */
public class DefaultResourceFactory implements ResourceFactory {

    private InternalDataStore dataStore;

    private static final String BASE_PACKAGE = "com.stormpath.sdk.";
    private static final String IMPL_PACKAGE_NAME_FRAGMENT = "impl";
    private static final String IMPL_PACKAGE_NAME = IMPL_PACKAGE_NAME_FRAGMENT + ".";
    private static final String IMPL_CLASS_PREFIX = "Default";

    public DefaultResourceFactory(InternalDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public <T extends Resource> T instantiate(Class<T> clazz, Object... constructorArgs) {

        if (clazz == null) {
            throw new NullPointerException("Resource class cannot be null.");
        }

        Class<T> implClass = getImplementationClass(clazz);

        Object[] ctorArgs = createConstructorArgs(constructorArgs);

        Constructor<T> ctor;

        if (ctorArgs.length == 1) {
            ctor = ClassUtils.getConstructor(implClass, InternalDataStore.class);
        } else {
            ctor = ClassUtils.getConstructor(implClass, InternalDataStore.class, Map.class);
        }

        return ClassUtils.instantiate(ctor, ctorArgs);
    }

    private <T extends Resource> Class<T> getImplementationClass(Class<T> clazz) {
        if (clazz.isInterface()) {
            return convertToImplClass(clazz);
        }
        return clazz;
    }

    @SuppressWarnings("unchecked")
    private <T extends Resource> Class<T> convertToImplClass(Class<T> clazz) {
        String fqcn = clazz.getName();

        String afterBase = fqcn.substring(BASE_PACKAGE.length());
        //e.g. if interface is com.stormpath.sdk.account.Account, 'afterBase' is account.Account

        //split interface simple name and the remainder of the package structure:

        //if this is an impl-module-specific class, strip off the existing 'impl.' package:
        if (afterBase.startsWith(IMPL_PACKAGE_NAME)) {
            afterBase = afterBase.substring(IMPL_PACKAGE_NAME.length());
        }

        int index = afterBase.indexOf('.');
        String beforeImpl = afterBase.substring(0, index);

        String implFqcn = BASE_PACKAGE + IMPL_PACKAGE_NAME_FRAGMENT + "." +
                beforeImpl + "." + IMPL_CLASS_PREFIX + clazz.getSimpleName();

        return ClassUtils.forName(implFqcn);
    }

    private Object[] createConstructorArgs(Object[] existing) {
        int argsLength = (existing != null ? existing.length : 0);
        argsLength += 1; //account for the 'DataStore' instance that is required for every implementation.

        List<Object> args = new ArrayList<Object>(argsLength);
        args.add(this.dataStore); //always first arg
        if (existing != null) {
            Collections.addAll(args, existing);
        }

        return args.toArray();
    }
}
