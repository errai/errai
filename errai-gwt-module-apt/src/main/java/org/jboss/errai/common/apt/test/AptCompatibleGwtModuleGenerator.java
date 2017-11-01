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

package org.jboss.errai.common.apt.test;

import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.AptAnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.AptResourceFilesFinder;
import org.jboss.errai.common.apt.ErraiAptCompatible;
import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ResourceFilesFinder;
import org.jboss.errai.common.apt.configuration.AptErraiAppConfiguration;
import org.jboss.errai.common.configuration.ErraiModule;
import org.jboss.errai.config.ErraiAppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("org.jboss.errai.common.apt.ErraiAptCompatible")
public class AptCompatibleGwtModuleGenerator extends AbstractProcessor {

  private static final String GWT_XML = ".gwt.xml";
  private static final Logger log = LoggerFactory.getLogger(AptCompatibleGwtModuleGenerator.class);

  private ResourceFilesFinder resourceFilesFinder;
  private AnnotatedSourceElementsFinder annotatedSourceElementsFinder;

  @Override
  public synchronized void init(final ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

    for (final TypeElement erraiAptCompatibleTestAnnotation : annotations) {

      resourceFilesFinder = new AptResourceFilesFinder(processingEnv.getFiler());
      annotatedSourceElementsFinder = new AptAnnotatedSourceElementsFinder(roundEnv);

      roundEnv.getElementsAnnotatedWith(ErraiAptCompatible.class)
              .stream()
              .map(s -> new APTClass(s.asType()))
              .map(s -> s.getAnnotation(ErraiAptCompatible.class))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .forEach(this::generateAptCompatibleGwtTestModuleFile);
    }

    return false;
  }

  private void generateAptCompatibleGwtTestModuleFile(final MetaAnnotation erraiAptCompatibleTestAnnotation) {
    final String gwtModuleName = erraiAptCompatibleTestAnnotation.value("gwtModuleName");
    final Optional<File> gwtTestModuleFileOptional = resourceFilesFinder.getResource(
            gwtModuleName.replace(".", "/") + GWT_XML);

    if (!gwtTestModuleFileOptional.isPresent()) {
      log.info("Module {} was not found present. Expect test failures.", gwtModuleName);
      return;
    }

    final File gwtTestModuleFile = gwtTestModuleFileOptional.get();
    final List<String> moduleContent = getFileContent(gwtTestModuleFile);
    final String modifiedModuleContent = overrideRebindRules(moduleContent, erraiAptCompatibleTestAnnotation);
    writeNewGwtModuleFile(gwtModuleName, modifiedModuleContent);
  }

  private String overrideRebindRules(final List<String> fileContent,
          final MetaAnnotation erraiAptCompatibleTestAnnotation) {

    final List<String> newFileContent = new ArrayList<>(fileContent);
    final String lastLine = fileContent.get(fileContent.size() - 1);

    newFileContent.remove(fileContent.size() - 1);
    newFileContent.addAll(modulesOverridingRebindRules(erraiAptCompatibleTestAnnotation));
    newFileContent.add("<set-property name=\"errai.useAptGenerators\" value=\"true\" />");
    newFileContent.add(lastLine);

    return newFileContent.stream().reduce((a, b) -> a + "\n" + b).orElse("");
  }

  private List<String> modulesOverridingRebindRules(final MetaAnnotation erraiAptCompatibleTestAnnotation) {
    final MetaClass erraiAppAnnotatedMetaClass = erraiAptCompatibleTestAnnotation.value("erraiApp");

    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(erraiAppAnnotatedMetaClass,
            processingEnv.getTypeUtils(), processingEnv.getElementUtils(), annotatedSourceElementsFinder,
            resourceFilesFinder);

    final ErraiAppConfiguration erraiAppConfiguration = new AptErraiAppConfiguration(erraiAppAnnotatedMetaClass);

    return erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiModule.class)
            .stream()
            .map(m -> getOverridingBindingRules(m, erraiAppConfiguration))
            .filter(s -> !s.isEmpty())
            .collect(toList());
  }

  private String getOverridingBindingRules(final MetaClass erraiModuleAnnotatedMetaClass,
          final ErraiAppConfiguration erraiAppConfiguration) {

    final String name = erraiModuleAnnotatedMetaClass.getName();
    if (name.equals("ErraiMarshallingModule")) {
      return "<replace-with class=\"org.jboss.errai.marshalling.client.api."
              + erraiAppConfiguration.namespace()
              + "MarshallerFactoryImpl\">\n"
              + "<when-type-assignable class=\"org.jboss.errai.marshalling.client.api.MarshallerFactory\" />\n"
              + "<when-property-is name=\"errai.useAptGenerators\" value=\"true\" />\n"
              + "</replace-with>";
    }

    if (name.equals("ErraiBusModule")) {
      return "<replace-with class=\"org.jboss.errai.bus.client.local."
              + erraiAppConfiguration.namespace()
              + "RpcProxyLoaderImpl\">\n"
              + "<when-type-assignable class=\"org.jboss.errai.bus.client.local.RpcProxyLoader\"/>\n"
              + "<when-property-is name=\"errai.useAptGenerators\" value=\"true\"/>\n"
              + "</replace-with>";
    }

    if (name.equals("ErraiIocModule")) {
      return "<replace-with class=\"org.jboss.errai.ioc.client."
              + erraiAppConfiguration.namespace()
              + "BootstrapperImpl\">\n"
              + "<when-type-assignable class=\"org.jboss.errai.ioc.client.Bootstrapper\"/>\n"
              + "<when-property-is name=\"errai.useAptGenerators\" value=\"true\"/>\n"
              + "</replace-with>"
              + ""
              + "<replace-with class=\"org.jboss.errai.ioc.client."
              + erraiAppConfiguration.namespace()
              + "QualifierEqualityFactoryImpl\">\n"
              + "<when-type-assignable class=\"org.jboss.errai.ioc.client.QualifierEqualityFactory\"/>\n"
              + "<when-property-is name=\"errai.useAptGenerators\" value=\"true\" />\n"
              + "</replace-with>"
              + ""
              + "<replace-with class=\"org.jboss.errai.ioc.client.container."
              + erraiAppConfiguration.namespace()
              + "IOCEnvironmentImpl\">\n"
              + "<when-type-assignable class=\"org.jboss.errai.ioc.client.container.IOCEnvironment\"/>\n"
              + "<when-property-is name=\"errai.useAptGenerators\" value=\"true\" />\n"
              + "</replace-with>";
    }

    if (name.equals("ErraiCdiSharedModule")) {
      return "<replace-with class=\"org.jboss.errai.enterprise.client.cdi."
              + erraiAppConfiguration.namespace()
              + "EventQualifierSerializerImpl\">\n"
              + "<when-type-is class=\"org.jboss.errai.enterprise.client.cdi.EventQualifierSerializer\"/>\n"
              + "<when-property-is name=\"errai.useAptGenerators\" value=\"true\"/>\n"
              + "</replace-with>";
    }

    if (name.equals("ErraiDataBindingModule")) {
      return "<replace-with class=\"org.jboss.errai.databinding.client.local."
              + erraiAppConfiguration.namespace()
              + "BindableProxyLoaderImpl\">\n"
              + "<when-type-assignable class=\"org.jboss.errai.databinding.client.local.BindableProxyLoader\"/>\n"
              + "<when-property-is name=\"errai.useAptGenerators\" value=\"true\"/>\n"
              + "</replace-with>";
    }

    if (name.equals("ErraiNavigationModule")) {
      return "<replace-with class=\"org.jboss.errai.ui.nav.client.local.spi."
              + erraiAppConfiguration.namespace()
              + "GeneratedNavigationGraph\">\n"
              + "<when-type-is class=\"org.jboss.errai.ui.nav.client.local.spi.NavigationGraph\"/>\n"
              + "<when-property-is name=\"errai.useAptGenerators\" value=\"true\"/>\n"
              + "</replace-with>";
    }

    if (name.equals("ErraiJaxrsModule")) {
      return "<replace-with class=\"org.jboss.errai.enterprise.client.local."
              + erraiAppConfiguration.namespace()
              + "JaxrsProxyLoaderImpl\">\n"
              + "<when-type-assignable class=\"org.jboss.errai.enterprise.client.local.JaxrsProxyLoader\" />\n"
              + "<when-property-is name=\"errai.useAptGenerators\" value=\"true\" />\n"
              + "</replace-with>";
    }

    return "";
  }

  private void writeNewGwtModuleFile(final String gwtModuleFilePath, final String fileContent) {

    final int lastDot = gwtModuleFilePath.lastIndexOf(".");
    final String fileName = gwtModuleFilePath.substring(lastDot + 1) + GWT_XML;
    final String packageName = gwtModuleFilePath.substring(0, lastDot);

    try {
      // By writing to CLASS_OUTPUT we overwrite the original .gwt.xml file
      final FileObject sourceFile = processingEnv.getFiler().createResource(CLASS_OUTPUT, packageName, fileName);
      try (final Writer writer = sourceFile.openWriter()) {
        writer.write(fileContent);
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to write file " + gwtModuleFilePath);
    }
  }

  private List<String> getFileContent(final File gwtModuleFile) {
    try {
      return Files.readAllLines(gwtModuleFile.toPath())
              .stream()
              .map(String::trim)
              .filter(s -> !StringUtils.isBlank(s))
              .collect(toList());
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read file " + gwtModuleFile.toURI());
    }
  }

}
