package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.ResourceProvider;

/**
 * @author Mike Brock .
 */
public interface Conversation {
    public void setValue(Object value);

    /**
     * Sets a Message part to the specified value.
     *
     * @param part  - The <tt>String</tt> name of the message part
     * @param value - the value to set the part to
     * @return the updated message
     */
    public void set(String part, Object value);

    /**
     * Sets a Message part to the specified value.
     *
     * @param part  - The <tt>Enum</tt> representation of the message part
     * @param value - the value to set the part to
     * @return the updated message
     */
    public void set(Enum<?> part, Object value);

    /**
     * @param part
     * @param provider
     * @return
     */
    public void setProvidedPart(String part, ResourceProvider provider);

    /**
     * @param part
     * @param provider
     * @return
     */
    public void setProvidedPart(Enum<?> part, ResourceProvider provider);

    public void reply();
}
