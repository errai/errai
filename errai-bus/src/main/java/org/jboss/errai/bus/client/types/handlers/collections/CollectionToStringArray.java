package org.jboss.errai.bus.client.types.handlers.collections;

import org.jboss.errai.bus.client.types.TypeHandler;

import java.util.Collection;

public class CollectionToStringArray implements TypeHandler<Collection, String[]> {
    public String[] getConverted(Collection in) {
        if (in == null) return null;
        String[] newArray = new String[in.size()];

        int i = 0;
        for (Object o : in) {
           newArray[i++] = String.valueOf(o);
        }

        return newArray;
    }
}
