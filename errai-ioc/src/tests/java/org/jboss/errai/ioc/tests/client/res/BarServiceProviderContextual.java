package org.jboss.errai.ioc.tests.client.res;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.Provider;

/**
 * @author Mike Brock .
 */
@Provider
public class BarServiceProviderContextual implements ContextualTypeProvider<BarService> {
    public BarService provide(final Class[] typeargs) {
         return new BarService() {
             public Object get() {
                 return typeargs[0].getName();
             }
         };
    }
}
