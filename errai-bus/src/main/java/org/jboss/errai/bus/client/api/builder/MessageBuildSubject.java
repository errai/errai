package org.jboss.errai.bus.client.api.builder;

/**
 * This interface, <tt>MessageBuildSubject</tt>, is a template for setting the subject of a message. This ensures
 * that the message is constructed properly
 */
public interface MessageBuildSubject<R> extends MessageBuild {

    /**
     * Sets the subject/receipent of the message, and returns a <tt>MessageBuildCommand</tt>, which needs to be
     * constructed following setting the subject
     *
     * @param subject - the subject of the message
     * @return an instance of <tt>MessageBuildCommand</tt>
     */
    public MessageBuildCommand<R> toSubject(String subject);

    /**
     * If this function is set, there is no need for a subject to be set for this message. Just move on...
     *
     * @return an instance of <tt>MessageBuildCommand</tt>
     */
    public MessageBuildCommand<R> subjectProvided();
}
