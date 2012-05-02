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

import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.server.api.ServerMessageBus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

/**
 * Basically a bean wrapper that provides CDI meta data.
 * It's used to inject the {@link org.jboss.errai.bus.client.framework.MessageBus} into the CDI context.
 *
 * @author Heiko Braun <hbraun@redhat.com>
 */
public class MessageBusBean implements Bean {
  final MessageBus delegate;


  static final Set<Annotation> qualifiers = unmodifiableSet(new HashSet<Annotation>(
          asList(new AnnotationLiteral<Default>() {
                 },
                 new AnnotationLiteral<Any>() {
                 }
          )));

  static final Set<Type> types = unmodifiableSet(new HashSet<Type>(
          asList(MessageBus.class, ServerMessageBus.class, Object.class)));

  public MessageBusBean(final MessageBus delegate) {
    // invocation target
    this.delegate = delegate;
  }

  public Class<?> getBeanClass() {
    return MessageBus.class;
  }

  public Set<InjectionPoint> getInjectionPoints() {
    return Collections.emptySet();
  }

  public String getName() {
    return null;
  }

  public Set<Annotation> getQualifiers() {
    return qualifiers;
  }

  public Class<? extends Annotation> getScope() {
    return ApplicationScoped.class;
  }

  public Set<Class<? extends Annotation>> getStereotypes() {
    return Collections.emptySet();
  }

  public Set<Type> getTypes() {
    return types;
  }

  public boolean isAlternative() {
    return false;
  }

  public boolean isNullable() {
    return false;
  }

  public Object create(CreationalContext ctx) {
    return delegate;
  }

  public void destroy(Object instance, CreationalContext ctx) {
    ctx.release();
  }
}
