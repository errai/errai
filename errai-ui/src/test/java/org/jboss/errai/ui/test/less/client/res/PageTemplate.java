package org.jboss.errai.ui.test.less.client.res;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.StyleDescriptor;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.DOM;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author edewit@redhat.com
 */
@Templated
@StyleDescriptor({ "/simple.less", "/simple-override.less" })
public class PageTemplate extends Composite {

  @DataField
  private Element box = DOM.createDiv();

  public Element getBox() {
    return box;
  }
}
