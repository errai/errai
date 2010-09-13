package org.jboss.errai.common.client.types;

import java.util.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class EncodingContext {
    private Map<Object, String> alreadyEncoded;
    private Set<String> refs;
    private int escapeMode;

    public EncodingContext() {
        alreadyEncoded = new HashMap<Object, String>();
    }

    public boolean isEncoded(Object instance) {
        return alreadyEncoded.containsKey(instance);
    }

    public void markEncoded(Object o) {
        if (o instanceof Number || o instanceof Boolean || o instanceof Character) return;
        
        alreadyEncoded.put(o, String.valueOf(o.hashCode()));
    }

    public String markRef(Object o) {
        if (refs == null) refs = new HashSet<String>();
        String ref = alreadyEncoded.get(o);
        refs.add(ref);
        return ref;      
    }

    public Set<String> getRefs() {
        return refs;
    }

    public boolean isEscapeMode() {
        return escapeMode != 0;
    }

    public void setEscapeMode() {
        escapeMode++;
    }

    public void unsetEscapeMode() {
        escapeMode--;
    }
}
