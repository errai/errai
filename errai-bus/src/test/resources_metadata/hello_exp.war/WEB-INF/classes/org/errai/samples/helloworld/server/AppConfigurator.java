package org.errai.samples.helloworld.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.api.ErraiConfig;
import org.jboss.errai.bus.server.api.ErraiConfigExtension;

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

  public void configure(ErraiConfig config) {
    // provide extension points here
  }
}
