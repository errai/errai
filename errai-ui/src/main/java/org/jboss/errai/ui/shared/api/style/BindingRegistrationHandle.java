package org.jboss.errai.ui.shared.api.style;

import org.jboss.errai.ui.shared.api.annotations.style.StyleBinding;

/**
 * A handle that allows beans to clean up {@link StyleBinding style bindings}
 * when they are taken out of service.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface BindingRegistrationHandle {

  /**
   * Remove all bindings that were created by the method that returned this handle.
   */
  void cleanup();

}
