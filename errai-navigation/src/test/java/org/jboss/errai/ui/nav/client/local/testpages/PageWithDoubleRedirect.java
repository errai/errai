package org.jboss.errai.ui.nav.client.local.testpages;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ui.nav.client.local.Page;

import javax.enterprise.context.ApplicationScoped;

/**
 * Simple test page for tracing lifecycle methods.
 *
 * @author Johannes Barop <jb@barop.de>
 */
@ApplicationScoped @Page
public class PageWithDoubleRedirect extends BasePageForLifecycleTracing {

  public Class<? extends Widget> secondRedirectPage;

  @Override
  protected void doRedirect() {
    super.doRedirect();
    navigation.goTo(secondRedirectPage, ImmutableMultimap.<String, String>of());
  }

}
