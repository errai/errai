package org.jboss.errai.ioc.async.test.beanmanager.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class SomeOtherSingleton {
    
    @Inject
    private SomeOtherOtherSingleton singleton;
    
    public static int instances;
    
    @PostConstruct
    void onPostcontructed() {
        instances++;
    }
}
