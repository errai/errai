package org.jboss.errai.bus.server;

/**
 * This interface, <tt>SessionControl</tt>, provides functions for checking up on a session
 */
public interface SessionControl {

    /**
     * Returns true if the current session is still valid
     *
     * @return true if session is valid, false otherwise
     */
    public boolean isSessionValid();

    /**
     * This function indicates activity on the session, so the session knows when the last time there was activity.
     * For example, <tt>MessageQueue</tt> relies on this function to figure out whether or not to timeout
     */
    public void activity();
}
