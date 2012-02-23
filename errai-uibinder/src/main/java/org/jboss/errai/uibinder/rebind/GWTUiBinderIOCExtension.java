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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import org.jboss.errai.codegen.framework.InnerClass;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.literal.LiteralFactory;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.client.api.PackageTarget;
import org.jboss.errai.uibinder.client.UiBinderProvider;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.IOCProcessorFactory;
import org.jboss.errai.ioc.rebind.ioc.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.InjectorFactory;
import org.jboss.errai.ioc.rebind.ioc.TypeDiscoveryListener;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
@IOCExtension
public class GWTUiBinderIOCExtension implements IOCExtensionConfigurator {
  @Override
  public void configure(IOCProcessingContext context, InjectorFactory injectorFactory, IOCProcessorFactory procFactory) {

    context.registerTypeDiscoveryListener(new TypeDiscoveryListener() {
       @Override
       public void onDiscovery(final IOCProcessingContext context, final InjectionPoint injectionPoint) {
         if (injectionPoint.getType().isAssignableFrom(UiBinder.class)) {
           MetaClass uiBinderParameterized = MetaClassFactory.parameterizedAs(UiBinder.class,
                   MetaClassFactory
                           .typeParametersOf(injectionPoint.getType().getParameterizedType().getTypeParameters()[0],
                                   injectionPoint.getEnclosingType()));

           BuildMetaClass uiBinderBoilerPlaterIface = ClassBuilder.define(injectionPoint.getEnclosingType().getName()
                   + "UiBinder", uiBinderParameterized)
                   .publicScope().staticClass().interfaceDefinition()
                   .body().getClassDefinition();

           UiTemplate handler = new UiTemplate() {
             @Override
             public String value() {
               return injectionPoint.getEnclosingType().getFullyQualifiedName() + ".ui.xml";
             }

             @Override
             public Class<? extends Annotation> annotationType() {
               return UiTemplate.class;
             }
           };

           PackageTarget packageTarget = new PackageTarget() {
             @Override
             public String value() {
               return injectionPoint.getEnclosingType().getPackageName();
             }

             @Override
             public Class<? extends Annotation> annotationType() {
               return PackageTarget.class;
             }
           };

           uiBinderBoilerPlaterIface.addAnnotation(handler);
           uiBinderBoilerPlaterIface.addAnnotation(packageTarget);

           context.getBootstrapClass().addInnerClass(new InnerClass(uiBinderBoilerPlaterIface));

           String varName = "uiBinderInst_" + injectionPoint.getEnclosingType().getFullyQualifiedName()
                   .replaceAll("\\.", "_");

           if (Boolean.getBoolean("errai.simulatedClient")) {
             context.append(Stmt.declareVariable(UiBinder.class).named(varName).initializeWith(
                     ObjectBuilder.newInstanceOf(uiBinderBoilerPlaterIface)
                             .extend()
                             .publicOverridesMethod("createAndBindUi", Parameter.of(injectionPoint.getEnclosingType(), "w"))
                             .append(Stmt.loadLiteral(null).returnValue())
                             .finish().finish()
             )
             );

           }
           else {

             context.append(Stmt.declareVariable(UiBinder.class).named(varName).initializeWith(
                     Stmt.invokeStatic(GWT.class, "create", LiteralFactory.getLiteral(uiBinderBoilerPlaterIface))
             ));
           }

           context.append(Stmt.invokeStatic(UiBinderProvider.class, "registerBinder",
                   injectionPoint.getEnclosingType(), Refs.get(varName)));
         }

       }
     });
  }

  @Override
  public void afterInitialization(IOCProcessingContext context, InjectorFactory injectorFactory, IOCProcessorFactory procFactory) {
  }
}
