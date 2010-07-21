/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.rebind;

import org.mvel2.util.StringAppender;

import java.lang.reflect.Method;

public class RebindUtils {
    public static String createCallSignature(Method m) {
        StringAppender append = new StringAppender(m.getName()).append(':');
        for (Class c : m.getParameterTypes()) {
            append.append(c.getCanonicalName()).append(':');
        }
        return append.toString();
    }

    public static boolean isMethodInInterface(Class iface, Method member) {
        try {
            if (iface.getMethod(member.getName(), member.getParameterTypes()) != null) return true;
        }
        catch (NoSuchMethodException e) {
        }
        return false;
    }
}
