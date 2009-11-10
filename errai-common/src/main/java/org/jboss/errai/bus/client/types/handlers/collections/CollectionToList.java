package org.jboss.errai.bus.client.types.handlers.collections;

import org.jboss.errai.bus.client.types.TypeHandler;

import java.util.*;

public class CollectionToList implements TypeHandler<Collection, List> {
    public List getConverted(Collection in) {
        return new ArrayList(in);
    }
}