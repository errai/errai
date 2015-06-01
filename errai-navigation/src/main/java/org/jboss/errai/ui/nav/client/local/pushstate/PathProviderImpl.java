package org.jboss.errai.ui.nav.client.local.pushstate;

/**
 * Path provider implementation which defines a path prefix.
 */
public class PathProviderImpl implements PathProvider {

  @Override
  public final String getPathPrefix() {
    return "/";
  }
}
