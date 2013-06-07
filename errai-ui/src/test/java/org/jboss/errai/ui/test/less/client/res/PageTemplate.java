package org.jboss.errai.ui.test.less.client.res;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * @author edewit@redhat.com
 */
@Templated
public class PageTemplate extends Composite {

  @DataField
  private Element box = DOM.createDiv();

  public Element getBox() {
    return box;
  }
}
