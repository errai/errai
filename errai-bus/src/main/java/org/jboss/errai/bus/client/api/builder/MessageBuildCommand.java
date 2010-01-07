package org.jboss.errai.bus.client.api.builder;

public interface MessageBuildCommand extends MessageBuild {
    public MessageBuildParms command(Enum command);

    public MessageBuildParms command(String command);

    public MessageBuildParms signalling();
}
