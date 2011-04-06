package org.jboss.errai.bus.client.api;

/**
 * @author Mike Brock .
 */
public interface Consumer<T> {
    public void consume(T value);

    public void setToSubject(String subjectName);

    public void setReplyTo(String replyTo);
}
