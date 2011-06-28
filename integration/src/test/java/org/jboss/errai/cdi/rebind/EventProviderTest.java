/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.cdi.rebind;

import java.lang.annotation.Annotation;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.bus.rebind.ProcessingContext;
import org.jboss.errai.cdi.client.EventProvider;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.ioc.rebind.ioc.ContextualProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.TaskType;
import org.jboss.errai.ioc.rebind.ioc.TypeInjector;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.junit.Test;

import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EventProviderTest extends AbstractErraiCDIRebindTest implements EventProviderTestResult {
    class HasEventWithoutQualifiers {
        @Inject
        private Event<String> event;
    }

    class HasEventWithQualifiers {
        @Inject @A @B
        private Event<String> event;
    }

    private Annotation observes = new Annotation() {
        public Class<? extends Annotation> annotationType() {
            return Observes.class;
        }
    };

    private InjectionContext mockContext = new InjectionContext(null) {
        public ProcessingContext getProcessingContext() {
            return new ProcessingContext(null, null, null, null) {
                public SourceWriter getWriter() {
                    return new StringSourceWriter();
                }
            };
        }
    };

    @Test
    public void testEventProviderWithoutQualifiers() {
        String s = new ContextualProviderInjector(MetaClassFactory.get(HasEventWithoutQualifiers.class),
                    MetaClassFactory.get(EventProvider.class))
                .getType(mockContext, getInjectionPoint(HasEventWithoutQualifiers.class));

        assertEquals("failed to generate observes extension for Observer without qualifiers",
                EVENT_PROVIDER_FOR_STRING_WITHOUT_QUALIFIERS, s.replaceAll("inj[0-9]+", "inj"));
    }

    @Test
    public void testEventProviderWithQualifiers() {
        String s = new ContextualProviderInjector(MetaClassFactory.get(HasEventWithQualifiers.class),
                MetaClassFactory.get(EventProvider.class))
                .getType(mockContext, getInjectionPoint(HasEventWithQualifiers.class));

        assertEquals("failed to generate observes extension for Observer with qualifiers",
                EVENT_PROVIDER_FOR_STRING_WITH_QUALIFIERS, s.replaceAll("inj[0-9]+", "inj"));
    }

    private InjectionPoint getInjectionPoint(Class<?> clazz) {
        MetaClass type = MetaClassFactory.get(clazz);
        MetaField field = type.getDeclaredField("event");

        InjectionPoint injectionPoint = new InjectionPoint(observes, TaskType.Field, null, null, field,
                field.getType(), null, new TypeInjector(type), mockContext);
        return injectionPoint;
    }
}