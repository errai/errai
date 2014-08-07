package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;


@Dependent
public class DependendBeanWithSingleton {
    
    @Inject
    SingletonBean bean2;
    
    @Inject
    MyLazyBeanInterface bean;
   
    public MyLazyBeanInterface getLazySingletonBean() {
        return bean;
    }
    
 
    public SingletonBean getBean2() {
        return bean2;
    } 
    
}
