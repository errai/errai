package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;


import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
public class SingletonBean {
    
    @PostConstruct
    private void postConstr() {
        LazySingletonTestUtil.record(SingletonBean.class.getName());
    }
}
