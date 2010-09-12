package org.jboss.errai.common.client.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<Object, List<UnsatisfiedForwardLookup>> getUnsatisfiedDependencies() {
        return unsatisfiedDependencies;
    }
}
