package org.jboss.errai.ui.test.nested.client.res;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Part of the reconstruction of the failure case documented in ERRAI-464.
 */
@Templated
public class A extends Composite {
  @Inject @DataField private TextBox address;
  @Inject @DataField private B b;

  public TextBox getAddress() {
    return address;
  }

  public B getB() {
    return b;
  }
}
