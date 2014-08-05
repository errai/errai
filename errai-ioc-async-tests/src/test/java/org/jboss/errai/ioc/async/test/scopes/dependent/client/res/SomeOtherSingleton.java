package org.jboss.errai.ioc.async.test.scopes.dependent.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Singleton;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOC;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;

@Singleton
public class SomeOtherSingleton {

  @PostConstruct
  void onPost() {
    Container.runAfterInit(new Runnable() {

      @Override
      public void run() {
        onContainerInit();
      }
    });
  }

  protected void onContainerInit() {
    IOC.getAsyncBeanManager()
            .lookupBean(SomeDependendBeanWithSingleton.class)
            .getInstance(
                    new CreationalCallback<SomeDependendBeanWithSingleton>() {

                      @Override
                      public void callback(
                              SomeDependendBeanWithSingleton beanInstance) {
                        // TODO Auto-generated method stub

                      }
                    });

  }
}
