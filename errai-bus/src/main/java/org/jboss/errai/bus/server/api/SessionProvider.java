package org.jboss.errai.bus.server.api;

/**
 * This interface, <tt>SessionProvider</tt>, is a template for creating a session provider based on the type of
 * session specified
 *
 */
public interface SessionProvider<T> {

    /**
     * Gets an instance of <tt>QueueSession</tt> using the external session reference given.
     *
     * @param externSessRef - the external session reference
     * @return an instance of <tt>QueueSession</tt>
     */
    public QueueSession getSession(T externSessRef, String remoteQueueId);
}
