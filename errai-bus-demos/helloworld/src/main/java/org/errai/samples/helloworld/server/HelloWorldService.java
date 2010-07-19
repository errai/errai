package org.errai.samples.helloworld.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.util.SimpleMessage;
import org.jboss.errai.bus.server.annotations.ApplicationComponent;
import org.jboss.errai.bus.server.annotations.Service;


@ApplicationComponent
public class HelloWorldService {

    @Inject
    public RequestDispatcher dispatcher;

    public void helloWorld(@Service("HelloWorldService") Message message) {
        SimpleMessage.send(message, "Hello, World");
    }
}
