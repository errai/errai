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

package org.jboss.errai.common.apt.generator.app;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.apt.exportfile.ExportedTypesFromExportFiles;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.jboss.errai.common.configuration.ErraiGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
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
public class ErraiAppAptGenerator {

  private static final Logger log = LoggerFactory.getLogger(ErraiAppAptGenerator.class);

  private static final String GWT_XML = ".gwt.xml";

  private final ProcessingEnvironment processingEnv;

  public ErraiAppAptGenerator(final ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  public void generateAndSaveSourceFiles(final Set<MetaClass> erraiApps) {

    final long start = System.currentTimeMillis();
    log.info("Generating files using Errai APT Generators..");

    erraiApps.stream()
            .map(a -> newErraiAptExportedTypes(a, processingEnv.getFiler(), processingEnv.getElementUtils()))
            .peek(this::generateAptCompatibleGwtModuleFile)
            .flatMap(this::createGenerators)
            .flatMap(this::generateFiles)
            .forEach(this::saveFile);

    log.info("Successfully generated files using Errai APT Generators in {}ms", System.currentTimeMillis() - start);
  }

  private void generateAptCompatibleGwtModuleFile(final ExportedTypesFromExportFiles erraiAptExportedTypes) {
    erraiAptExportedTypes.resourceFilesFinder()
            .getResource(erraiAptExportedTypes.erraiAppConfiguration().gwtModuleName().replace(".", "/") + GWT_XML)
            .map(file -> new AptCompatibleGwtModuleFile(file, erraiAptExportedTypes))
            .ifPresent(this::saveFile);
  }

  private Stream<ErraiAptGeneratedSourceFile> generateFiles(final ErraiAptGenerators.Any generator) {
    try {
      return generator.files().stream();
    } catch (final Exception e) {
      // Continues to next generator even when errors occur
      e.printStackTrace();
      return Stream.of();
    }
  }

  private ExportedTypesFromExportFiles newErraiAptExportedTypes(final MetaClass erraiAppAnnotatedMetaClass,
          final Filer filer,
          final Elements elements) {

    log.info("Processing {}", erraiAppAnnotatedMetaClass.getFullyQualifiedName());
    return new ExportedTypesFromExportFiles(erraiAppAnnotatedMetaClass, new AptResourceFilesFinder(filer), elements);
  }

  private Stream<ErraiAptGenerators.Any> createGenerators(final ExportedTypesFromExportFiles erraiAptExportedTypes) {
    return erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiGenerator.class)
            .stream()
            .map(this::loadGeneratorClass)
            .map(generatorClass -> newGenerator(generatorClass, erraiAptExportedTypes))
            .sorted(comparing(ErraiAptGenerators.Any::priority).thenComparing(g -> g.getClass().getSimpleName()));
  }

  @SuppressWarnings("unchecked")
  private Class<? extends ErraiAptGenerators.Any> loadGeneratorClass(final MetaClass metaClass) {
    try {
      // Because we're sure generators will always be pre-compiled, it's safe to get their classes using Class.forName
      return (Class<? extends ErraiAptGenerators.Any>) Class.forName(metaClass.getFullyQualifiedName());
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException(metaClass.getFullyQualifiedName() + " is not an ErraiAptGenerator", e);
    }
  }

  private ErraiAptGenerators.Any newGenerator(final Class<? extends ErraiAptGenerators.Any> generatorClass,
          final ExportedTypesFromExportFiles erraiAptExportedTypes) {
    try {
      final Constructor<? extends ErraiAptGenerators.Any> constructor = generatorClass.getConstructor(
              ExportedTypesFromExportFiles.class);
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
