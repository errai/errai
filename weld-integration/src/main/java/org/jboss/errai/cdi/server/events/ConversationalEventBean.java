/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.cdi.server.events;

import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.weld.Container;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.Arrays2;

import javax.enterprise.context.Dependent;
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

/**
 * @author Mike Brock
 */
public class ConversationalEventBean implements Bean {
  private static final Set<Annotation> DEFAULT_QUALIFIERS;

  static {
    DEFAULT_QUALIFIERS = new HashSet<Annotation>();
    DEFAULT_QUALIFIERS.add(new AnnotationLiteral<Any>() {
    });
    DEFAULT_QUALIFIERS.add(new AnnotationLiteral<Default>() {
    });
  }

  private BeanManagerImpl manager;
  private MessageBus bus;
  private Set<Type> typesSet;
  private Type type;

  public ConversationalEventBean(Type type, BeanManagerImpl manager, MessageBus bus) {
    this.manager = manager;
    this.bus = bus;
    this.type = type;
    typesSet = Arrays2.<Type>asSet(type, Object.class);
  }

  @Override
  public Class<?> getBeanClass() {
    return ConversationalEventImpl.class;
  }

  public Set<Type> getTypes() {
    return typesSet;
  }

  @Override
  public Set<Annotation> getQualifiers() {
    return DEFAULT_QUALIFIERS;
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return Dependent.class;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public Set<Class<? extends Annotation>> getStereotypes() {
    return Collections.emptySet();
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
  public Set<InjectionPoint> getInjectionPoints() {
    return Collections.emptySet();
  }

  @Override
  public Object create(CreationalContext creationalContext) {
    InjectionPoint injectionPoint = Container.instance().services().get(CurrentInjectionPoint.class).peek();
    return ConversationalEventImpl.of(injectionPoint, manager, bus);
  }

  @Override
  public void destroy(Object instance, CreationalContext creationalContext) {
  }

  @Override
  public String toString() {
    return "Implicit Bean [" + ConversationalEvent.class.getName() + "] with qualifiers [@Default]";
  }

}
