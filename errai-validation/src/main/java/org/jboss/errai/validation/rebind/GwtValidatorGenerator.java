/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.validation.rebind;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ioc.util.PropertiesUtil;
import org.jboss.errai.reflections.Reflections;
import org.jboss.errai.reflections.scanners.FieldAnnotationsScanner;
import org.jboss.errai.reflections.scanners.TypeAnnotationsScanner;
import org.jboss.errai.reflections.util.ClasspathHelper;
import org.jboss.errai.reflections.util.ConfigurationBuilder;
import org.jboss.errai.reflections.util.SimplePackageFilter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.google.gwt.validation.client.GwtValidation;

/**
 * Generates the GWT {@link Validator} interface based on validation
 * annotations.
 *
 * @author Johannes Barop <jb@barop.de>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
class GwtValidatorGenerator {

  class ContraintScanner extends Reflections {
    ContraintScanner() {
      super(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forClassLoader())
        .setScanners(
            new FieldAnnotationsScanner(),
            new TypeAnnotationsScanner()));
      scan();
    }
  }

  private static final String BLACKLIST_PROPERTY = "errai.validation.blacklist";
  private static Set<MetaClass> globalConstraints;
  
  public ClassStructureBuilder<?> generate(final GeneratorContext context) {
    if (globalConstraints == null) {
      globalConstraints = new HashSet<MetaClass>();
      for (Class<?> clazz : new ContraintScanner().getTypesAnnotatedWith(Constraint.class)) {
        globalConstraints.add(MetaClassFactory.get(clazz)); 
      }
    }
    
    Collection<MetaClass> constraints =  ClassScanner.getTypesAnnotatedWith(Constraint.class, context);
    Set<MetaClass> allConstraints = new HashSet<MetaClass>();
    allConstraints.addAll(globalConstraints);
    allConstraints.addAll(constraints);
    
    final SetMultimap<MetaClass, Annotation> validationConfig = getValidationConfig(allConstraints, context);
    final Set<Class<?>> beans = extractValidatableBeans(validationConfig.keySet(), context);
    final Set<Class<?>> groups = extractValidationGroups(validationConfig);
    
    final Set<Class<?>> filteredBeans = new HashSet<Class<?>>();
    SimplePackageFilter filter = new SimplePackageFilter(PropertiesUtil.getPropertyValues(BLACKLIST_PROPERTY, " "));
    for (Class<?> bean : beans) {
      if (!filter.apply(bean.getName())) {
        filteredBeans.add(bean);
      }
    }
    
    if (filteredBeans.isEmpty() || groups.isEmpty()) {
      // Nothing to validate
      return null;
    }

    ClassStructureBuilder<?> builder = ClassBuilder.define("Gwt" + Validator.class.getSimpleName()).publicScope()
            .interfaceDefinition().implementsInterface(Validator.class).body();

    builder.getClassDefinition().addAnnotation(new GwtValidation() {
      @Override
      public Class<?>[] value() {
        return filteredBeans.toArray(new Class<?>[filteredBeans.size()]);
      }

      @Override
      public Class<?>[] groups() {
        return groups.toArray(new Class<?>[groups.size()]);
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return GwtValidation.class;
      }
    });

    return builder;
  }

  @SuppressWarnings("unchecked")
  private SetMultimap<MetaClass, Annotation> getValidationConfig(Collection<MetaClass> validationAnnotations, GeneratorContext context) {
    SetMultimap<MetaClass, Annotation> beans = HashMultimap.create();
    for (MetaClass annotation : validationAnnotations) {
      for (MetaField field : ClassScanner.getFieldsAnnotatedWith((Class<? extends Annotation>) annotation.asClass(), null, context)) {
        beans.put(field.getDeclaringClass(), field.getAnnotation((Class<? extends Annotation>) annotation.asClass()));
      }
      for (MetaMethod method : ClassScanner.getMethodsAnnotatedWith((Class<? extends Annotation>) annotation.asClass(), null, context)) {
        beans.put(method.getDeclaringClass(), method.getAnnotation((Class<? extends Annotation>) annotation.asClass()));
      }
      for (MetaClass type : ClassScanner.getTypesAnnotatedWith((Class<? extends Annotation>) annotation.asClass(), null, context)) {
        beans.put(type, type.getAnnotation((Class<? extends Annotation>) annotation.asClass()));
      }
    }

    return beans;
  }

  private Set<Class<?>> extractValidatableBeans(final Set<MetaClass> beans, final GeneratorContext context) {
    Set<Class<?>> allBeans = new HashSet<Class<?>>();
    
    for (MetaClass bean : beans) {
      allBeans.add(bean.asClass());
    }
    
    for (MetaField field : ClassScanner.getFieldsAnnotatedWith(Valid.class, null, context)) {
      allBeans.add(field.getDeclaringClass().asClass());
      allBeans.add(field.getType().asClass());
    }
    for (MetaMethod method : ClassScanner.getMethodsAnnotatedWith(Valid.class, null, context)) {
      allBeans.add(method.getDeclaringClass().asClass());
      allBeans.add(method.getReturnType().asClass());
    }
    
    return allBeans;
  }

  private Set<Class<?>> extractValidationGroups(SetMultimap<MetaClass, Annotation> validationConfig) {
    Set<Class<?>> groups = new HashSet<Class<?>>();

    for (Annotation annotation : validationConfig.values()) {
      try {
        Method method = annotation.getClass().getMethod("groups", (Class<?>[]) null);
        Class<?>[] ret = (Class<?>[]) method.invoke(annotation, (Object[]) null);
        if (ret.length != 0) {
          groups.addAll(Arrays.asList(ret));
        }
        else {
          groups.add(Default.class);
        }
      }
      catch (NoSuchMethodException e) {
        throw new RuntimeException("Error finding groups() parameter in " + annotation.getClass().getName(), e);
      }
      catch (InvocationTargetException e) {
        throw new RuntimeException("Error invoking groups() parameter in " + annotation.getClass().getName(), e);
      }
      catch (IllegalAccessException e) {
        throw new RuntimeException("Error invoking groups() parameter in " + annotation.getClass().getName(), e);
      }
    }
    return groups;
  }
}
