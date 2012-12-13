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

package org.jboss.errai.cdi.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.ioc.client.api.ProviderException;
import org.jboss.errai.ioc.client.api.ReplyTo;
import org.jboss.errai.ioc.client.api.Sender;
import org.jboss.errai.ioc.client.api.ToSubject;
import org.jboss.errai.ioc.client.api.builtin.ErraiMessageSender;
import org.jboss.weld.Container;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.util.collections.Arrays2;

/**
 * @author Mike Brock
 */
public class SenderBean implements Bean {

  private Set<Annotation> qualifiers;
  private final MessageBus bus;
  private final Set<Type> typesSet;
 
  public SenderBean(final Type type, final Set<Annotation> qualifiers, final MessageBus bus) {
    this.bus = bus;
    this.qualifiers = qualifiers;
    typesSet = Arrays2.<Type>asSet(type);
  }

  @Override
  public Class<?> getBeanClass() {
    return ErraiMessageSender.class;
  }

  public Set<Type> getTypes() {
    return typesSet;
  }

  @Override
  public Set<Annotation> getQualifiers() {
    return qualifiers;
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
  public Object create(final CreationalContext creationalContext) {
    final InjectionPoint injectionPoint = Container.instance().services().get(CurrentInjectionPoint.class).peek();
    final Set<Annotation> qualifiers = injectionPoint.getQualifiers();
    
//    final ParameterizedType injectionPointType = (ParameterizedType) injectionPoint.getType();
//    final Type innerType = injectionPointType.getActualTypeArguments()[0];
//    Class senderType = null;
//
//    if (innerType instanceof Class) {
//      senderType = (Class) innerType;
//    }
//    else if (innerType instanceof ParameterizedType) {
//      senderType = (Class) ((ParameterizedType) innerType).getRawType();
//    }
        
    String toSubject = null, replyTo = null;

    for (final Annotation a : qualifiers) {
      if (a instanceof ToSubject) {
        toSubject = ((ToSubject) a).value();
      }
      else if (a instanceof ReplyTo) {
        replyTo = ((ReplyTo) a).value();
      }
    }

    if (toSubject == null) {
      throw new ProviderException(PROVIDER_EXCEPTION_ERROR_MSG_BASE + ": Required "
              + ToSubject.class.getName() + " qualifier missing at injection point.");
    }

    return ErraiMessageSender.of(toSubject, replyTo, bus);
  }
  
  private static final String PROVIDER_EXCEPTION_ERROR_MSG_BASE
          = "Injection of " + Sender.class.getName() + " implicit bean failed. ";

  @Override
  public void destroy(final Object instance, final CreationalContext creationalContext) {
  }

  @Override
  public String toString() {
    return "Implicit Bean [" + Sender.class.getName() + "] with qualifiers [@Default]";
  }

}
