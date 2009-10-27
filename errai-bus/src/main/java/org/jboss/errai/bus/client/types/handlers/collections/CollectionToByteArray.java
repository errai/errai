package org.jboss.errai.bus.client.types.handlers.collections;

import org.jboss.errai.bus.client.types.TypeHandler;

import java.util.Collection;

public class CollectionToByteArray implements TypeHandler<Collection, Byte[]> {
    public Byte[] getConverted(Collection in) {
        if (in == null) return null;
        Byte[] newArray = new Byte[in.size()];

        int i = 0;
        for (Object o : in) {
           newArray[i++] = ((Number)o).byteValue();
        }

        return newArray;
    }
}