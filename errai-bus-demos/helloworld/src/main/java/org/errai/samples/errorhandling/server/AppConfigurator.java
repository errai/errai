package org.errai.samples.errorhandling.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.api.ErraiConfigExtension;

import java.util.Map;

/**
 * Create a config extension class so we can do things like setup the default tables
 * when the application is deployed, etc.
 */
@ExtensionComponent
public class AppConfigurator implements ErraiConfigExtension {

    private MessageBus bus;

    @Inject
    public AppConfigurator(MessageBus bus) {
        this.bus = bus;
    }

    public void configure(Map<Class<?>, ResourceProvider> bindings, Map<String, ResourceProvider> resourceProviders) {
        // provide extension points here
    }
}
