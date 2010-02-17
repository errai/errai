package org.jboss.errai.bus.client.api;


public interface ErrorCallback {
    /**
     * Called when an error occurs on the bus.
     * @param message The message for which the failure occured
     * @param throwable  The exception thrown
     * @return boolean indicating whether or not the default error handling should be performed.
     */
    public boolean error(Message message, Throwable throwable);
}
