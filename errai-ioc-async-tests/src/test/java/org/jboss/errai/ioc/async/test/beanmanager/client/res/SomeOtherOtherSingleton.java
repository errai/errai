package org.jboss.errai.ioc.async.test.beanmanager.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
public class SomeOtherOtherSingleton {
    
    
    public static int instances;
    
    @PostConstruct
    void onPostConstructed() {
        instances++;
    }
}
