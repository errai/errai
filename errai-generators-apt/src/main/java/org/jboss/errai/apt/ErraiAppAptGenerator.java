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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;
import static org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile.Type.CLIENT;
import static org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile.Type.SHARED;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ "org.jboss.errai.common.configuration.ErraiApp" })
public class ErraiAppAptGenerator extends AbstractProcessor {

  private static final Logger log = LoggerFactory.getLogger(ErraiAppAptGenerator.class);

  private static final String GWT_XML = ".gwt.xml";

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

    try {
      generateAndSaveSourceFiles(annotations, new AptAnnotatedSourceElementsFinder(roundEnv));
    } catch (final Exception e) {
      log.error("Error generating files:");
      e.printStackTrace();
    }

    return false;
  }

  void generateAndSaveSourceFiles(final Set<? extends TypeElement> annotations,
          final AnnotatedSourceElementsFinder annotatedElementsFinder) {

    for (final TypeElement erraiAppAnnotation : annotations) {
      final long start = System.currentTimeMillis();
      log.info("Generating files using Errai APT Generators..");

      final Types types = processingEnv.getTypeUtils();
      final Elements elements = processingEnv.getElementUtils();
      final Filer filer = processingEnv.getFiler();
      APTClassUtil.init(types, elements);

      annotatedElementsFinder.findSourceElementsAnnotatedWith(ErraiApp.class)
              .stream()
              .map(Element::asType)
              .map(APTClass::new)
              .map(app -> newErraiAptExportedTypes(annotatedElementsFinder, elements, filer, app))
              .peek(this::generateAptCompatibleGwtModuleFile)
              .flatMap(this::findGenerators)
              .flatMap(this::generatedFiles)
              .forEach(this::saveFile);

      log.info("Successfully generated files using Errai APT Generators in {}ms", System.currentTimeMillis() - start);
    }
  }

  private void generateAptCompatibleGwtModuleFile(final ErraiAptExportedTypes erraiAptExportedTypes) {
    erraiAptExportedTypes.resourceFilesFinder()
            .getResource(erraiAptExportedTypes.erraiAppConfiguration().gwtModuleName().replace(".", "/") + GWT_XML)
            .map(file -> new AptCompatibleGwtModuleFile(file, erraiAptExportedTypes))
            .ifPresent(this::saveFile);
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
          final Elements elements,
          final Filer filer,
          final MetaClass erraiAppAnnotatedMetaClass) {

    log.info("Processing {}", erraiAppAnnotatedMetaClass.getFullyQualifiedName());
    return new ErraiAptExportedTypes(erraiAppAnnotatedMetaClass, elements, annotatedElementsFinder,
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

  private void saveFile(final AptCompatibleGwtModuleFile file) {

    final int lastDot = file.gwtModuleName().lastIndexOf(".");
    final String fileName = file.gwtModuleName().substring(lastDot + 1) + GWT_XML;
    final String packageName = file.gwtModuleName().substring(0, lastDot);

    try {
      // By writing to CLASS_OUTPUT we overwrite the original .gwt.xml file
      final FileObject sourceFile = processingEnv.getFiler().createResource(CLASS_OUTPUT, packageName, fileName);
      final String newGwtModuleFileContent = file.generate();

      try (final Writer writer = sourceFile.openWriter()) {
        writer.write(newGwtModuleFileContent);
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to write file " + file.gwtModuleName());
    }
  }

  private void saveFile(final ErraiAptGeneratedSourceFile file) {
    try {
      try (final Writer writer = getFileObject(file).openWriter()) {
        writer.write(file.getSourceCode());
      }
    } catch (final IOException e) {
      throw new RuntimeException("Could not write generated file", e);
    }
  }

  private FileObject getFileObject(final ErraiAptGeneratedSourceFile file) throws IOException {
    final String pkg = file.getPackageName();
    final String classSimpleName = file.getClassSimpleName();

    if (file.getType().equals(CLIENT)) {
      // By saving .java source files as resources we skip javac compilation. This behavior is
      // desirable since generated client code will be compiled by the GWT/J2CL compiler.
      return processingEnv.getFiler().createResource(SOURCE_OUTPUT, pkg, classSimpleName + ".java");
    }

    if (file.getType().equals(SHARED)) {
      return processingEnv.getFiler().createSourceFile(pkg + "." + classSimpleName);
    }

    throw new RuntimeException("Unsupported generated source file type " + file.getType());
  }
}
