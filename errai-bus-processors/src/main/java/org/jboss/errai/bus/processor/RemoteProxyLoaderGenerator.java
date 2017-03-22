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

package org.jboss.errai.bus.processor;

import static java.util.stream.Collectors.toCollection;
import static org.jboss.errai.bus.processor.TypeNames.FEATURE_INTERCEPTOR;
import static org.jboss.errai.bus.processor.TypeNames.INTERCEPTED_CALL;
import static org.jboss.errai.bus.processor.TypeNames.REMOTE;
import static org.jboss.errai.codegen.builder.impl.ObjectBuilder.newInstanceOf;
import static org.jboss.errai.codegen.util.Stmt.invokeStatic;
import static org.jboss.errai.codegen.util.Stmt.nestedCall;
import static org.jboss.errai.codegen.util.Stmt.newObject;

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
import javax.tools.JavaFileObject;

import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.framework.RpcProxyLoader;
import org.jboss.errai.bus.rebind.RpcProxyGenerator;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.apt.APTClass;
import org.jboss.errai.codegen.apt.APTClassUtil;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.codegen.util.ProxyUtil.InterceptorProvider;
import org.jboss.errai.common.client.framework.ProxyProvider;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@SupportedAnnotationTypes({REMOTE, FEATURE_INTERCEPTOR, INTERCEPTED_CALL})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RemoteProxyLoaderGenerator extends AbstractProcessor {

  private final List<APTClass> remoteIfaces = new ArrayList<>();
  private final List<APTClass> featureInterceptors = new ArrayList<>();
  private final List<APTClass> standaloneInterceptors = new ArrayList<>();

  private final List<MetaClass> preCompiledRemoteIfaces = new ArrayList<>();

  @Override
  public synchronized void init(final ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    APTClassUtil.setTypes(processingEnv.getTypeUtils());
    APTClassUtil.setElements(processingEnv.getElementUtils());
    final MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    scanner
      .getTypesAnnotatedWith(Remote.class)
      .stream()
      .map(JavaReflectionClass::newInstance)
      .collect(toCollection(() -> preCompiledRemoteIfaces));
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    storeRemoteIfaces(annotations, roundEnv);
    storeFeatureInterceptors(annotations, roundEnv);
    storeInterceptedCalls(annotations, roundEnv);

    if (roundEnv.processingOver()) {
      ClassStructureBuilder<?> classBuilder = ClassBuilder.implement(RpcProxyLoader.class);
      final MethodBlockBuilder<?> loadProxies =
              classBuilder.publicMethod(void.class, "loadProxies", Parameter.of(MessageBus.class, "bus", true));
      final InterceptorProvider interceptorProvider = generateInterceptorProvider();

      Stream
        .of(remoteIfaces, preCompiledRemoteIfaces)
        .flatMap(list -> list.stream())
        .map(remote -> new RpcProxyGenerator(remote, interceptorProvider, Function.identity(), true))
        .forEach(proxyGenerator -> {
          final ClassStructureBuilder<?> remoteProxy = proxyGenerator.generate();

          // create the proxy provider
          final Statement proxyProvider = newInstanceOf(ProxyProvider.class)
                  .extend()
                  .publicOverridesMethod("getProxy")
                  .append(nestedCall(newObject(remoteProxy.getClassDefinition())).returnValue())
                  .finish()
                  .finish();


          loadProxies.append(new InnerClass(remoteProxy.getClassDefinition()));
          loadProxies.append(invokeStatic(RemoteServiceProxyFactory.class, "addRemoteProxy",
                  proxyGenerator.getRemoteType(), proxyProvider));
      });
      classBuilder = (ClassStructureBuilder<?>) loadProxies.finish();
      final String gen = classBuilder.toJavaString();
      final Filer filer = processingEnv.getFiler();

      try {
        final String fqcn = classBuilder.getClassDefinition().getFullyQualifiedName();
        final JavaFileObject sourceFile = filer.createSourceFile(fqcn, getOriginatingElements());
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
      .of(remoteIfaces, featureInterceptors, standaloneInterceptors)
      .flatMap(list -> list.stream())
      .map(aptClass -> aptClass.getEnclosedMetaObject())
      .map(mirror -> processingEnv.getTypeUtils().asElement(mirror))
      .toArray(Element[]::new);
  }

  private void storeRemoteIfaces(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    // TODO validate types are interfaces and show warnings/errors
    annotations
    .stream()
    .filter(anno -> anno.getQualifiedName().contentEquals(REMOTE))
    .flatMap(anno -> roundEnv.getElementsAnnotatedWith(anno).stream())
    .map(element -> (TypeElement) element)
    .map(TypeElement::asType)
    .map(APTClass::new)
    .filter(mc -> mc.isInterface())
    .collect(toCollection(() -> remoteIfaces));
  }

  private void storeFeatureInterceptors(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    // TODO validate types are interfaces and show warnings/errors
    annotations
    .stream()
    .filter(anno -> anno.getQualifiedName().contentEquals(FEATURE_INTERCEPTOR))
    .flatMap(anno -> roundEnv.getElementsAnnotatedWith(anno).stream())
    .map(element -> (TypeElement) element)
    .map(TypeElement::asType)
    .map(APTClass::new)
    .collect(toCollection(() -> featureInterceptors));
  }

  private void storeInterceptedCalls(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    // TODO validate types are interfaces and show warnings/errors
    annotations
    .stream()
    .filter(anno -> anno.getQualifiedName().contentEquals(TypeNames.REMOTE))
    .flatMap(anno -> roundEnv.getElementsAnnotatedWith(anno).stream())
    .filter(element -> TypeKind.DECLARED.equals(element.getKind()))
    .map(element -> (TypeElement) element)
    .map(TypeElement::asType)
    .map(APTClass::new)
    .collect(toCollection(() -> standaloneInterceptors));
  }

  private InterceptorProvider generateInterceptorProvider() {
    return new InterceptorProvider(featureInterceptors, standaloneInterceptors);
  }

}
