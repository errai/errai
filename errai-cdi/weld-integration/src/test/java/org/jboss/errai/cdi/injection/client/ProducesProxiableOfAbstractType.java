package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class ProducesProxiableOfAbstractType {

  public static abstract class NotConcrete {
    protected boolean value;

    public abstract void setValueTrue();

    public boolean getValue() {
      return value;
    }
  }

  @Produces
  @ApplicationScoped
  public NotConcrete produceNotConcrete() {
    return new NotConcrete() {

      @Override
      public void setValueTrue() {
        value = true;
      }
    };
  }

}
