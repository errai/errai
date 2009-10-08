package org.jboss.errai.bus.client.types.handlers;

import org.jboss.errai.bus.client.types.TypeHandler;

import java.util.Collection;

public class CollectionToCharArray implements TypeHandler<Collection, Character[]> {
    public Character[] getConverted(Collection in) {
        if (in == null) return null;
        Character[] newArray = new Character[in.size()];

        int i = 0;
        for (Object o : in) {
           newArray[i++] = String.valueOf(o).charAt(0);
        }

        return newArray;
    }
}