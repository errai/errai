package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;


@Dependent
public class DependendBean2WithSingleton {
    
    
    @Inject
    MyLazyBeanInterface bean;
    
    public MyLazyBeanInterface getLazySingletonBean() {
        return bean;
    }
    
    
    
}
