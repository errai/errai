package org.jboss.errai.common.client.webworkers;

/**
 * @author Mike Brock
 */
public interface WorkerMessageListener<T> {
  public void onMessage(WorkerMessage<T> workerMessage);
}
