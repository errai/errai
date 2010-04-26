package org.jboss.errai.bus.client.api;

import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;

import java.io.Serializable;
import java.util.Map;

public interface Message extends Serializable {

    /**
     * Sets the subject of this message, which is the intended recipient, and returns the message
     *
     * @param subject - the intended recipent of the message
     * @return the updated message
     */
    public Message toSubject(String subject);

    /**
     * Returns the message's subject
     *
     * @return this messages subject, it's intended recipent
     */
    public String getSubject();

    /**
     * Set the command type for this message.
     * Command is an optional extension for creating services that can respond to different specific commands.
     *
     * @param type - <tt>String</tt> representation of a command type
     * @return the updated message
     */
    public Message command(String type);

    /**
     * Set the command type for this message.
     * Command is an optional extension for creating services that can respond to different specific commands.
     *
     * @param type - <tt>Enum</tt> representation of a command type
     * @return the updated message
     */
    public Message command(Enum type);

    /**
     * Returns the command type for this message as a <tt>String</tt>
     *
     * @return the command type
     */
    public String getCommandType();

    /**
     * Sets a Message part to the specified value.
     *
     * @param part - The <tt>String</tt> name of the message part
     * @param value - the value to set the part to
     * @return the updated message
     */
    public Message set(String part, Object value);

    /**
     * Sets a Message part to the specified value.
     *
     * @param part - The <tt>Enum</tt> representation of the message part
     * @param value - the value to set the part to
     * @return the updated message
     */
    public Message set(Enum part, Object value);


    public Message setProvidedPart(String part, ResourceProvider provider);

    public Message setProvidedPart(Enum part, ResourceProvider provider);

    /**
     * Check if message contains the specified part
     *
     * @param part - <tt>String</tt> part to check for
     * @return true if message contains part
     */
    public boolean hasPart(String part);

    /**
     * Check if message contains the specified part
     *
     * @param part - <tt>Enum</tt> part to check for
     * @return true if message contains part
     */
    public boolean hasPart(Enum part);

    /**
     * Removes specified part from the message
     *
     * @param part - part to remove
     */
    public void remove(String part);

    /**
     * Removes specified part from the message
     *
     * @param part - part to remove
     */
    public void remove(Enum part);

    /**
     * Copy a part of this message to another message
     *
     * @param part - the part of this message to copy
     * @param m - the message to copy the part to
     * @return this message
     */
    public Message copy(String part, Message m);

    /**
     * Copy a part of this message to another message
     *
     * @param part - the part of this message to copy
     * @param m - the message to copy the part to
     * @return this message
     */
    public Message copy(Enum part, Message m);

    /**
     * Set the message to contain the specified parts.  Note: This overwrites any existing message contents.
     *
     * @param parts - Parts to be used in the message.
     * @return this message
     */
    public Message setParts(Map<String,Object> parts);

    /**
     * Copy in a set of message parts from the provided map.
     *
     * @param parts - Parts to be added to the message.
     * @return this message
     */
    public Message addAllParts(Map<String, Object> parts);

    /**
     * Copy in a set of provided message parts from the provided maps
     * @param provided - provided parts to be added to the message
     * @return this message
     */
    public Message addAllProvidedParts(Map<String, ResourceProvider> provided);

    /**
     * Return a Map of all the specified parts.
     *
     * @return - a Map of the message parts.
     */
    public Map<String, Object> getParts();

    /**
     * Return a Map of all provided pars.
     * @return - a Map of the provided message parts.
     */
    public Map<String, ResourceProvider> getProvidedParts();

    /**
     * Add the Map of resources to the message.
     *
     * @param resources - Map of resource
     */
    public void addResources(Map<String, ?> resources);

    /**
     * Set a transient resource.  A resource is not transmitted beyond the current bus scope.  It can be used for
     * managing the lifecycle of a message within a bus.
     *
     * @param key - Name of resource
     * @param res - Instance of resouce
     * @return this message
     */
    public Message setResource(String key, Object res);

    /**
     * Obtain a transient resource based on the specified key.
     *
     * @param key - Name of resource.
     * @return - Instancee of resource.
     */
    public <T> T getResource(Class<T> type, String key);

    /**
     * Returns true if the specified transient resource is present.
     *
     * @param key - Name of resouce
     * @return - boolean value indicating if the specified resource is present in the message.
     */
    public boolean hasResource(String key);

    /**
     * Copy a transient resource to this mesage from the specified message.
     *
     * @param key      - Name of resource.
     * @param m - Message to copy from.
     * @return
     */
    public Message copyResource(String key, Message m);

    /**
     * Sets the error callback for this message
     *
     * @param callback - error callback
     * @return this
     */
    public Message errorsCall(ErrorCallback callback);

    /**
     * Gets the error callback for this message
     *
     * @return the error callback
     */
    public ErrorCallback getErrorCallback();

    /**
     * Get the specified message part in the specified type.  A <tt>ClassCastException</tt> is thrown if the value
     * cannot be coerced to the specified type.
     *
     * @param type - Type to be returned.
     * @param part - Message part.
     * @param <T>  - Type to be returned.
     * @return - Value in the specified type.
     */
    public <T> T get(Class<T> type, String part);

    /**
     * Get the specified message part in the specified type.  A <tt>ClassCastException</tt> is thrown if the value
     * cannot be coerced to the specified type.
     *
     * @param type - Type to be returned.
     * @param part - Message part.
     * @param <T>  - Type to be returned.
     * @return - Value in the specified type.
     */
    public <T> T get(Class<T> type, Enum part);

    /**
     * Set flags for this message
     *
     * @param flag - <tt>RoutingFlags</tt> can be set to NonGlobalRouting or PriorityProcessing
     */
    public void setFlag(RoutingFlags flag);

    /**
     * Unset flags for this message
     *
     * @param flag - <tt>RoutingFlags</tt> can be set to NonGlobalRouting or PriorityProcessing
     */
    public void unsetFlag(RoutingFlags flag);

    /**
     * Checks if a flag is setfor this message
     *
     * @param flag - <tt>RoutingFlags</tt> can be set to NonGlobalRouting or PriorityProcessing
     * @return true if the flag is set
     */
    public boolean isFlagSet(RoutingFlags flag);

    /**
     * Commit the message in it's current structure.  After this method is called, there is no guarantee that
     * any changes in the message will be communicated across the bus. In fact, modifying the message after
     * calling commit() may create a corrupt payload.  In theory, you should never call this method.  It's
     * called by the message bus immediately before transmission.
     */
    public void commit();

    /**
     * Transmit this message to the specified {@link MessageBus} instance.
     *
     * @param viaThis - <tt>MessageBus</tt> instance to send message to
     */
    public void sendNowWith(MessageBus viaThis);

    /**
     * Transmit this message using the specified {@link org.jboss.errai.bus.client.framework.RequestDispatcher}.
     *
     * @param viaThis - <tt>RequestDispatcher</tt> instance to send message to
     */
    public void sendNowWith(RequestDispatcher viaThis);

}
