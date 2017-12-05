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

import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.configuration.ErraiModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
class AptCompatibleGwtModuleFile {

  private final File file;
  private final ErraiAptExportedTypes erraiAptExportedTypes;
  private final String gwtModuleName;

  public AptCompatibleGwtModuleFile(final File file, final ErraiAptExportedTypes erraiAptExportedTypes) {
    this.file = file;
    this.erraiAptExportedTypes = erraiAptExportedTypes;
    this.gwtModuleName = erraiAptExportedTypes.erraiAppConfiguration().gwtModuleName();
  }

  public String generate() {
    final List<String> fileLines = getFileLines(file);
    final String lastLine = fileLines.get(fileLines.size() - 1);

    fileLines.remove(fileLines.size() - 1);

    fileLines.addAll(erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiModule.class)
            .stream()
            .map(this::getOverridingBindingRules)
            .filter(s -> !s.isEmpty())
            .collect(toList()));

    fileLines.add("<set-property name=\"errai.useAptGenerators\" value=\"true\" />");
    fileLines.add(lastLine);

    return fileLines.stream().reduce((a, b) -> a + "\n" + b).orElse("");
  }

  private List<String> getFileLines(final File gwtModuleFile) {
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

  //FIXME: Place each of this binding rules in its own module with a little more type safety
  private String getOverridingBindingRules(final MetaClass erraiModuleAnnotatedMetaClass) {

    final String name = erraiModuleAnnotatedMetaClass.getName();

    if (name.equals("ErraiMarshallingModule")) {
      return replaceWith("org.jboss.errai.marshalling.client.api.MarshallerFactory",
              "org.jboss.errai.marshalling.client.api.MarshallerFactoryImpl");
    }

    if (name.equals("ErraiBusModule")) {
      return replaceWith("org.jboss.errai.bus.client.local.RpcProxyLoader",
              "org.jboss.errai.bus.client.local.RpcProxyLoaderImpl");
    }

    if (name.equals("ErraiCdiSharedModule")) {
      return replaceWith("org.jboss.errai.enterprise.client.cdi.EventQualifierSerializer",
              "org.jboss.errai.enterprise.client.cdi.EventQualifierSerializerImpl");
    }

    if (name.equals("ErraiDataBindingModule")) {
      return replaceWith("org.jboss.errai.databinding.client.local.BindableProxyLoader",
              "org.jboss.errai.databinding.client.local.BindableProxyLoaderImpl");
    }

    if (name.equals("ErraiNavigationModule")) {
      return replaceWith("org.jboss.errai.ui.nav.client.local.spi.NavigationGraph",
              "org.jboss.errai.ui.nav.client.local.spi.GeneratedNavigationGraph");
    }

    if (name.equals("ErraiJaxrsModule")) {
      return replaceWith("org.jboss.errai.enterprise.client.local.JaxrsProxyLoader",
              "org.jboss.errai.enterprise.client.local.JaxrsProxyLoaderImpl");
    }

    if (name.equals("ErraiIocModule")) {
      String replaceWith = "";

      replaceWith += replaceWith("org.jboss.errai.ioc.client.Bootstrapper",
              "org.jboss.errai.ioc.client.BootstrapperImpl");

      replaceWith += replaceWith("org.jboss.errai.ioc.client.QualifierEqualityFactory",
              "org.jboss.errai.ioc.client.QualifierEqualityFactoryImpl");

      replaceWith += replaceWith("org.jboss.errai.ioc.client.container.IOCEnvironment",
              "org.jboss.errai.ioc.client.container.IOCEnvironmentImpl");

      return replaceWith;
    }

    return "";
  }

  private String replaceWith(final String classToBeReplaced, final String newClass) {

    final int lastDotIndex = newClass.lastIndexOf(".");
    final String newClassFqcn = newClass.substring(0, lastDotIndex)
            + "."
            + erraiAptExportedTypes.erraiAppConfiguration().namespace()
            + newClass.substring(lastDotIndex + 1);

    return "<replace-with class=\""
            + newClassFqcn
            + "\">\n"
            + "<when-type-assignable class=\""
            + classToBeReplaced
            + "\" />\n"
            + "<when-property-is name=\"errai.useAptGenerators\" value=\"true\" />\n"
            + "</replace-with>";
  }

  public String gwtModuleName() {
    return gwtModuleName;
  }
}
