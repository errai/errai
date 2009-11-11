package org.jboss.errai.common.client.types.handlers.collections;

import org.jboss.errai.common.client.types.TypeHandler;

import java.util.Collection;

public class CollectionToObjArray implements TypeHandler<Collection, Object[]> {
    public Object[] getConverted(Collection in) {
        if (in == null) return null;
        Object[] newArray = new String[in.size()];

        int i = 0;
        for (Object o : in) {
           newArray[i++] = o;
        }

        return newArray;
    }
}