package org.jboss.errai.ui.test.path.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.path.client.res.PathAbsoluteComponent;
import org.jboss.errai.ui.test.path.client.res.PathRelativeComponent;
import org.jboss.errai.ui.test.path.client.res.PathRelativeParentComponent;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class PathTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private PathRelativeComponent relativeComponent;
  @Inject
  private PathRelativeParentComponent relativeParentComponent;
  @Inject
  private PathAbsoluteComponent absoluteComponent;

  @PostConstruct
  public void setup() {
    root.add(relativeComponent);
    root.add(absoluteComponent);
    System.out.println(root.getElement().getInnerHTML());
  }

  public PathRelativeComponent getRelativeComponent() {
    return relativeComponent;
  }

  public PathRelativeParentComponent getRelativeParentComponent() {
    return relativeParentComponent;
  }

  public PathAbsoluteComponent getAbsoluteComponent() {
    return absoluteComponent;
  }
}
