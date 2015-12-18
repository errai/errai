/*
 * Copyright (C) 2009 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.cdi.server;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.server.api.ServerMessageBus;

/**
 * Basically a bean wrapper that provides CDI meta data.
 * It's used to inject the {@link org.jboss.errai.bus.client.api.messaging.MessageBus} into the CDI context.
 *
 * @author Heiko Braun <hbraun@redhat.com>
 */
public class MessageBusBean implements Bean, PassivationCapable {
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

  @Override
  public Class<?> getBeanClass() {
    return MessageBus.class;
  }

  @Override
  public Set<InjectionPoint> getInjectionPoints() {
    return Collections.emptySet();
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public Set<Annotation> getQualifiers() {
    return qualifiers;
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return ApplicationScoped.class;
  }

  @Override
  public Set<Class<? extends Annotation>> getStereotypes() {
    return Collections.emptySet();
  }

  @Override
  public Set<Type> getTypes() {
    return types;
  }

  @Override
  public boolean isAlternative() {
    return false;
  }

  @Override
  public boolean isNullable() {
    return false;
  }

  @Override
  public Object create(CreationalContext ctx) {
    return delegate;
  }

  @Override
  public void destroy(Object instance, CreationalContext ctx) {
    ctx.release();
  }

  @Override
  public String getId() {
    return this.getClass().getName();
  }
}
