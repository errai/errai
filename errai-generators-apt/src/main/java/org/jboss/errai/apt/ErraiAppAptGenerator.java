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

import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.common.apt.AnnotatedElementsFinder;
import org.jboss.errai.common.apt.AptAnnotatedElementsFinder;
import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;
import static org.jboss.errai.common.apt.ErraiAptPackages.generatorsPackageElement;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ "org.jboss.errai.common.apt.ErraiApp" })
public class ErraiAppAptGenerator extends AbstractProcessor {

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

    try {
      generateAndSaveSourceFiles(annotations, new AptAnnotatedElementsFinder(roundEnv));
    } catch (final Exception e) {
      System.out.println("Error generating files: " + e.getMessage());
      e.printStackTrace();
    }

    return false;
  }

  void generateAndSaveSourceFiles(final Set<? extends TypeElement> annotations,
          final AnnotatedElementsFinder annotatedElementsFinder) {

    for (final TypeElement erraiAppAnnotation : annotations) {
      System.out.println("Generating files using Errai APT Generators..");

      APTClassUtil.init(processingEnv.getTypeUtils(), processingEnv.getElementUtils());
      ErraiAptExportedTypes.init(processingEnv.getTypeUtils(), processingEnv.getElementUtils(),
              annotatedElementsFinder);

      findGenerators(processingEnv.getElementUtils()).forEach(this::generateAndSaveSourceFile);
      System.out.println("Successfully generated files using Errai APT Generators");
    }
  }

  List<ErraiAptGenerator> findGenerators(final Elements elements) {
    return generatorsPackageElement(elements).map(this::newGenerators).orElseGet(ArrayList::new);
  }

  private List<ErraiAptGenerator> newGenerators(final PackageElement packageElement) {
    return packageElement.getEnclosedElements().stream().map(this::loadClass).map(this::newGenerator).collect(toList());
  }

  @SuppressWarnings("unchecked")
  private Class<? extends ErraiAptGenerator> loadClass(final Element element) {
    try {
      // Because generators will always be pre-compiled, it's safe to get their classes using Class.forName
      return (Class<? extends ErraiAptGenerator>) Class.forName(element.asType().toString());
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException(element.asType().toString() + " is not an ErraiAptGenerator", e);
    }
  }

  private ErraiAptGenerator newGenerator(final Class<? extends ErraiAptGenerator> generatorClass) {
    try {
      final Constructor<? extends ErraiAptGenerator> constructor = generatorClass.getConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (final Exception e) {
      throw new RuntimeException("Class " + generatorClass.getName() + " couldn't be instantiated.", e);
    }
  }

  private void generateAndSaveSourceFile(final ErraiAptGenerator generator) {
    try {
      final String generatedSourceCode = generator.generate();
      saveFile(generatedSourceCode, generator.getPackageName(), generator.getClassSimpleName() + ".java");
    } catch (final IOException e) {
      throw new RuntimeException("Could not write generated file", e);
    }
  }

  private void saveFile(final String generatedSourceCode, final String pkg, final String fileName) throws IOException {
    // By saving .java source files as resources we skip javac compilation. This behavior is desirable since all
    // generated code is client code and will be compiled by the GWT/J2CL compiler.
    // FIXME: errai-marshalling will generate server code too
    final FileObject sourceFile = processingEnv.getFiler().createResource(SOURCE_OUTPUT, pkg, fileName);

    try (final Writer writer = sourceFile.openWriter()) {
      writer.write(generatedSourceCode);
    }
  }
}
