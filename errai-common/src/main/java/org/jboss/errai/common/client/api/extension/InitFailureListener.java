package org.jboss.errai.common.client.api.extension;

import java.util.Set;

/**
 * An <tt>InitFailureListener</tt> is used to listen for components which do not initialize as part of the
 * framework bootstrap at runtime. Generally, framework components are bootstrapped based on class-based
 * topic ("org.jboss.errai.bus.client.framework.ClientMessageBus" for instance). The appearance of one of
 * these topics in the {@link Set} passed to the {@link #onInitFailure(java.util.Set)} method indicates
 * this component timed out and did not initialize.
 *
 * @author Mike Brock
 */
public interface InitFailureListener {

  /**
   * Called when an initialization failure occurs.
   *
   * @param failedTopics
   *     Represents a set of initialization topics which did not initialize. These are typically
   *     based on the fully-qualified class names of components.
   *     (e.g. "org.jboss.errai.bus.client.framework.ClientMessageBus)
   */
  public void onInitFailure(Set<String> failedTopics);
}
