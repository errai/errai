package org.jboss.errai.bus.server.cluster;

/**
 * @author Mike Brock
 */
public interface ClusteringProvider {
  /**
   * Sends an asynchronous message to the cluster to find if any of the clustered buses are aware of the specified
   * session and subject.
   *
   * @param sessionId
   *        the session ID requested.
   * @param subject
   *        the subject requested.
   */
  public void clusterTransmit(final String sessionId, final String subject, final String messageId);
}

