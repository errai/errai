package org.jboss.errai.common.client.types;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public final class UnsatisfiedForwardLookup {
    private final String id;
    private String path;

    private Object key;
    private Object val;

    private DeferredBinder binder;

    public UnsatisfiedForwardLookup(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getVal() {
        return val;
    }

    public void setVal(Object val) {
        this.val = val;
    }

    public DeferredBinder getBinder() {
        return binder;
    }

    public void setBinder(DeferredBinder binder) {
        this.binder = binder;
    }
}
