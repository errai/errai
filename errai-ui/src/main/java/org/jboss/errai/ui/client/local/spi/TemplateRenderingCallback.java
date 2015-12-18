package org.jboss.errai.ui.client.local.spi;

/**
 * Used by the IOC bootstrapper to asynchronously render templates constructed
 * at runtime (i.e. server-side templates).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface TemplateRenderingCallback {

  /**
   * Renders the provided template.
   * 
   * @param template
   *          The template content, must not be null.
   */
  public void renderTemplate(String template);
}
