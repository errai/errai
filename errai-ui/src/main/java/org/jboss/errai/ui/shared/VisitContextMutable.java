package org.jboss.errai.ui.shared;

public interface VisitContextMutable<T> extends VisitContext<T> {

  void setResult(T result);

}
