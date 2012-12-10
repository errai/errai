package org.jboss.errai.ui.nav.client.local.testpages;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.Transition;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Page(path="pwtA")
@Templated("#page")
public class PageWithTransitionA extends Composite {

  @Inject @DataField("linkToB") @Transition(PageB.class)
  public Anchor linkToB;

}
