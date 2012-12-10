package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A container that can hold a widget and show and hide itself on demand. The
 * appearance is based on (and depends on) the Twitter Bootstrap stylesheet
 * collection. It does not require jQuery or the Bootstrap jquery.popup.js
 * plugin (it is a GWT-based replacement for that plugin).
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Templated
@Dependent
public class PopoverContainer extends Composite {

  /**
   * This is the widget that contains the user-supplied title we show in the popover.
   */
  @DataField
  private DivElement popoverTitle = Document.get().createDivElement();

  /**
   * This is the widget that contains the user-supplied content we show in the popover.
   */
  @Inject @DataField
  private VerticalPanel popoverContent;

  /**
   * Positions the popover so that its arrow points at the centre of the given widget Makes the popover visible
   * @param positionNear
   */
  public void show(Widget positionNear) {
    getElement().getStyle().setDisplay(Display.BLOCK);
    getElement().getStyle().setLeft(positionNear.getAbsoluteLeft() + positionNear.getOffsetWidth(), Unit.PX);
    getElement().getStyle().setTop(
            positionNear.getAbsoluteTop() + positionNear.getOffsetHeight() / 2
            - getElement().getOffsetHeight() / 2, Unit.PX);
  }

  /**
   * Causes this popover to become invisible.
   */
  public void hide() {
    getElement().getStyle().setDisplay(Display.NONE);
  }

  /**
   * Sets the widget that will be displayed as the title of this popover,
   * replacing any existing title widget.
   */
  public void setTitleHtml(SafeHtml html) {
    popoverTitle.setInnerHTML(html.asString());
  }

  /**
   * Sets the widget that will be displayed as the title of this popover,
   * replacing any existing body widget.
   */
  public void setBodyWidget(Widget bodyWidget) {
    popoverContent.clear();
    popoverContent.add(bodyWidget);
  }

  /**
   * Adds this popover to the document so it can be made visible. This method is
   * called automatically when this bean is created.
   */
  @PostConstruct
  private void init() {
    RootPanel.get().add(this);
  }

  /**
   * Removes this popover from the document so it does not leak resources. This
   * method is called automatically when this bean is destroyed.
   */
  @PreDestroy
  private void destroy() {
    RootPanel.get().remove(this);
  }
}
