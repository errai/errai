package org.jboss.errai.ui.shared;

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Used to merge a {@link Template} onto a {@link Composite} component.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class TemplateWidget extends Widget {
  private List<Widget> children;

  public TemplateWidget(Element root, List<Widget> children) {
    this.setElement(root);
    this.children = children;
  }

  @Override
  protected void doAttachChildren() {
    for (Widget child : children) {
      System.out.println("Attaching child widget: " + child);
      onAttachNative(child);
    }
    super.doAttachChildren();
  }

  @Override
  protected void doDetachChildren() {
    for (Widget child : children) {
      System.out.println("Detaching child widget: " + child);
      onDetachNative(child);
    }
    super.doDetachChildren();
  }

  private static native void onAttachNative(Widget widget) /*-{
		widget.@com.google.gwt.user.client.ui.Widget::onAttach();
  }-*/;

  private static native void onDetachNative(Widget widget) /*-{
		widget.@com.google.gwt.user.client.ui.Widget::onDetach();
  }-*/;
}
