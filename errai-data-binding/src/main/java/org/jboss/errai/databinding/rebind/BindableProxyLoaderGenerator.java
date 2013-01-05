/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.databinding.rebind;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.impl.gwt.GWTUtil;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.BindableProxyLoader;
import org.jboss.errai.databinding.client.BindableProxyProvider;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;
import org.jboss.errai.databinding.client.api.InitialState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;

/**
 * Generates the proxy loader for {@link Bindable}s.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BindableProxyLoaderGenerator extends Generator {
  private final Logger log = LoggerFactory.getLogger(BindableProxyLoaderGenerator.class);

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName)
      throws UnableToCompleteException {

    String packageName = null;
    String className = null;

    try {
      GWTUtil.populateMetaClassFactoryFromTypeOracle(context, logger);
      JClassType classType = context.getTypeOracle().getType(typeName);
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      PrintWriter printWriter = context.tryCreate(logger, packageName, className);
      // If code has not already been generated.
      if (printWriter != null) {
        printWriter.append(generate(context, className));
        context.commit(logger, printWriter);
      }
    }
    catch (Throwable e) {
      logger.log(TreeLogger.ERROR, "Error generating data-binding extensions", e);
    }

    return packageName + "." + className;
  }

  private String generate(final GeneratorContext context, final String className) {
    final File fileCacheDir = RebindUtils.getErraiCacheDir();
    final File cacheFile = new File(fileCacheDir.getAbsolutePath() + "/" + className + ".java");

    log.info("generating bindable proxy loader class.");
    final String gen = generate(context);
    RebindUtils.writeStringToFile(cacheFile, gen);

    return gen;
  }

  private String generate(final GeneratorContext context) {
    ClassStructureBuilder<?> classBuilder = ClassBuilder.implement(BindableProxyLoader.class);
    MethodBlockBuilder<?> loadProxies = classBuilder.publicMethod(void.class, "loadBindableProxies");

    Collection<MetaClass> annotatedBindableTypes =
        ClassScanner.getTypesAnnotatedWith(Bindable.class, RebindUtils.findTranslatablePackages(context));
    
    Set<MetaClass> bindableTypes = new HashSet<MetaClass>(annotatedBindableTypes);
    bindableTypes.addAll(getBindableTypesFromErraiAppProperties());

    for (MetaClass bindable : bindableTypes) {
      if (bindable.isFinal()) {
        throw new RuntimeException("@Bindable type cannot be final: " + bindable.getFullyQualifiedName());
      }

      if (bindable.getDeclaredConstructor() == null || !bindable.getDeclaredConstructor().isPublic()) {
        throw new RuntimeException("@Bindable type needs a public default no-arg constructor: "
            + bindable.getFullyQualifiedName());
      }

      ClassStructureBuilder<?> bindableProxy = new BindableProxyGenerator(bindable).generate();
      loadProxies.append(new InnerClass(bindableProxy.getClassDefinition()));
      Statement proxyProvider =
          ObjectBuilder.newInstanceOf(BindableProxyProvider.class)
              .extend()
              .publicOverridesMethod("getBindableProxy", Parameter.of(Object.class, "model"),
                  Parameter.of(InitialState.class, "state"))
              .append(Stmt.nestedCall(Stmt.newObject(bindableProxy.getClassDefinition())
                  .withParameters(Cast.to(bindable, Stmt.loadVariable("model")), Variable.get("state"))).returnValue())
              .finish()
              .publicOverridesMethod("getBindableProxy", Parameter.of(InitialState.class, "state"))
              .append(
                  Stmt.nestedCall(
                      Stmt.newObject(bindableProxy.getClassDefinition()).withParameters(Variable.get("state")))
                      .returnValue())
              .finish()
              .finish();
      loadProxies.append(Stmt.invokeStatic(BindableProxyFactory.class, "addBindableProxy", bindable, proxyProvider));
    }

    generateDefaultConverterRegistrations(loadProxies, context);

    classBuilder = (ClassStructureBuilder<?>) loadProxies.finish();
    return classBuilder.toJavaString();
  }

  /**
   * Reads all bindable types from all ErraiApp.properties files on the classpath.
   * 
   * @return a set of meta classes representing the configured bindable types.
   */
  private Set<MetaClass> getBindableTypesFromErraiAppProperties() {
    final Set<MetaClass> bindableTypes = new HashSet<MetaClass>();

    final Enumeration<URL> erraiAppProperties = EnvUtil.getErraiAppProperties();
    while (erraiAppProperties.hasMoreElements()) {
      InputStream inputStream = null;
      try {
        final URL url = erraiAppProperties.nextElement();
        log.debug("Checking " + url.getFile() + " for bindable types...");
        inputStream = url.openStream();

        final ResourceBundle props = new PropertyResourceBundle(inputStream);
        for (final String key : props.keySet()) {
          if (key.equals("errai.ui.bindableTypes")) {
            for (final String s : props.getString(key).split(" ")) {
              try {
                bindableTypes.add(MetaClassFactory.get(s.trim()));
              }
              catch (Exception e) {
                throw new RuntimeException("Could not find class defined in ErraiApp.properties as bindable type: " + s);
              }
            }
            break;
          }
        }
      }
      catch (IOException e) {
        throw new RuntimeException("Error reading ErraiApp.properties", e);
      }
      finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          }
          catch (IOException e) {
            log.warn("Failed to close input stream", e);
          }
        }
      }
    }
    return bindableTypes;
  }

  private void generateDefaultConverterRegistrations(final MethodBlockBuilder<?> loadProxies,
      final GeneratorContext context) {

    for (MetaClass converter : ClassScanner.getTypesAnnotatedWith(DefaultConverter.class,
        RebindUtils.findTranslatablePackages(context))) {

      Statement registerConverterStatement = null;
      for (MetaClass iface : converter.getInterfaces()) {
        if (iface.getErased().equals(MetaClassFactory.get(Converter.class))) {
          MetaParameterizedType parameterizedInterface = iface.getParameterizedType();
          if (parameterizedInterface != null) {
            MetaType[] typeArgs = parameterizedInterface.getTypeParameters();
            if (typeArgs != null && typeArgs.length == 2) {
              registerConverterStatement = Stmt.invokeStatic(Convert.class, "registerDefaultConverter",
                  typeArgs[0], typeArgs[1], Stmt.newObject(converter));
            }
          }
        }
      }

      if (registerConverterStatement != null) {
        loadProxies.append(registerConverterStatement);
      }
      else {
        log.warn("Ignoring @DefaultConverter: " + converter
            + "! Make sure it implements Converter and specifies type arguments for the model and widget type");
      }
    }
  }
}