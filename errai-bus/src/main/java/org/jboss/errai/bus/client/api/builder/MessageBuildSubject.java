package org.jboss.errai.bus.client.api.builder;

public interface MessageBuildSubject extends MessageBuild {
    public MessageBuildCommand toSubject(String subject);
    public MessageBuildCommand subjectProvided();
}
