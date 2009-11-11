package org.jboss.errai.common.client.types.handlers.collections;

import org.jboss.errai.common.client.types.TypeHandler;

import java.util.Collection;

public class CollectionToFloatArray implements TypeHandler<Collection, Float[]> {
    public Float[] getConverted(Collection in) {
        if (in == null) return null;
        Float[] newArray = new Float[in.size()];

        int i = 0;
        for (Object o : in) {
           newArray[i++] = ((Number)o).floatValue();
        }

        return newArray;
    }
}