package org.jboss.errai.bus.client.api;


/**
 * This interface indicates to the bus whether or not the Message being routed already contains a pre-built JSON
 * encoding. It is implemented by the <tt>JSONMessage</tt> class. The main purpose is to accelerate the performance
 * of the message building, so the bus does not need to deconstruct the message. Rather, it indicates that the
 * underlying message has already been constructed.
 */
public interface HasEncoded {

    /**
     * Gets the encoded JSON string
     *
     * @return the encoded JSON string
     */
    public String getEncoded();
}
