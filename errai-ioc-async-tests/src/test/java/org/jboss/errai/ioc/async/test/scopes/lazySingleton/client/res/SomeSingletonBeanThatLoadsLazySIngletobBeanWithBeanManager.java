package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOC;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;

@EntryPoint
public class SomeSingletonBeanThatLoadsLazySIngletobBeanWithBeanManager implements Runnable {

  public static int instances;

  @PostConstruct
  void onPostConstruct() {
    instances++;
    //calling bean manager directly causes error in test right now. so thats a workaround
     Container.$(this);
  }

  @Override
  public void run() {
    IOC.getAsyncBeanManager()
    .lookupBean(SomeLazySingletonBeanForBeanManager.class)
    .getInstance(
            new CreationalCallback<SomeLazySingletonBeanForBeanManager>() {

              @Override
              public void callback(
                      SomeLazySingletonBeanForBeanManager beanInstance) {
                // TODO Auto-generated method stub

              }
            });
    
  }
}
