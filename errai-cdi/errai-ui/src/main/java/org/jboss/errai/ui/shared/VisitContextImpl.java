package org.jboss.errai.ui.shared;

public class VisitContextImpl implements VisitContext {

  private boolean complete;

  public boolean isVisitComplete() {
    return complete;
  }

  @Override
  public void setVisitComplete() {
    complete = true;
  }
}
