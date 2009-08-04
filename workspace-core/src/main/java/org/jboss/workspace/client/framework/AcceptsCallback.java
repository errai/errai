package org.jboss.workspace.client.framework;

/**
 * A class that implements this interface must be able to accept a callback.
 */
public interface AcceptsCallback {
    public static final String MESSAGE_OK = "OK";
    public static final String MESSAGE_CANCEL = "CANCEL";

    public void callback(Object message);
}
