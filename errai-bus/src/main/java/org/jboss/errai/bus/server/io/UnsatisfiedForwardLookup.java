package org.jboss.errai.bus.server.io;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public final class UnsatisfiedForwardLookup {
    private String id;

    public UnsatisfiedForwardLookup(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
