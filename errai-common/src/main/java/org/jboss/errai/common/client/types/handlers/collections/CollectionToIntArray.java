package org.jboss.errai.common.client.types.handlers.collections;

import org.jboss.errai.common.client.types.TypeHandler;

import java.util.Collection;

public class CollectionToIntArray implements TypeHandler<Collection, Integer[]> {
    public Integer[] getConverted(Collection in) {
        if (in == null) return null;
        Integer[] newArray = new Integer[in.size()];

        int i = 0;
        for (Object o : in) {
           newArray[i++] = ((Number)o).intValue();
        }

        return newArray;
    }
}