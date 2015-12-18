package org.jboss.errai.ui.nav.client.local;

import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithTransitionToSomeUniquePageRole;

@EntryPoint
public class TransitionToRoleTestApp {

  @Inject
  PageWithTransitionToSomeUniquePageRole testPage;

}
