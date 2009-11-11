package org.jboss.errai.common.client.types.handlers.collections;

import org.jboss.errai.common.client.types.TypeHandler;

import java.util.*;

public class CollectionToList implements TypeHandler<Collection, List> {
    public List getConverted(Collection in) {
        return new ArrayList(in);
    }
}