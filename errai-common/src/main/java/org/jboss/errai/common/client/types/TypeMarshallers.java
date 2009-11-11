package org.jboss.errai.common.client.types;

import java.util.HashMap;
import java.util.Map;

public class TypeMarshallers {
    private static final Map<String, Marshaller> classMap = new HashMap<String, Marshaller>();
    private static final Map<Class, Marshaller> marshallers = new HashMap<Class, Marshaller>();

    static {
        addMarshaller(java.sql.Date.class, new Marshaller<java.sql.Date>() {
            public String marshall(java.sql.Date object) {
                return String.valueOf(object.getTime());
            }
        });

        addMarshaller(java.util.Date.class, new Marshaller<java.util.Date>() {
            public String marshall(java.util.Date object) {
                return String.valueOf(object.getTime());
            }
        });
    }

    public static void addMarshaller(Class type, Marshaller d) {
        classMap.put(type.getName(), d);
        marshallers.put(type, d);
    }

    public static <T> Marshaller<T> getMarshaller(Class<? extends T> type) {
        return marshallers.get(type);
    }

    public static Marshaller getMarshaller(String type) {
        return classMap.get(type);
    }

    public static boolean hasMarshaller(Class type) {
        return marshallers.containsKey(type);
    }

    public static boolean hasMarshaller(String type) {
        return classMap.containsKey(type);
    }
}