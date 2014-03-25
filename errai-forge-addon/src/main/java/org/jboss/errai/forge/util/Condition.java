package org.jboss.errai.forge.util;

public interface Condition<T> {

  public boolean isSatisfied(final T subject);
  
  public String getShortDescription();

}
