package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;


import javax.annotation.PostConstruct;


public abstract class ProvidedBean {
    
    
    
    @PostConstruct
    void onPostruct() {
        LazySingletonTestUtil.record(ProvidedBean.class.getName());
    }
}
