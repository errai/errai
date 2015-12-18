package org.jboss.errai.ui.shared;

public interface VisitContext<T> {

  void setVisitComplete();

  T getResult();

}
