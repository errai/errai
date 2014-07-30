/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.validation.rebind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.ioc.util.PropertiesUtil;
import org.jboss.errai.reflections.Reflections;
import org.jboss.errai.reflections.scanners.FieldAnnotationsScanner;
import org.jboss.errai.reflections.scanners.TypeAnnotationsScanner;
import org.jboss.errai.reflections.util.ClasspathHelper;
import org.jboss.errai.reflections.util.ConfigurationBuilder;
import org.jboss.errai.reflections.util.SimplePackageFilePathPredicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
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

  class ValidationScanner extends Reflections {

    ValidationScanner() {
      super(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forClassLoader())
        .filterInputsBy(new SimplePackageFilePathPredicate(PropertiesUtil.getPropertyValues(BLACKLIST_PROPERTY, "\\s")))
        .setScanners(
            new FieldAnnotationsScanner(),
            new TypeAnnotationsScanner()));
      scan();
    }

  }

  public static final String BLACKLIST_PROPERTY = "errai.validation.blacklist";

  private final ValidationScanner scanner;

  GwtValidatorGenerator() {
    this.scanner = new ValidationScanner();
  }

  ClassStructureBuilder<?> generate() {
    final Set<Class<?>> validationAnnotations = scanner.getTypesAnnotatedWith(Constraint.class);
    final SetMultimap<Class<?>, Annotation> validationConfig = getValidationConfig(validationAnnotations);
    final Set<Class<?>> beans = Sets.newHashSet(validationConfig.keySet());
    // look for beans that use @Valid but no other constraints.
    addBeansAnnotatedWithValid(beans);
    final Set<Class<?>> groups = extractValidationGroups(validationConfig);
    
    if (beans.isEmpty() || groups.isEmpty()) {
      // Nothing to validate
      return null;
    }

    ClassStructureBuilder<?> builder = ClassBuilder.define("Gwt" + Validator.class.getSimpleName()).publicScope()
            .interfaceDefinition().implementsInterface(Validator.class).body();

    builder.getClassDefinition().addAnnotation(new GwtValidation() {
      @Override
      public Class<?>[] value() {
        return beans.toArray(new Class<?>[beans.size()]);
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
  private SetMultimap<Class<?>, Annotation> getValidationConfig(Set<Class<?>> validationAnnotations) {
    SetMultimap<Class<?>, Annotation> beans = HashMultimap.create();
    for (Class<?> annotation : validationAnnotations) {
      for (Field field : scanner.getFieldsAnnotatedWith((Class<? extends Annotation>) annotation)) {
        beans.put(field.getDeclaringClass(), field.getAnnotation((Class<? extends Annotation>) annotation));
      }
      for (Method method : scanner.getMethodsAnnotatedWith((Class<? extends Annotation>) annotation)) {
        beans.put(method.getDeclaringClass(), method.getAnnotation((Class<? extends Annotation>) annotation));
      }
    }

    return beans;
  }

  private void addBeansAnnotatedWithValid(Set<Class<?>> beans) {
    for (Field field : scanner.getFieldsAnnotatedWith(Valid.class)) {
      beans.add(field.getDeclaringClass());
      beans.add(field.getType());
    }
    for (Method method : scanner.getMethodsAnnotatedWith(Valid.class)) {
      beans.add(method.getDeclaringClass());
      beans.add(method.getReturnType());
    }
  }

  private Set<Class<?>> extractValidationGroups(SetMultimap<Class<?>, Annotation> validationConfig) {
    Set<Class<?>> groups = new HashSet<Class<?>>();

    for (Annotation annotation : validationConfig.values()) {
      try {
        Method method = annotation.getClass().getMethod("groups", null);
        Class<?>[] ret = (Class<?>[]) method.invoke(annotation, null);
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