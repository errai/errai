/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.databinding.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.apt.MetaClassFinder;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.ErraiAppPropertiesConfiguration;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.BindableProxyProvider;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;
import org.jboss.errai.databinding.client.local.BindableProxyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Generates the proxy loader for {@link Bindable}s.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@GenerateAsync(BindableProxyLoader.class)
public class BindableProxyLoaderGenerator extends AbstractAsyncGenerator {

  private final Logger log = LoggerFactory.getLogger(BindableProxyLoaderGenerator.class);

  private final String packageName = "org.jboss.errai.databinding.client.local";
  private final String classSimpleName = "BindableProxyLoaderImpl";

  @Override
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
          throws UnableToCompleteException {

    return startAsyncGeneratorsAndWaitFor(BindableProxyLoader.class, context, logger, packageName, classSimpleName);
  }

  @Override
  protected String generate(final TreeLogger logger, final GeneratorContext context) {
    final Set<String> translatablePackages = RebindUtils.findTranslatablePackages(context);
    final Set<MetaClass> allConfiguredBindableTypes = new ErraiAppPropertiesConfiguration().modules().getBindableTypes();

    return generate((annotation) -> findAnnotatedElements(annotation, context, translatablePackages,
            allConfiguredBindableTypes));
  }

  public String generate(final MetaClassFinder metaClassFinder) {

    final Collection<MetaClass> defaultConverters = metaClassFinder.findAnnotatedWith(DefaultConverter.class);
    addCacheRelevantClasses(defaultConverters);

    final Collection<MetaClass> bindableTypes = metaClassFinder.findAnnotatedWith(Bindable.class);
    addCacheRelevantClasses(bindableTypes);

    ClassStructureBuilder<?> classBuilder = ClassBuilder.implement(BindableProxyLoader.class);
    final MethodBlockBuilder<?> loadProxies = classBuilder.publicMethod(void.class, "loadBindableProxies");

    for (final MetaClass bindable : bindableTypes) {
      if (bindable.isFinal()) {
        throw new RuntimeException("@Bindable type cannot be final: " + bindable.getFullyQualifiedName());
      }

      if (bindable.getDeclaredConstructor(new MetaClass[0]) == null || !bindable.getDeclaredConstructor(
              new MetaClass[0]).isPublic()) {
        throw new RuntimeException(
                "@Bindable type needs a public default no-arg constructor: " + bindable.getFullyQualifiedName());
      }

      final ClassStructureBuilder<?> bindableProxy = new BindableProxyGenerator(bindable, bindableTypes).generate();
      loadProxies.append(new InnerClass(bindableProxy.getClassDefinition()));
      final Statement proxyProvider = ObjectBuilder.newInstanceOf(BindableProxyProvider.class)
              .extend()
              .publicOverridesMethod("getBindableProxy", Parameter.of(Object.class, "model"))
              .append(Stmt.nestedCall(Stmt.newObject(bindableProxy.getClassDefinition())
                      .withParameters(Cast.to(bindable, Stmt.loadVariable("model")))).returnValue())
              .finish()
              .publicOverridesMethod("getBindableProxy")
              .append(Stmt.nestedCall(Stmt.newObject(bindableProxy.getClassDefinition())).returnValue())
              .finish()
              .finish();
      loadProxies.append(Stmt.invokeStatic(BindableProxyFactory.class, "addBindableProxy", bindable, proxyProvider));
    }

    generateDefaultConverterRegistrations(loadProxies, defaultConverters);

    classBuilder = (ClassStructureBuilder<?>) loadProxies.finish();
    return classBuilder.toJavaString();
  }

  /**
   * Scans for and registers global default converters.
   */
  private void generateDefaultConverterRegistrations(final MethodBlockBuilder<?> loadProxies,
          final Collection<MetaClass> defaultConverters) {

    for (final MetaClass converter : defaultConverters) {

      Statement registerConverterStatement = null;
      for (final MetaClass iface : converter.getInterfaces()) {
        if (iface.getErased().equals(MetaClassFactory.get(Converter.class))) {
          final MetaParameterizedType parameterizedInterface = iface.getParameterizedType();
          if (parameterizedInterface != null) {
            final MetaType[] typeArgs = parameterizedInterface.getTypeParameters();
            if (typeArgs != null && typeArgs.length == 2) {
              registerConverterStatement = Stmt.invokeStatic(Convert.class, "registerDefaultConverter", typeArgs[0],
                      typeArgs[1], Stmt.newObject(converter));
            }
          }
        }
      }

      if (registerConverterStatement != null) {
        loadProxies.append(registerConverterStatement);
      } else {
        log.warn("Ignoring @DefaultConverter: "
                + converter
                + "! Make sure it implements Converter and specifies type arguments for the model and widget type");
      }
    }
  }

  @Override
  protected boolean isRelevantClass(final MetaClass clazz) {
    for (final Annotation anno : clazz.unsafeGetAnnotations()) {
      if (anno.annotationType().equals(Bindable.class) || anno.annotationType().equals(DefaultConverter.class)) {
        return true;
      }
    }

    return false;
  }

  private Set<MetaClass> findAnnotatedElements(final Class<? extends Annotation> annotation,
          final GeneratorContext context,
          final Set<String> translatablePackages,
          final Set<MetaClass> allConfiguredBindableTypes) {

    if (annotation.equals(Bindable.class)) {
      final Set<MetaClass> annotatedBindableTypes = new HashSet<>(
              ClassScanner.getTypesAnnotatedWith(Bindable.class, translatablePackages, context));

      final Set<MetaClass> bindableTypes = new HashSet<>(annotatedBindableTypes);
      bindableTypes.addAll(allConfiguredBindableTypes);
      return bindableTypes;
    }

    return new HashSet<>(ClassScanner.getTypesAnnotatedWith(annotation, translatablePackages, context));
  }

  public String getPackageName() {
    return packageName;
  }

  public String getClassSimpleName() {
    return classSimpleName;
  }

  @Override
  public boolean alreadyGeneratedSourcesViaAptGenerators(final GeneratorContext context) {
    try {
      return context.getTypeOracle().getType(getPackageName() + "." + getClassSimpleName()) != null;
    } catch (final NotFoundException e) {
      return false;
    }
  }

}