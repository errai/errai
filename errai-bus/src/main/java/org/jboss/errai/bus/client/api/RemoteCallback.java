package org.jboss.errai.bus.client.api;

/**
 * Interface for a remote callback that takes any specified type
 *
 * @param <T> - type of response the callback expects
 */
public interface RemoteCallback<T> {

    /**
     * Callback function dispatched when the message it is attached to has been sent
     *
     * @param response - response of any particular type
     */
    public void callback(T response);
}
