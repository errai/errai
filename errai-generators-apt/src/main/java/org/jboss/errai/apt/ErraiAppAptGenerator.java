/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.apt;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.AptAnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.AptResourceFilesFinder;
import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.jboss.errai.common.configuration.ErraiApp;
import org.jboss.errai.common.configuration.ErraiGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ "org.jboss.errai.common.configuration.ErraiApp" })
public class ErraiAppAptGenerator extends AbstractProcessor {

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

    try {
      generateAndSaveSourceFiles(annotations, new AptAnnotatedSourceElementsFinder(roundEnv));
    } catch (final Exception e) {
      System.out.println("Error generating files:");
      e.printStackTrace();
    }

    return false;
  }

  void generateAndSaveSourceFiles(final Set<? extends TypeElement> annotations,
          final AnnotatedSourceElementsFinder annotatedElementsFinder) {

    for (final TypeElement erraiAppAnnotation : annotations) {
      final long start = System.currentTimeMillis();
      System.out.println("Generating files using Errai APT Generators..");

      final Types types = processingEnv.getTypeUtils();
      final Elements elements = processingEnv.getElementUtils();
      final Filer filer = processingEnv.getFiler();
      APTClassUtil.init(types, elements);

      annotatedElementsFinder.findSourceElementsAnnotatedWith(ErraiApp.class)
              .stream()
              .map(Element::asType)
              .map(APTClass::new)
              .sorted(comparing(APTClass::getFullyQualifiedName))
              .map(app -> newErraiAptExportedTypes(annotatedElementsFinder, types, elements, filer, app))
              .flatMap(this::findGenerators)
              .flatMap(this::generatedFiles)
              .forEach(this::saveFile);

      System.out.println("Successfully generated files using Errai APT Generators in "
              + (System.currentTimeMillis() - start)
              + "ms");
    }
  }

  private Stream<ErraiAptGeneratedSourceFile> generatedFiles(final ErraiAptGenerators.Any generator) {
    try {
      return generator.files().stream();
    } catch (final Exception e) {
      // Continues to next generator even when errors occur
      e.printStackTrace();
      return Stream.of();
    }
  }

  private ErraiAptExportedTypes newErraiAptExportedTypes(final AnnotatedSourceElementsFinder annotatedElementsFinder,
          final Types types,
          final Elements elements,
          final Filer filer,
          final MetaClass erraiAppAnnotatedMetaClass) {

    System.out.println("Generating classes for " + erraiAppAnnotatedMetaClass.getFullyQualifiedName());
    return new ErraiAptExportedTypes(erraiAppAnnotatedMetaClass, types, elements, annotatedElementsFinder,
            new AptResourceFilesFinder(filer));
  }

  private Stream<ErraiAptGenerators.Any> findGenerators(final ErraiAptExportedTypes erraiAptExportedTypes) {
    return erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiGenerator.class)
            .stream()
            .map(this::loadClass)
            .map(generatorClass -> newGenerator(generatorClass, erraiAptExportedTypes))
            .sorted(comparing(ErraiAptGenerators.Any::priority).thenComparing(g -> g.getClass().getSimpleName()));
  }

  @SuppressWarnings("unchecked")
  private Class<? extends ErraiAptGenerators.Any> loadClass(final MetaClass metaClass) {
    try {
      // Because we're sure generators will always be pre-compiled, it's safe to get their classes using Class.forName
      return (Class<? extends ErraiAptGenerators.Any>) Class.forName(metaClass.getFullyQualifiedName());
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException(metaClass.getFullyQualifiedName() + " is not an ErraiAptGenerator", e);
    }
  }

  private ErraiAptGenerators.Any newGenerator(final Class<? extends ErraiAptGenerators.Any> generatorClass,
          final ErraiAptExportedTypes erraiAptExportedTypes) {
    try {
      final Constructor<? extends ErraiAptGenerators.Any> constructor = generatorClass.getConstructor(
              ErraiAptExportedTypes.class);
      constructor.setAccessible(true);
      return constructor.newInstance(erraiAptExportedTypes);
    } catch (final Exception e) {
      throw new RuntimeException("Class " + generatorClass.getName() + " couldn't be instantiated.", e);
    }
  }

  private void saveFile(final ErraiAptGeneratedSourceFile file) {
    try {
      // By saving .java source files as resources we skip javac compilation. This behavior is desirable since all
      // generated code is client code and will be compiled by the GWT/J2CL compiler.
      // FIXME: errai-marshalling will generate server code too

      final FileObject sourceFile;

      if (file.saveAsResource()) {
        sourceFile = processingEnv.getFiler()
                .createResource(SOURCE_OUTPUT, file.getPackageName(), file.getClassSimpleName() + ".java");
      } else {
        sourceFile = processingEnv.getFiler().createSourceFile(file.getPackageName() + "." + file.getClassSimpleName());
      }

      try (final Writer writer = sourceFile.openWriter()) {
        writer.write(file.getSourceCode());
      }
    } catch (final IOException e) {
      throw new RuntimeException("Could not write generated file", e);
    }
  }
}
