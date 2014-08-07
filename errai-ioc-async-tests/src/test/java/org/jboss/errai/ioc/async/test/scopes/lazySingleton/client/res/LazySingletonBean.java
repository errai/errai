package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;


import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.LazySingleton;
import org.jboss.errai.ioc.client.api.LoadAsync;

@Singleton
@LazySingleton
@LoadAsync
public class LazySingletonBean implements MyLazyBeanInterface {
    
    
    @PostConstruct
    private void postConstr() {
        LazySingletonTestUtil.record(LazySingletonBean.class.getName());
    }
}
