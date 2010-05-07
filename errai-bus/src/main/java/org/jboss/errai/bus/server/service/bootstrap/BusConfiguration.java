/* jboss.org */
package org.jboss.errai.bus.server.service.bootstrap;

import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;

/**
 * Set the default bus properties.
 * 
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 3, 2010
 */
class BusConfiguration implements BootstrapExecution
{
  public void execute(BootstrapContext context)
  {
    ErraiServiceConfigurator service = context.getConfig();
    context.getBus().configure(service);
  }
}
