package org.jboss.errai.common.client.types;

import java.util.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DecodingContext {
    private Map<String, Object> objects = new HashMap<String, Object>();
    private Map<Object, List<UnsatisfiedForwardLookup>> unsatisfiedDependencies;

    public void putObject(String id, Object obj) {
        objects.put(id, obj);
    }

    public boolean hasObject(String id) {
        return objects.containsKey(id);
    }

    public Object getObject(String id) {
        return objects.get(id);
    }

    public void addUnsatisfiedDependency(Object instance, UnsatisfiedForwardLookup ufl) {
        if (unsatisfiedDependencies == null)
            unsatisfiedDependencies = new HashMap<Object, List<UnsatisfiedForwardLookup>>();

        List<UnsatisfiedForwardLookup> usls = unsatisfiedDependencies.get(instance);
        if (usls == null) unsatisfiedDependencies.put(instance, usls = new ArrayList<UnsatisfiedForwardLookup>());

        usls.add(ufl);
    }

    public boolean isUnsatisfiedDependencies() {
        return unsatisfiedDependencies != null && !unsatisfiedDependencies.isEmpty();
    }

    public Map<String, Object> getObjects() {
        return objects;
    }

    public boolean hasUnsatisfiedDependency(Object o) {
        return unsatisfiedDependencies != null && (o instanceof Map ? __lookup((Map) o) : unsatisfiedDependencies.containsKey(o));
    }

    private boolean __lookup(Map o) {
        for (Object o1 : unsatisfiedDependencies.keySet()) {
            if (o == o1) return true;
        }
        return false;
    }

    public void swapDepReference(Object oldRef, Object newRef) {
        List<UnsatisfiedForwardLookup> list;
        if (oldRef instanceof Map) {
            list = __get((Map) oldRef);
            __remove((Map) oldRef);
        }
        else {
           list =  unsatisfiedDependencies.remove(oldRef);
        }

        unsatisfiedDependencies.put(newRef, list);
    }

    private List<UnsatisfiedForwardLookup> __get(Map o) {
        for (Map.Entry<Object, List<UnsatisfiedForwardLookup>> entry : unsatisfiedDependencies.entrySet()) {
            if (entry.getKey() == o) return entry.getValue();
        }
        return null;
    }

    private void __remove(Map o) {
        Iterator<Object> iter = unsatisfiedDependencies.keySet().iterator();
        while (iter.hasNext()) {
            if (iter.next() == o) {
                iter.remove();
                return;
            }
        }
        return;
    }

    public Map<Object, List<UnsatisfiedForwardLookup>> getUnsatisfiedDependencies() {
        return unsatisfiedDependencies;
    }
}
