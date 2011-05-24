package org.jboss.errai.ioc.tests.client.res;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock .
 */
@IOCProvider
public class BarServiceProviderContextual implements ContextualTypeProvider<BarService> {
    public BarService provide(final Class[] typeargs, Annotation... qualifiers) {
         return new BarService() {
             public Object get() {
                 return typeargs[0].getName();
             }
         };
    }
}
