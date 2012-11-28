package org.jboss.errai.ui.nav.client.local.testpages;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;

import com.google.gwt.user.client.ui.Widget;

@Dependent
@Page
public class CircularRef2 extends Widget {
  @Inject TransitionTo<CircularRef1> linkToCR1;

  // you would never do this in real life; this is just exposing the field for test purposes!
  public TransitionTo<CircularRef1> getLink() {
    return linkToCR1;
  }
}
