package org.jboss.errai.bus.client.types.handlers.collections;

import org.jboss.errai.bus.client.types.TypeHandler;

import java.util.Collection;

public class CollectionToDoubleArray implements TypeHandler<Collection, Double[]> {
    public Double[] getConverted(Collection in) {
        if (in == null) return null;
        Double[] newArray = new Double[in.size()];

        int i = 0;
        for (Object o : in) {
           newArray[i++] =  ((Number)o).doubleValue();
        }

        return newArray;
    }
}