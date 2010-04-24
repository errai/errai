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
