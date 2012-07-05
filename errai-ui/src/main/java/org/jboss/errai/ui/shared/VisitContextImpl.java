package org.jboss.errai.ui.shared;

public class VisitContextImpl<T> implements VisitContextMutable<T> {

  private boolean complete;
  private T result;

  public boolean isVisitComplete() {
    return complete;
  }

  @Override
  public void setVisitComplete() {
    complete = true;
  }

  @Override
  public T getResult() {
    return result;
  }

  @Override
  public void setResult(T result) {
    this.result = result;
  }
  
}
