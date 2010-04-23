package org.jboss.errai.bus.server.api;

/**
 * This interface, <tt>QueueSession</tt>, is a template for creating a queue session with the bus. In practice, the
 * <tt>QueueSession</tt> would be wrapped around an HTTP session. The purpose is to separate Errai from the Servlet API
 */
public interface QueueSession {

    /**
     * Gets the current session id
     *
     * @return the session id
     */
    public String getSessionId();

    /**
     * Returns true if the session is still valid
     *
     * @return false if the session is not valid
     */
    public boolean isValid();

    /**
     * Closes the session
     *
     * @return true if session was closed successfully
     */
    public boolean endSession();

    /**
     * Sets the attribute with the specified value.
     *
     * @param attribute - new or old attribute to set
     * @param value - new value for attribute
     */
    public void setAttribute(String attribute, Object value);

    /**
     * Gets an attribute, if it exists.
     *
     * @param type - the type of to cast the attribute's value to
     * @param attribute - the attribute's name
     * @param <T> - the type
     * @return the value of the attribute as the specified <tt>type</tt>
     */
    public <T> T getAttribute(Class<T> type, String attribute);

    /**
     * Returns true if the specified attribute exists
     *
     * @param attribute - the attribute to search for
     * @return true if it exists
     */
    public boolean hasAttribute(String attribute);

    /**
     * Removes the specified attribute
     *
     * @param attribute - the attribute to remove
     */
    public void removeAttribute(String attribute);

    /**
     * Register a listener to be fired when the session ends.
     * @param listener The listener to be registered
     */
    public void addSessionEndListener(SessionEndListener listener);
}
