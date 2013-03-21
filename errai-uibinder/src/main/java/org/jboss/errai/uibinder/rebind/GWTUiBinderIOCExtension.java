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

package org.jboss.errai.uibinder.rebind;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.literal.LiteralFactory;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.client.api.PackageTarget;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCConfigProcessor;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TypeDiscoveryListener;
import org.jboss.errai.uibinder.client.UiBinderProvider;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
@IOCExtension
public class GWTUiBinderIOCExtension implements IOCExtensionConfigurator {
  @Override
  public void configure(final IOCProcessingContext context, final InjectionContext injectionContext, final IOCConfigProcessor procFactory) {

    context.registerTypeDiscoveryListener(new TypeDiscoveryListener() {
      @Override
      public void onDiscovery(final IOCProcessingContext context,
                              final InjectionPoint injectionPoint,
                              final MetaClass type) {
       // final MetaClass type = injectionPoint.getElementTypeOrMethodReturnType();
        final MetaClass enclosingType = injectionPoint.getEnclosingType();

        if (type.isAssignableFrom(UiBinder.class)) {
          MetaClass uiBinderParameterized = MetaClassFactory.parameterizedAs(UiBinder.class,
                  MetaClassFactory
                          .typeParametersOf(type.getParameterizedType().getTypeParameters()[0],
                              enclosingType));

          BuildMetaClass uiBinderBoilerPlaterIface = ClassBuilder.define(enclosingType.getFullyQualifiedName().replaceAll("\\.", "_")
                  + "_UiBinder", uiBinderParameterized)
                  .publicScope().staticClass().interfaceDefinition()
                  .body().getClassDefinition();

          UiTemplate handler = new UiTemplate() {
            @Override
            public String value() {
              return enclosingType.getFullyQualifiedName() + ".ui.xml";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
              return UiTemplate.class;
            }
          };

          PackageTarget packageTarget = new PackageTarget() {
            @Override
            public String value() {
              return enclosingType.getPackageName();
            }

            @Override
            public Class<? extends Annotation> annotationType() {
              return PackageTarget.class;
            }
          };

          uiBinderBoilerPlaterIface.addAnnotation(handler);
          uiBinderBoilerPlaterIface.addAnnotation(packageTarget);

          context.getBootstrapClass().addInnerClass(new InnerClass(uiBinderBoilerPlaterIface));

          final BlockStatement staticInit = context.getBootstrapClass().getStaticInitializer();

          String varName = "uiBinderInst_" + enclosingType.getFullyQualifiedName()
                  .replaceAll("\\.", "_");

          if (Boolean.getBoolean("errai.simulatedClient")) {
            staticInit.addStatement(Stmt.declareVariable(UiBinder.class).named(varName).initializeWith(
                    ObjectBuilder.newInstanceOf(uiBinderBoilerPlaterIface)
                            .extend()
                            .publicOverridesMethod("createAndBindUi", Parameter.of(type, "w"))
                            .append(Stmt.loadLiteral(null).returnValue())
                            .finish().finish()
            )
            );
          }
          else {
            staticInit.addStatement(Stmt.declareVariable(UiBinder.class).named(varName).initializeWith(
                    Stmt.invokeStatic(GWT.class, "create", LiteralFactory.getLiteral(uiBinderBoilerPlaterIface))
            ));
          }

          staticInit.addStatement(Stmt.invokeStatic(UiBinderProvider.class, "registerBinder",
              enclosingType, Refs.get(varName)));
        }
        else if (type.isAssignableTo(SafeHtmlTemplates.class)) {
          final String varName = "safeTemplateInst_" + type.getFullyQualifiedName()
                  .replaceAll("\\.", "_");

          if (Boolean.getBoolean("errai.simulatedClient")) {
            context.append(Stmt.declareVariable(SafeHtmlTemplates.class).named(varName).initializeWith(
                ObjectBuilder.newInstanceOf(type)
                    .extend()
                    .publicOverridesMethod("link", Parameter.of(SafeUri.class, "safe"),
                        Parameter.of(String.class, "str"))
                    .append(Stmt.loadLiteral(null).returnValue())
                    .finish().finish()
            )
            );

          }
          else {
            context.append(Stmt.declareVariable(type).named(varName).initializeWith(
                Stmt.invokeStatic(GWT.class, "create", LiteralFactory.getLiteral(type))
            ));
          }


          injectionContext.registerInjector(new AbstractInjector() {
            @Override
            public void renderProvider(InjectableInstance injectableInstance) {
            }

            @Override
            public Statement getBeanInstance(InjectableInstance injectableInstance) {
              return Refs.get(varName);
            }

            @Override
            public boolean isRendered() {
              return false;
            }

            @Override
            public boolean isSingleton() {
              return false;
            }

            @Override
            public boolean isPseudo() {
              return false;
            }

            @Override
            public String getInstanceVarName() {
              return varName;
            }

            @Override
            public MetaClass getInjectedType() {
              return type;
            }
          });
        }
      }
    });
  }

  @Override
  public void afterInitialization(IOCProcessingContext context, InjectionContext injectionContext, IOCConfigProcessor procFactory) {
  }
}
