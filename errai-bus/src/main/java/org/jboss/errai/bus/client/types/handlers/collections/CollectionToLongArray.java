package org.jboss.errai.bus.client.types.handlers.collections;

import org.jboss.errai.bus.client.types.TypeHandler;

import java.util.Collection;

public class CollectionToLongArray implements TypeHandler<Collection, Long[]> {
    public Long[] getConverted(Collection in) {
        if (in == null) return null;
        Long[] newArray = new Long[in.size()];

        int i = 0;
        for (Object o : in) {
           newArray[i++] = ((Double)o).longValue();
        }

        return newArray;
    }
}