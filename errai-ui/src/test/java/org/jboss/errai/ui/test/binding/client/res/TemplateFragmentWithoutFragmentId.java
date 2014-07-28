package org.jboss.errai.ui.test.binding.client.res;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

@Templated
public class TemplateFragmentWithoutFragmentId extends Composite {

  @Inject
  @DataField
  private Anchor anchor;
}
