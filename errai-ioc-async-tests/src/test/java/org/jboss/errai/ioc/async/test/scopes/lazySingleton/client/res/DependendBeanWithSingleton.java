package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;


@Dependent
public class DependendBeanWithSingleton {
    
    @Inject
    SingletonBean bean2;
    
    @Inject
    MyLazyBeanInterface bean;
    
    
    /**
     * @return the bean
     */
    public MyLazyBeanInterface getLazySingletonBean() {
        return bean;
    }
    
    
    
    /**
     * @return the bean2
     */
    public SingletonBean getBean2() {
        return bean2;
    }
    
    
}
