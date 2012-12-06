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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Qualifier;

import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.enterprise.client.cdi.api.Conversational;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author Mike Brock
 */
public class ConversationalEventImpl<T> implements ConversationalEvent<T>, Serializable {
  private final Type type;
  private final BeanManagerImpl manager;
  private final Annotation[] qualifiers;

  private Class rawType;
  private Set<String> qualifiersForWire;
  private MessageBus bus;

  public static ConversationalEventImpl of(InjectionPoint injectionPoint, BeanManager manager, MessageBus bus) {
    return new ConversationalEventImpl(injectionPoint, manager, bus);
  }

  private ConversationalEventImpl(InjectionPoint injectionPoint, BeanManager manager, MessageBus bus) {
    this.bus = bus;
    this.manager = (BeanManagerImpl) manager;
    List<Annotation> filteredQualifiers = new ArrayList<Annotation>();
    for (Annotation a : injectionPoint.getQualifiers()) {
      if (a instanceof Conversational ||
              a instanceof Default ||
              !a.annotationType().isAnnotationPresent(Qualifier.class)) continue;
      filteredQualifiers.add(a);
    }
    this.qualifiers = filteredQualifiers.toArray(new Annotation[filteredQualifiers.size()]);
    this.qualifiersForWire = CDI.getQualifiersPart(qualifiers);

    Type t = injectionPoint.getType();
    if (t instanceof ParameterizedType) {
      ParameterizedType pType = (ParameterizedType) t;

      Type toMarshallType = pType.getActualTypeArguments()[0];

      this.type = toMarshallType;

      if (toMarshallType instanceof ParameterizedType) {
        rawType = (Class) ((ParameterizedType) toMarshallType).getRawType();
      }
      else if (toMarshallType instanceof Class) {
        rawType = (Class) toMarshallType;
      }
      else {
        throw new RuntimeException("invalid type parameter for " + ConversationalEvent.class.getName() + ": " + type);
      }
    }
    else {
      throw new RuntimeException("unparameterized conversational event: " + injectionPoint.getType());
    }
  }

  private ConversationalEventImpl(BeanManager manager, Type subtype, Annotation[] qualifiers) {
    this.manager = (BeanManagerImpl) manager;
    this.type = subtype;
    this.qualifiers = qualifiers;
  }

  @Override
  public ConversationalEvent<T> select(Annotation... qualifiers) {
    return selectEvent(this.getType(), qualifiers);
  }

  @Override
  public <U extends T> ConversationalEvent<U> select(Class<U> subtype, Annotation... qualifiers) {
    return selectEvent(subtype, qualifiers);
  }

  @Override
  public <U extends T> ConversationalEvent<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
    return selectEvent(subtype.getType(), qualifiers);
  }

  private <U extends T> ConversationalEvent<U> selectEvent(Type subtype, Annotation[] newQualifiers) {
    return (ConversationalEvent<U>) new ConversationalEventImpl<T>(manager, subtype, newQualifiers);
  }

  public Type getType() {
    return type;
  }

  public Annotation[] getQualifiers() {
    return qualifiers;
  }
}
