package org.jboss.errai.bus.client.types;

import org.jboss.errai.bus.client.types.handlers.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TypeHandlerFactory {
    private static Map<Class, Map<Class, TypeHandler>> handlers =
            new HashMap<Class, Map<Class, TypeHandler>>();

    private static Map<Class, Class> inheritanceMap = new HashMap<Class, Class>();

    static {
        Map<Class, TypeHandler> collectionHandlers = new HashMap<Class, TypeHandler>();
        collectionHandlers.put(Object[].class, new CollectionToObjArray());
        collectionHandlers.put(String[].class, new CollectionToStringArray());
        collectionHandlers.put(Integer[].class, new CollectionToIntArray());
        collectionHandlers.put(Long[].class, new CollectionToLongArray());
        collectionHandlers.put(Boolean[].class, new CollectionToBooleanArray());
        collectionHandlers.put(Double[].class, new CollectionToDoubleArray());

        handlers.put(Collection.class, collectionHandlers);

        /**
         * We can specifically discriminate on ArrayList pretty exclusively for now, because we
         * know the JSONDecoder always uses it for lists/arrays.
         */
        inheritanceMap.put(ArrayList.class, Collection.class);
    }

    public static Map<Class, TypeHandler> getHandler(Class from) {
        Map<Class, TypeHandler> toHandlers = handlers.get(from);
        if (toHandlers == null) {
            toHandlers = handlers.get(inheritanceMap.get(from));
        }
        return toHandlers;
    }

    public static Object convert(Class from, Class to, Object value) {
        Map<Class, TypeHandler> toHandlers = getHandler(from);
        if (toHandlers == null) throw new RuntimeException("Can't convert from: " + from.getName());
        TypeHandler handler = toHandlers.get(to);
        if (handler == null) throw new RuntimeException("Can't convert to: " + to.getName());
        return handler.getConverted(value);
    }
}
