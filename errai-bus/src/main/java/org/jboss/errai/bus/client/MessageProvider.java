package org.jboss.errai.bus.client;

/**
 * This class allows different implementations of Message to be provided based on whether or not you're on the
 * client or server.
 */
public interface MessageProvider {

    /**
     * Gets the appropriate message
     *
     * @return the appropriate message
     */
    public Message get();
}
