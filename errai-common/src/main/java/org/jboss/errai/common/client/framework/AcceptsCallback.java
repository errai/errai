package org.jboss.errai.common.client.framework;

/**
 * A class that implements this interface must be able to accept a callback.
 */
public interface AcceptsCallback {
    public static final String MESSAGE_OK = "OK";
    public static final String MESSAGE_CANCEL = "CANCEL";

    /**
     * This is method called by the caller.
     * @param message The message being returned
     * @param data Any additional data (optional)
     */
    public void callback(Object message, Object data);
}
