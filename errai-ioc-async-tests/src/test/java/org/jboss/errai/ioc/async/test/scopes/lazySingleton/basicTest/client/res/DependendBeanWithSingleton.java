package org.jboss.errai.ioc.async.test.scopes.lazySingleton.basicTest.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;


@Dependent
public class DependendBeanWithSingleton {
    
    @Inject
    private
    SingletonBean bean2;
    
    @Inject
    private
    LazySingletonBean bean;

    public SingletonBean getBean2() {
      return bean2;
    }

    public LazySingletonBean getBean() {
      return bean;
    }    
}
