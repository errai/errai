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

package org.jboss.errai.enterprise.rebind;

import com.google.common.collect.Multimap;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.apt.MetaClassFinder;
import org.jboss.errai.codegen.util.AnnotationFilter;
import org.jboss.errai.codegen.util.InterceptorProvider;
import org.jboss.errai.codegen.util.RuntimeAnnotationFilter;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.interceptor.FeatureInterceptor;
import org.jboss.errai.common.client.api.interceptor.InterceptsRemoteCall;
import org.jboss.errai.common.client.framework.ProxyProvider;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.enterprise.client.local.JaxrsProxyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Generates the JAX-RS proxy loader.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@GenerateAsync(JaxrsProxyLoader.class)
public class JaxrsProxyLoaderGenerator extends AbstractAsyncGenerator {
  private static final String IOC_MODULE_NAME = "org.jboss.errai.ioc.Container";
  private static final Logger log = LoggerFactory.getLogger(JaxrsProxyLoaderGenerator.class);

  private final String packageName = "org.jboss.errai.enterprise.client.local";
  private final String classSimpleName = "JaxrsProxyLoaderImpl";

  @Override
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
          throws UnableToCompleteException {

    return startAsyncGeneratorsAndWaitFor(JaxrsProxyLoader.class, context, logger, packageName, classSimpleName);
  }

  protected String generate(final TreeLogger logger, final GeneratorContext context) {
    final Boolean iocEnabled = RebindUtils.isModuleInherited(context, IOC_MODULE_NAME);
    final Set<String> translatablePackages = RebindUtils.findTranslatablePackages(context);
    final AnnotationFilter annotationFilter = new RuntimeAnnotationFilter(translatablePackages);
    final MetaClassFinder metaClassFinder = annotation -> getMetaClasses(context, annotation, translatablePackages);

    return generate(metaClassFinder, iocEnabled, annotationFilter);
  }

  public String generate(final MetaClassFinder metaClassFinder,
          final Boolean iocEnabled,
          final AnnotationFilter annotationFilter) {

    log.info("Generating JAX-RS RPC proxy loader class...");
    final long time = System.currentTimeMillis();

    ClassStructureBuilder<?> classBuilder = ClassBuilder.implement(JaxrsProxyLoader.class);

    final MethodBlockBuilder<?> loadProxies = classBuilder.publicMethod(void.class, "loadProxies");

    final Collection<MetaClass> featureInterceptors = metaClassFinder.findAnnotatedWith(FeatureInterceptor.class);
    addCacheRelevantClasses(featureInterceptors);

    final Collection<MetaClass> standaloneInterceptors = metaClassFinder.findAnnotatedWith(InterceptsRemoteCall.class);
    addCacheRelevantClasses(standaloneInterceptors);

    final InterceptorProvider interceptorProvider = new InterceptorProvider(featureInterceptors,
            standaloneInterceptors);

    final Collection<MetaClass> providers = metaClassFinder.findAnnotatedWith(Provider.class);
    addCacheRelevantClasses(providers);

    final Multimap<MetaClass, MetaClass> exceptionMappers = Utils.getClientExceptionMappers(providers);

    final Collection<MetaClass> remotes = metaClassFinder.findAnnotatedWith(Path.class);
    addCacheRelevantClasses(remotes);

    for (final MetaClass remote : remotes) {
      if (remote.isInterface()) {
        // create the remote proxy for this interface
        final ClassStructureBuilder<?> remoteProxy = new JaxrsProxyGenerator(remote, interceptorProvider,
                exceptionMappers, annotationFilter, iocEnabled).generate();
        loadProxies.append(new InnerClass(remoteProxy.getClassDefinition()));

        // create the proxy provider
        final Statement proxyProvider = ObjectBuilder.newInstanceOf(ProxyProvider.class)
                .extend()
                .publicOverridesMethod("getProxy")
                .append(Stmt.nestedCall(Stmt.newObject(remoteProxy.getClassDefinition())).returnValue())
                .finish()
                .finish();

        // create the call that registers the proxy provided for the generated proxy
        loadProxies.append(Stmt.invokeStatic(RemoteServiceProxyFactory.class, "addRemoteProxy", remote, proxyProvider));
      }
    }
    classBuilder = (ClassStructureBuilder<?>) loadProxies.finish();
    final String gen = classBuilder.toJavaString();
    log.info("Generated JAX-RS RPC proxy loader class in " + (System.currentTimeMillis() - time) + "ms.");
    return gen;
  }

  @Override
  protected boolean isRelevantClass(final MetaClass clazz) {
    // It's ok to use unsafe methods here because the APT environment doesn't call this method
    for (final Annotation anno : clazz.unsafeGetAnnotations()) {
      if (anno.annotationType().equals(Path.class)
              || anno.annotationType().equals(FeatureInterceptor.class)
              || anno.annotationType().equals(InterceptsRemoteCall.class)
              || anno.annotationType().equals(Provider.class)) {
        return true;
      }
    }

    return false;
  }

  private Set<MetaClass> getMetaClasses(final GeneratorContext context,
          final Class<? extends Annotation> annotation,
          final Set<String> translatablePackages) {

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
