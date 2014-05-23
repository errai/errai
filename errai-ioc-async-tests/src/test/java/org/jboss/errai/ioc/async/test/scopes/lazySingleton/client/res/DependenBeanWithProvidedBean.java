package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;


@Dependent
public class DependenBeanWithProvidedBean {
    
    
    @Inject
    ProvidedBean bean;
    
    /**
     * @return the bean
     */
    public ProvidedBean getBean() {
        return bean;
    }
    
    
}
