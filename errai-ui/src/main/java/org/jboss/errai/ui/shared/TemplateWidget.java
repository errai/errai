package org.jboss.errai.ui.shared;

import java.util.Collection;
import java.util.Iterator;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Used to merge a {@link Template} onto a {@link Composite} component.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class TemplateWidget extends Panel {
  private Collection<Widget> children;

  public TemplateWidget(Element root, Collection<Widget> children) {
    this.setElement(root);
    this.children = children;
    
    for (Widget child : children) {
      setParentNative(this, child);
    }
  }

  private static native void setParentNative(Widget parent, Widget field) /*-{
		field.@com.google.gwt.user.client.ui.Widget::setParent(Lcom/google/gwt/user/client/ui/Widget;)(parent);
  }-*/;

  @Override
  public Iterator<Widget> iterator() {
    return children.iterator();
  }

  @Override
  public boolean remove(Widget child) {
    if(child.getParent() != this)
    {
      return false;
    }
    orphan(child);
    child.getElement().removeFromParent();
    return children.remove(child);
  }

}
