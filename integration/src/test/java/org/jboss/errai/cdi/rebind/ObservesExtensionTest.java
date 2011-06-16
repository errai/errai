package org.jboss.errai.cdi.rebind;

import java.lang.annotation.Annotation;

import javax.enterprise.event.Observes;

import org.jboss.errai.cdi.client.events.BusReadyEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.Injector;
import org.jboss.errai.ioc.rebind.ioc.TaskType;
import org.jboss.errai.ioc.rebind.ioc.TypeInjector;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.junit.Test;

/**
 * Tests for the {@link ObservesExtension}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ObservesExtensionTest implements ObservesExtensionTestResult {
    interface HasObserverWithoutQualifiers {
        public void withoutQualifiers(@Observes BusReadyEvent event);
    }

    interface HasObserverWithQualifiers {
        public void withQualifiers(@Observes @A @B BusReadyEvent event);
    }

    private class MockInjector extends Injector {
        @Override
        public String instantiateOnly(InjectionContext injectContext, InjectionPoint injectionPoint) {
            return null;
        }

        @Override
        public String getType(InjectionContext injectContext, InjectionPoint injectionPoint) {
            return "bus";
        }

        @Override
        public boolean isInjected() {
            return false;
        }

        @Override
        public boolean isSingleton() {
            return false;
        }

        @Override
        public String getVarName() {
            return "var";
        }

        @Override
        public MetaClass getInjectedType() {
            return null;
        }
    }

    private Annotation observes = new Annotation() {
        public Class<? extends Annotation> annotationType() {
            return Observes.class;
        }
    };

    private InjectionContext ctx = new InjectionContext(null) {
        public Injector getInjector(Class<?> injectorType) {
            return new MockInjector();
        }
    };
    
    @Test
    public void testObservesExtensionForObserverWithoutQualifiers() {
        String s = new ObservesExtension(null).generateDecorator(getInjectionPoint(HasObserverWithoutQualifiers.class));
        assertEquals("failed to generate observes extension for Observer without qualifiers", 
                OBSERVES_EXTENSION_WITHOUT_QUALIFIERS, s.replaceAll("inj[0-9]+", "inj"));
    }

    @Test
    public void testObservesExtensionForObserverWithQualifiers() {
        String s = new ObservesExtension(null).generateDecorator(getInjectionPoint(HasObserverWithQualifiers.class));
        assertEquals("failed to generate observes extension for Observer without qualifiers", 
                OBSERVES_EXTENSION_WITH_QUALIFIERS, s.replaceAll("inj[0-9]+", "inj"));
    }
    
    private InjectionPoint getInjectionPoint(Class<?> clazz) {
        MetaClass type = MetaClassFactory.get(clazz);
        MetaMethod method = type.getMethods()[0];
      
        InjectionPoint injectionPoint = new InjectionPoint (observes, TaskType.Parameter, null, method, null,
                type, method.getParameters()[0], new TypeInjector(type), ctx);
        
        return injectionPoint;
    }

    protected static void assertEquals(String message, String expected, String actual) {
        org.junit.Assert.assertEquals(message, expected.replaceAll("\\s+", " ").trim(), 
                actual.replaceAll("\\s+", " ").trim());
    }
}