package org.jboss.errai.ui.nav.client.local.testpages;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;

import com.google.gwt.user.client.ui.Widget;

@Dependent
@Page
public class CircularRef1 extends Widget {
  @Inject private TransitionTo<CircularRef2> linkToCR2;

  // you would never do this in real life; this is just exposing the field for test purposes!
  public TransitionTo<CircularRef2> getLink() {
    return linkToCR2;
  }
}
