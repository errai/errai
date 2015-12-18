package org.jboss.errai.ui.test.nested.client.res;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * Part of the reconstruction of the failure case documented in ERRAI-464.
 */
@Templated
public class B extends Composite {
  @Inject @DataField private Label address;

  public Label getAddress() {
    return address;
  }
}
