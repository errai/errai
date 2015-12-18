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
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;

/**
 * @author Heiko Braun <hbraun@redhat.com>
 */
public class ErraiServiceBean implements Bean, PassivationCapable {

  final InjectionTarget it;
  final String name;

  static final Set<Annotation> qualifiers = unmodifiableSet(new HashSet<Annotation>(
          asList(new AnnotationLiteral<Default>() {
                 },
                 new AnnotationLiteral<Any>() {
                 }
          )));

  static final Set<Type> types = unmodifiableSet(new HashSet<Type>(
          asList(ErraiService.class, Object.class)));

  @SuppressWarnings("unchecked")
  public ErraiServiceBean(final BeanManager bm, final String name) {

    //use this to read annotations of the class
    final AnnotatedType at = bm.createAnnotatedType(ErraiServiceImpl.class);

    //use this to create the class and inject dependencies
    this.it = bm.createInjectionTarget(at);
    this.name = name;
  }

  @Override
  public Class<?> getBeanClass() {
    return ErraiService.class;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<InjectionPoint> getInjectionPoints() {
    return it.getInjectionPoints();
  }

  @Override
  public String getName() {
    return "ErraiServiceBean" + name;
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
  @SuppressWarnings("unchecked")
  public Object create(final CreationalContext ctx) {
    final Object instance = ErraiServiceSingleton.getService();
    it.inject(instance, ctx);
    it.postConstruct(instance);
    return instance;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void destroy(final Object instance, final CreationalContext ctx) {
    it.preDestroy(instance);
    it.dispose(instance);
    ctx.release();
  }

  @Override
  public String getId() {
    return this.getClass().getName();
  }
}
