package org.jboss.errai.common.client.webworkers;

/**
 * @author Mike Brock
 */
public class WorkerMessage<T> {
  final T payload;

  public WorkerMessage(T payload) {
    this.payload = payload;
  }

  public T getPayload() {
    return payload;
  }
}
