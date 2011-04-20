/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.cdi.server;

import com.sun.xml.internal.ws.resources.ServerMessages;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.api.ServerMessageBus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Basically a bean wrapper that provides CDI meta data.
 * It's used to inject the {@link org.jboss.errai.bus.client.framework.MessageBus} into the CDI context.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 15, 2010
 */
public class MessageBusMetaData implements Bean {

   // final InjectionTarget it;
    final MessageBus delegate;

    public MessageBusMetaData(BeanManager bm, MessageBus delegate) {
        AnnotatedType at = new AnnotatedType() {
            public Class getJavaClass() {
                return ServerMessageBusImpl.class;
            }

            public Set getConstructors() {
                return Collections.emptySet();
            }

            public Set getMethods() {
                return Collections.emptySet();
            }

            public Set getFields() {
                return Collections.emptySet();
            }

            public Type getBaseType() {
                return ServerMessageBusImpl.class;
            }

            public Set<Type> getTypeClosure() {
                return Collections.emptySet();
            }

            ApplicationScoped a = new ApplicationScoped() {
                public Class<? extends Annotation> annotationType() {
                    return ApplicationScoped.class;
                }
            };

            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                if (ApplicationScoped.class.isAssignableFrom(annotationType)) {
                    return (T) a;
                }

                return null;
            }

            public Set<Annotation> getAnnotations() {
                return Collections.singleton((Annotation) a);
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                return ApplicationScoped.class.isAssignableFrom(annotationType);
            }
        };

        //use this to create the class and inject dependencies
      //  this.it = bm.createInjectionTarget(at);

        // invocation target
        this.delegate = delegate;
    }

    public Class<?> getBeanClass() {
        return MessageBus.class;
    }

    public Set<InjectionPoint> getInjectionPoints() {
     //   return it.getInjectionPoints();

        return Collections.emptySet();
    }

    public String getName() {
        return null;
    }

    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        qualifiers.add(new AnnotationLiteral<Default>() {
        });
        qualifiers.add(new AnnotationLiteral<Any>() {
        });
        return qualifiers;
    }

    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    public Set<Type> getTypes() {
        Set<Type> types = new HashSet<Type>();
        types.add(MessageBus.class);
        types.add(ServerMessageBus.class);
       // types.add(ServerMessageBusImpl.class);
    //    types.add(Object.class);
        return types;
    }

    public boolean isAlternative() {
        return false;
    }

    public boolean isNullable() {
        return false;
    }

    public Object create(CreationalContext ctx) {
    //    Object instance = delegate;
  //      it.inject(instance, ctx);
   //     it.postConstruct(instance);
        return delegate;
    }

    public void destroy(Object instance, CreationalContext ctx) {
   //     it.preDestroy(instance);
   //     it.dispose(instance);
        ctx.release();
    }
}
