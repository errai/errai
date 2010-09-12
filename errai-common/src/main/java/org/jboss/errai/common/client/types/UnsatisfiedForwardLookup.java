package org.jboss.errai.common.client.types;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public final class UnsatisfiedForwardLookup extends RuntimeException {
    private final String id;
    private String path;

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
}
