#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.server;

import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.api.ErraiConfig;
import org.jboss.errai.bus.server.api.ErraiConfigExtension;

/**
 * Create a config extension class so we can do things like setup the default tables
 * when the application is deployed, etc.
 */
@ExtensionComponent
public class AppConfigurator implements ErraiConfigExtension
{
  public void configure(ErraiConfig config)
  {
    // provide extension points here
  }
}
