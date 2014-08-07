package org.jboss.errai.ioc.async.test.beanmanager.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;


// @EntryPoint
@Singleton
public class SomeSingleton {
    
    public static int instances = 0;
    
    @PostConstruct
    void onPostrConstructed() {
        instances++;
        AsyncBeanManager asyncBeanManager = IOC.getAsyncBeanManager();
        asyncBeanManager.lookupBean(SomeOtherSingleton.class).getInstance(new CreationalCallback<SomeOtherSingleton>() {
            
            @Override
            public void callback(SomeOtherSingleton beanInstance) {
                // TODO Auto-generated method stub
                
            }
        });
        
    }
}
