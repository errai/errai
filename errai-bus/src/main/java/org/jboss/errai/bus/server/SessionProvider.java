package org.jboss.errai.bus.server;

/**
 * This interface, <tt>SessionProvider</tt>, is a template for creating a session provider based on the type of
 * session specified
 *
 * @param <T> - the type of session to create the provider for
 */
public interface SessionProvider<T> {

    /**
     * Gets an instance of <tt>QueueSession</tt> using the external session reference given.
     *
     * @param externSessRef - the external session reference
     * @return an instance of <tt>QueueSession</tt>
     */
    public QueueSession getSession(T externSessRef);
}
