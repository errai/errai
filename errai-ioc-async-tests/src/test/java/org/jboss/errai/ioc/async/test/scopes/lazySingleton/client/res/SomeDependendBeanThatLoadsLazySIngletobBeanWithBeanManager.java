package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Singleton;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;

@Dependent
public class SomeDependendBeanThatLoadsLazySIngletobBeanWithBeanManager {

  public static int instances;

  @PostConstruct
  void onPostConstruct() {
    instances++;
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      
      @Override
      public void execute() {
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
    });
  }
}
