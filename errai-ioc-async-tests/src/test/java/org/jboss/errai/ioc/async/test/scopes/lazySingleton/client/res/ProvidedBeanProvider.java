package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;


import javax.inject.Provider;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.IOCProvider;

@Singleton
@IOCProvider
public class ProvidedBeanProvider implements Provider<ProvidedBean> {
    
    
    @Override
    public ProvidedBean get() {
        return new ProvidedBean() {};
    }
    
}
