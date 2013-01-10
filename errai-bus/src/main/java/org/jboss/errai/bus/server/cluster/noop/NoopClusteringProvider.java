package org.jboss.errai.bus.server.cluster.noop;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.cluster.ClusteringProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Brock
 */
public class NoopClusteringProvider implements ClusteringProvider {
  private static Logger log = LoggerFactory.getLogger(NoopClusteringProvider.class);

  public NoopClusteringProvider() {
    log.info("clustering support not configured.");
  }

  @Override
  public void clusterTransmit(String sessionId, String subject, String messageId) {
  }

  @Override
  public void clusterTransmitGlobal(Message message) {
  }
}
