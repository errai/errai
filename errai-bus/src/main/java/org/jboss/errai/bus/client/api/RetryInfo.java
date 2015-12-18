package org.jboss.errai.bus.client.api;

/**
 * Contains information about a potential upcoming attempt to retry something.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public final class RetryInfo {

  public static final RetryInfo NO_RETRY = new RetryInfo(-1, 0);
  private long delayUntilNextRetry;
  private int retryCount;

  /**
   * Creates a new RetryInfo with the given settings.
   *
   * @param delayUntilNextRetry
   *          The amount of time, in milliseconds, before the action is retried.
   *          Negative values mean that no retry is planned.
   * @param retryCount
   *          The number of retries that have already been performed for this
   *          same action.
   */
  public RetryInfo(long delayUntilNextRetry, int retryCount) {
    this.delayUntilNextRetry = delayUntilNextRetry;
    this.retryCount = retryCount;
  }

  /**
   * Returns the amount of time, in milliseconds, before the action is retried.
   *
   * @return The amount of time, in milliseconds, before the action is retried.
   *         Negative values mean that no retry is planned.
   */
  public long getDelayUntilNextRetry() {
    return delayUntilNextRetry;
  }

  /**
   * Returns the number of retries that have already been performed for this
   * same action.
   *
   * @return The number of retries that have already been performed for this
   *         same action.
   */
  public int getRetryCount() {
    return retryCount;
  }

  @Override
  public String toString() {
    return "RetryInfo [delayUntilNextRetry=" + delayUntilNextRetry
            + ", retryCount=" + retryCount + "]";
  }
  
}
