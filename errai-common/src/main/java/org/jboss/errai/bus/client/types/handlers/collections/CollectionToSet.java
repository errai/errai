package org.jboss.errai.bus.client.types.handlers.collections;

import org.jboss.errai.bus.client.types.TypeHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CollectionToSet implements TypeHandler<Collection, Set> {
    public Set getConverted(Collection in) {
        return new HashSet(in);
    }
}