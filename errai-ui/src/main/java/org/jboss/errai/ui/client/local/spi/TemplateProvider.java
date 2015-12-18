package org.jboss.errai.ui.client.local.spi;

import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * Implementations of this type are IOC-managed beans that can supply templates
 * at runtime (see {@link Templated#provider()}).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface TemplateProvider {

  /**
   * Constructs a template at runtime using the provided location and passes the
   * template's content to the provided rendering callback. Synchronous and
   * asynchronous implementations are supported.
   * 
   * @param location
   *          The location of the template (i.e. a URL or path to a file), must
   *          not be null.
   * @param renderingCallback
   *          The callback that will cause the template to get rendered, must
   *          not be null.
   */
  public void provideTemplate(final String location, final TemplateRenderingCallback renderingCallback);
}
