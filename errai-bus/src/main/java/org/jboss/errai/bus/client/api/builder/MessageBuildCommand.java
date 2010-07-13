package org.jboss.errai.bus.client.api.builder;

/**
 * This interface, <tt>MessageBuildCommand</tt>, is a template for building the command part of a message.
 * This ensures that the call is constructed properly
 */
public interface MessageBuildCommand<R> extends MessageBuildParms<R> {

    /**
     * Sets the command for the message, and returns an instance of <tt>MessageBuildParms</tt>, which needs to be
     * constructed following setting the command
     *
     * @param command - the command to set for this message.
     *                Could be one of {@link org.jboss.errai.bus.client.protocols.BusCommands}
     * @return an instance of <tt>MessageBuildParms</tt>
     */
    public MessageBuildParms<R> command(Enum command);

    /**
     * Sets the command for the message, and returns an instance of <tt>MessageBuildParms</tt>, which needs to be
     * constructed following setting the command
     *
     * @param command - the command to set for this message.
     * @return an instance of <tt>MessageBuildParms</tt>
     */
    public MessageBuildParms<R> command(String command);

    /**
     * If <tt>signalling</tt> is called, the service is only signalled as opposed to sending a specific command.
     *
     * @return an instance of <tt>MessageBuildParms</tt>
     */
    public MessageBuildParms<R> signalling();


}
