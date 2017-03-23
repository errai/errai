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

package org.jboss.errai.enterprise.processor;

import static java.util.stream.Collectors.toCollection;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static org.jboss.errai.codegen.util.Stmt.invokeStatic;
import static org.jboss.errai.codegen.util.Stmt.nestedCall;
import static org.jboss.errai.codegen.util.Stmt.newObject;
import static org.jboss.errai.enterprise.processor.TypeNames.FEATURE_INTERCEPTOR;
import static org.jboss.errai.enterprise.processor.TypeNames.INTERCEPTED_CALL;
import static org.jboss.errai.enterprise.processor.TypeNames.PATH;
import static org.jboss.errai.enterprise.processor.TypeNames.PROVIDER;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;

import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.apt.APTClass;
import org.jboss.errai.codegen.apt.APTClassUtil;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.ProxyUtil.InterceptorProvider;
import org.jboss.errai.common.client.framework.ProxyProvider;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsProxyLoader;
import org.jboss.errai.enterprise.rebind.JaxrsProxyGenerator;
import org.jboss.errai.enterprise.rebind.Utils;

import com.google.common.collect.Multimap;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@SupportedAnnotationTypes({PATH, FEATURE_INTERCEPTOR, INTERCEPTED_CALL, PROVIDER})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JaxrsProxyLoaderGenerator extends AbstractProcessor {

  private static final String IMPL_FQCN = "org.jboss.errai.enterprise.client.local.JaxrsProxyLoaderImpl";
  private final List<APTClass> jaxrsIfaces = new ArrayList<>();
  private final List<APTClass> featureInterceptors = new ArrayList<>();
  private final List<APTClass> standaloneInterceptors = new ArrayList<>();
  private final List<APTClass> providers = new ArrayList<>();

  @Override
  public synchronized void init(final ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    APTClassUtil.setTypes(processingEnv.getTypeUtils());
    APTClassUtil.setElements(processingEnv.getElementUtils());
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    storeJaxrsIfaces(annotations, roundEnv);
    storeFeatureInterceptors(annotations, roundEnv);
    storeInterceptedCalls(annotations, roundEnv);
    storeProviders(annotations, roundEnv);

    if (roundEnv.processingOver()) {
      ClassStructureBuilder<?> classBuilder = ClassBuilder
        .define(IMPL_FQCN)
        .publicScope()
        .implementsInterface(JaxrsProxyLoader.class)
        .body();
      final MethodBlockBuilder<?> loadProxies = classBuilder.publicMethod(void.class, "loadProxies");
      final InterceptorProvider interceptorProvider = generateInterceptorProvider();
      final Multimap<MetaClass, MetaClass> exceptionMappers = Utils.getClientExceptionMappers(providers);

      jaxrsIfaces
        .stream()
        .forEach(remote -> {
          // create the remote proxy for this interface
          final ClassStructureBuilder<?> remoteProxy =
              new JaxrsProxyGenerator(remote, interceptorProvider, exceptionMappers, Function.identity(), true).generate();
          loadProxies.append(new InnerClass(remoteProxy.getClassDefinition()));

          // create the proxy provider
          final Statement proxyProvider = ObjectBuilder.newInstanceOf(ProxyProvider.class)
              .extend()
              .publicOverridesMethod("getProxy")
              .append(nestedCall(newObject(remoteProxy.getClassDefinition())).returnValue())
              .finish()
              .finish();

          // create the call that registers the proxy provided for the generated proxy
          loadProxies.append(invokeStatic(RemoteServiceProxyFactory.class, "addRemoteProxy", remote, proxyProvider));
        });
      classBuilder = (ClassStructureBuilder<?>) loadProxies.finish();
      final String gen = classBuilder.toJavaString();
      final Filer filer = processingEnv.getFiler();

      try {
        final BuildMetaClass mc = classBuilder.getClassDefinition();
        final FileObject sourceFile = filer.createResource(CLASS_OUTPUT, mc.getPackageName(), mc.getName() + ".java",
                getOriginatingElements());
        try (Writer writer = sourceFile.openWriter()) {
          writer.write(gen);
        }
      } catch (final IOException e) {
        final Messager messager = processingEnv.getMessager();
        messager.printMessage(Kind.ERROR,
                String.format("Unable to generate RpcProxyLoaderGeneratorImpl. Error: %s", e.getMessage()));
      }
    }

    return false;
  }

  private Element[] getOriginatingElements() {
    return Stream
      .of(jaxrsIfaces, featureInterceptors, standaloneInterceptors)
      .flatMap(list -> list.stream())
      .map(aptClass -> aptClass.getEnclosedMetaObject())
      .map(mirror -> processingEnv.getTypeUtils().asElement(mirror))
      .toArray(Element[]::new);
  }

  private InterceptorProvider generateInterceptorProvider() {
    return new InterceptorProvider(featureInterceptors, standaloneInterceptors);
  }

  private void storeProviders(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    // TODO validate types are interfaces and show warnings/errors
    annotations
    .stream()
    .filter(anno -> anno.getQualifiedName().contentEquals(PROVIDER))
    .flatMap(anno -> roundEnv.getElementsAnnotatedWith(anno).stream())
    .map(TypeElement.class::cast)
    .map(TypeElement::asType)
    .map(APTClass::new)
    .filter(mc -> mc.isInterface())
    .collect(toCollection(() -> providers));
  }

  private void storeJaxrsIfaces(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    // TODO validate types are interfaces and show warnings/errors
    annotations
    .stream()
    .filter(anno -> anno.getQualifiedName().contentEquals(PATH))
    .flatMap(anno -> roundEnv.getElementsAnnotatedWith(anno).stream())
    .filter(TypeElement.class::isInstance)
    .map(TypeElement.class::cast)
    .map(TypeElement::asType)
    .map(APTClass::new)
    .filter(mc -> mc.isInterface())
    .collect(toCollection(() -> jaxrsIfaces));
  }

  private void storeFeatureInterceptors(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    // TODO validate types are interfaces and show warnings/errors
    annotations
    .stream()
    .filter(anno -> anno.getQualifiedName().contentEquals(FEATURE_INTERCEPTOR))
    .flatMap(anno -> roundEnv.getElementsAnnotatedWith(anno).stream())
    .map(TypeElement.class::cast)
    .map(TypeElement::asType)
    .map(APTClass::new)
    .collect(toCollection(() -> featureInterceptors));
  }

  private void storeInterceptedCalls(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    // TODO validate types are interfaces and show warnings/errors
    annotations
    .stream()
    .filter(anno -> anno.getQualifiedName().contentEquals(INTERCEPTED_CALL))
    .flatMap(anno -> roundEnv.getElementsAnnotatedWith(anno).stream())
    .filter(element -> TypeKind.DECLARED.equals(element.getKind()))
    .map(TypeElement.class::cast)
    .map(TypeElement::asType)
    .map(APTClass::new)
    .collect(toCollection(() -> standaloneInterceptors));
  }

}
