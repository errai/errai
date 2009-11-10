package org.jboss.errai.bus.client.types;

import java.util.HashMap;
import java.util.Map;

public class TypeDemarshallers {
    private static final Map<String, Demarshaller> classMap = new HashMap<String, Demarshaller>();
    private static final Map<Class, Demarshaller> demarshallers = new HashMap<Class, Demarshaller>();

    public static void addDemarshaller(Class type, Demarshaller d) {
        classMap.put(type.getName(), d);
        demarshallers.put(type,d);
    }

    public static <T> Demarshaller<T> getDemarshaller(Class<? extends T> type) {
        return demarshallers.get(type);
    }

    public static Demarshaller getDemarshaller(String type) {
        return classMap.get(type);
    }

    public static boolean hasDemarshaller(Class type) {
        return demarshallers.containsKey(type);
    }

    public static boolean hasDemarshaller(String type) {
        return classMap.containsKey(type);
    }
}
