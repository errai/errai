package org.jboss.errai.bus.server;

import org.jboss.errai.bus.server.cluster.jgroups.JGroupsClusteringProvider;
import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceFactory;

/**
 * @author Mike Brock
 */
public class InVMBusUtil {

  public static ErraiService startService(int portOffset) {

    final ErraiServiceConfigurator configurator = new ErraiServiceConfiguratorImpl();
    int port = ErraiConfigAttribs.CLUSTER_PORT.getInt(configurator) + portOffset;
    ErraiConfigAttribs.CLUSTER_PORT.set(configurator, String.valueOf(port));
    ErraiConfigAttribs.CLUSTERING_PROVIDER.set(configurator, JGroupsClusteringProvider.class.getName());
    ErraiConfigAttribs.AUTO_DISCOVER_SERVICES.set(configurator, "false");
    return ErraiServiceFactory.create(configurator);
  }
}
