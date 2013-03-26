/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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
package org.jboss.errai.ui.rebind;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.reflections.Configuration;
import org.jboss.errai.reflections.Reflections;
import org.jboss.errai.reflections.scanners.ResourcesScanner;
import org.jboss.errai.reflections.util.ClasspathHelper;
import org.jboss.errai.reflections.util.ConfigurationBuilder;
import org.jboss.errai.reflections.util.FilterBuilder;
import org.jboss.errai.ui.shared.MessageBundle;
import org.jboss.errai.ui.shared.MessageBundleUtil;
import org.jboss.errai.ui.shared.api.annotations.Bundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.resources.client.TextResource;

/**
 * Generates the code required for i18n message bundles.
 * @author eric.wittmann@redhat.com
 */
@CodeDecorator
public class DecoratorBundle extends IOCDecoratorExtension<Bundle> {

  private static final String CONSTRUCTED_BUNDLES_KEY = "constructedBundles";

  /**
   * Constructor.
   * @param decoratesWith
   */
  public DecoratorBundle(final Class<Bundle> decoratesWith) {
    super(decoratesWith);
  }

  /**
   * @see org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension#generateDecorator(org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance)
   */
  @Override
  public List<? extends Statement> generateDecorator(final InjectableInstance<Bundle> instance) {
    final List<Statement> bundleInitStatements = new ArrayList<Statement>();
    generateBundleInitialization(instance, bundleInitStatements);
    final Statement initCallback = InjectUtil.createInitializationCallback(instance.getEnclosingType(),
            "obj", bundleInitStatements);
    return Collections.singletonList(Stmt.loadVariable("context").invoke("addInitializationCallback",
            Refs.get(instance.getInjector().getInstanceVarName()), initCallback));
  }

  /**
   * Generate the actual construction logic for our {@link Bundle}.
   */
  private void generateBundleInitialization(
          final InjectableInstance<Bundle> ctx, final List<Statement> initStmts) {

    // Get the root name of the bundle
    final String bundlePath = getMessageBundlePath(ctx);
    // Now get all files in the message bundle (all localized versions)
    MessageBundleScanner scanner = new MessageBundleScanner(
            new ConfigurationBuilder()
            .filterInputsBy(new FilterBuilder().include(".*json"))
            .setUrls(ClasspathHelper.forClassLoader())
            .setScanners(new MessageBundleResourceScanner(bundlePath)));
    Collection<String> resources = scanner.getStore().get(MessageBundleResourceScanner.class).values();
    // If we didn't find at least the specified root bundle file, that's a problem.
    if (!resources.contains(bundlePath)) {
      throw new GenerationException("Missing i18n bundle (specified in @Bundle): " + bundlePath);
    }
    System.out.println("******* RESOURCES: " + resources);

    // Get the map of already-constructed bundles
    final Map<String, BuildMetaClass> constructedBundles = getConstructedBundleTypes(ctx);
    // Create a bundle resource interface if one does not yet exist
    if (!constructedBundles.containsKey(bundlePath)) {
      // Generate this component's ClientBundle resource
      BuildMetaClass messageBundleResourceInterface = generateMessageBundleResourceInterface(ctx);
      constructedBundles.put(bundlePath, messageBundleResourceInterface);
      // Instantiate the ClientBundle MessageBundle resource
      final String msgBundleVarName = InjectUtil.getUniqueVarName();
      initStmts.add(Stmt.declareVariable(messageBundleResourceInterface).named(msgBundleVarName)
              .initializeWith(Stmt.invokeStatic(GWT.class, "create", messageBundleResourceInterface)));

      // Create a dictionary from the message bundle and register it.
      initStmts.add(Stmt.invokeStatic(MessageBundleUtil.class, "registerDictionary", Stmt
                  .loadVariable(msgBundleVarName).invoke("getContents").invoke("getText")));
    }
  }

  /**
   * Create an inner interface for the given {@link MessageBundle} class and its
   * corresponding JSON resource.
   * @param ctx
   */
  private BuildMetaClass generateMessageBundleResourceInterface(final InjectableInstance<Bundle> ctx) {
    final ClassStructureBuilder<?> componentMessageBundleResource = ClassBuilder
            .define(getMessageBundleTypeName(ctx)).publicScope()
            .interfaceDefinition().implementsInterface(MessageBundle.class)
            .implementsInterface(ClientBundle.class).body()
            .publicMethod(TextResource.class, "getContents")
            .annotatedWith(new Source() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return Source.class;
              }
              @Override
              public String[] value() {
                return new String[] { getMessageBundlePath(ctx) };
              }
            }).finish();
    BuildMetaClass classDef = componentMessageBundleResource.getClassDefinition();
    ctx.getInjectionContext()
            .getProcessingContext()
            .getBootstrapClass()
            .addInnerClass(
                    new InnerClass(classDef));

    return classDef;
  }

  /**
   * Get a map of all previously constructed {@link MessageBundle} object types
   */
  @SuppressWarnings("unchecked")
  private Map<String, BuildMetaClass> getConstructedBundleTypes(final InjectableInstance<Bundle> ctx) {
    Map<String, BuildMetaClass> result = (Map<String, BuildMetaClass>) ctx
            .getInjectionContext().getAttribute(CONSTRUCTED_BUNDLES_KEY);
    if (result == null) {
      result = new LinkedHashMap<String, BuildMetaClass>();
      ctx.getInjectionContext().setAttribute(CONSTRUCTED_BUNDLES_KEY, result);
    }
    return result;
  }

  /*
   * Non-generation utility methods.
   */

  /**
   * Gets the bundle name from the @Bundle annotation.
   * @param ctx
   */
  private String getMessageBundlePath(InjectableInstance<Bundle> ctx) {
    String name = ctx.getAnnotation().value();
    if (name == null) {
      throw new GenerationException("@Bundle's bundle name must not be null].");
    }
    // Absolute path vs. relative path.
    if (name.startsWith("/")) {
      return name.substring(1);
    } else {
      MetaClass type = ctx.getEnclosingType();
      String packageName = type.getPackageName();
      return packageName.replace('.', '/') + "/" + name;
    }
  }

  /**
   * Gets the name of the {@link MessageBundle} class.
   * @param ctx
   */
  private String getMessageBundleTypeName(final InjectableInstance<Bundle> ctx) {
    String bundlePath = getMessageBundlePath(ctx);
    String typeName = bundlePath.replace(".json", "MessageBundleResource").replace('/', '.').replace('-', '_').replace('.', '_');
    return typeName;
  }

  /**
   * A scanner that finds i18n message bundles.
   * @author eric.wittmann@redhat.com
   */
  private static class MessageBundleResourceScanner extends ResourcesScanner {
    private String bundlePrefix;
    private String bundleSuffix = ".json";
    /**
     * Constructor.
     * @param bundlePath
     */
    public MessageBundleResourceScanner(String bundlePath) {
      this.bundlePrefix = bundlePath.substring(0, bundlePath.lastIndexOf(".json")).replace('/', '.');
    }

    /**
     * @see org.jboss.errai.reflections.scanners.ResourcesScanner#acceptsInput(java.lang.String)
     */
    @Override
    public boolean acceptsInput(String file) {
      return file != null && file.startsWith(this.bundlePrefix) && file.endsWith(this.bundleSuffix);
    }
  }

  /**
   * Scanner used to find i18n message bundles on the classpath.
   *
   * @author eric.wittmann@redhat.com
   */
  private static class MessageBundleScanner extends Reflections {

    /**
     * Constructor.
     * @param config
     */
    public MessageBundleScanner(Configuration config) {
      super(config);
      scan();
    }

  }

}