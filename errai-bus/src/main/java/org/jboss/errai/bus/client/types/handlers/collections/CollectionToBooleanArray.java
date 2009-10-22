package org.jboss.errai.bus.client.types.handlers.collections;

import org.jboss.errai.bus.client.types.TypeHandler;

import java.util.Collection;

public class CollectionToBooleanArray implements TypeHandler<Collection, Boolean[]> {
    public Boolean[] getConverted(Collection in) {
        if (in == null) return null;
        Boolean[] newArray = new Boolean[in.size()];

        int i = 0;
        for (Object o : in) {
           newArray[i++] = (Boolean) o; 
        }

        return newArray;
    }
}