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

package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.common.apt.ResourceFilesFinder;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.MetaClassFinder;
import org.jboss.errai.config.propertiesfile.ErraiAppPropertiesConfiguration;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ioc.client.Bootstrapper;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.client.container.IOCEnvironment;

import javax.enterprise.event.Observes;
import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

/**
 * The main generator class for the Errai IOC framework.
 * <p/>
 * <pre>
 *
 * </pre>
 *
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@GenerateAsync(Bootstrapper.class)
public class IOCGenerator extends AbstractAsyncGenerator {

  private final String packageName = "org.jboss.errai.ioc.client";
  private final String className = "BootstrapperImpl";

  public IOCGenerator() {
  }

  @Override
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
          throws UnableToCompleteException {

    logger.log(TreeLogger.INFO, "generating ioc bootstrapping code...");
    return startAsyncGeneratorsAndWaitFor(Bootstrapper.class, context, logger, packageName, className);
  }

  @Override
  protected String generate(final TreeLogger logger, final GeneratorContext context) {
    final Set<String> translatablePackages = RebindUtils.findTranslatablePackages(context);
    final MetaClassFinder metaClassFinder = ann -> findMetaClasses(context, translatablePackages, ann);
    final ErraiConfiguration erraiConfiguration = new ErraiAppPropertiesConfiguration();
    final IocRelevantClassesFinder iocRelevantClasses = ann -> IocRelevantClassesUtil.findRelevantClasses();
    final ResourceFilesFinder resourceFilesFinder = this::findResourceFile;

    return generate(context, metaClassFinder, erraiConfiguration, iocRelevantClasses, resourceFilesFinder, className);
  }

  private Optional<File> findResourceFile(final String name) {
    return Optional.ofNullable(Thread.currentThread().getContextClassLoader().getResource(name)).map(url -> {
      try {
        return url.toURI();
      } catch (final URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }).map(File::new);
  }

  public String generate(final GeneratorContext context,
          final MetaClassFinder metaClassFinder,
          final ErraiConfiguration erraiConfiguration,
          final IocRelevantClassesFinder relevantClasses,
          final ResourceFilesFinder resourceFilesFinder,
          final String classSimpleName) {

    return new IOCBootstrapGenerator(metaClassFinder, resourceFilesFinder, context, erraiConfiguration, relevantClasses)
            .generate(packageName, classSimpleName);
  }

  private Set<MetaClass> findMetaClasses(final GeneratorContext context,
          final Set<String> translatablePackages,
          final Class<? extends Annotation> annotation) {

    if (asList(IOCExtension.class, CodeDecorator.class).contains(annotation)) {
      return ScannerSingleton.getOrCreateInstance()
              .getTypesAnnotatedWith(annotation)
              .stream()
              .map(JavaReflectionClass::newUncachedInstance)
              .collect(toSet());
    }

    if (Observes.class.equals(annotation)) {
      return ClassScanner.getParametersAnnotatedWith(Observes.class, context)
              .stream()
              .map(MetaParameter::getType)
              .collect(toSet());
    }

    return new HashSet<>(ClassScanner.getTypesAnnotatedWith(annotation, translatablePackages, context));
  }

  @Override
  protected boolean isCacheValid() {
    // This ensures the logged total build time of factories is reset even if
    // the BootstrapperImpl is not regenerated.
    FactoryGenerator.resetTotalTime();
    Collection<MetaClass> newOrUpdated = MetaClassFactory.getAllNewOrUpdatedClasses();
    // filter out generated IOC environment config
    if (newOrUpdated.size() == 1) {
      MetaClass clazz = newOrUpdated.iterator().next();
      if (clazz.isAssignableTo(IOCEnvironment.class)) {
        newOrUpdated.clear();
      }
    }

    boolean hasAnyChanges = !newOrUpdated.isEmpty() || !MetaClassFactory.getAllDeletedClasses().isEmpty();
    return hasGenerationCache() && (EnvUtil.isProdMode() || !hasAnyChanges);
  }

  public String getPackageName() {
    return packageName;
  }

  public String getClassSimpleName() {
    return className;
  }

  @Override
  public boolean alreadyGeneratedSourcesViaAptGenerators(final GeneratorContext context) {
    return RebindUtils.isErraiUseAptGeneratorsPropertyEnabled(context);
  }

}
