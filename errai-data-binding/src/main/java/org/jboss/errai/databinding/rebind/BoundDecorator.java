/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.shared.api.annotations.Bound;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Generates an {@link InitializationCallback} that contains automatic binding logic.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@CodeDecorator
public class BoundDecorator extends IOCDecoratorExtension<Bound> {

  final Map<MetaClass, BlockBuilder<AnonymousClassStructureBuilder>> initBlockCache =
      new ConcurrentHashMap<MetaClass, BlockBuilder<AnonymousClassStructureBuilder>>();

  public BoundDecorator(Class<Bound> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<Bound> ctx) {
    final MetaClass targetClass = ctx.getTargetInjector().getInjectedType();
    final List<Statement> statements = new ArrayList<Statement>();
    BlockBuilder<AnonymousClassStructureBuilder> initBlock = initBlockCache.get(targetClass);

    // Ensure private accessors are generated for bound widget fields
    ctx.ensureMemberExposed();

    final DataBindingUtil.DataBinderRef binderLookup = DataBindingUtil.lookupDataBinderRef(ctx);
    if (binderLookup != null) {
      // Generate a reference to the bean's @AutoBound data binder
      if (initBlock == null) {
        statements.add(Stmt.declareVariable("binder", DataBinder.class, binderLookup.getValueAccessor()));
        statements.add(If.isNull(Refs.get("binder")).append(
                Stmt.throw_(RuntimeException.class, "@AutoBound data binder for class "
                    + ctx.getInjector().getInjectedType()
                    + " has not been initialized. Either initialize or add @Inject!")).finish());
      }

      // Check if the bound property exists in data model type
      Bound bound = ctx.getAnnotation();
      String property = bound.property().equals("") ? ctx.getMemberName() : bound.property();
      if (!DataBindingValidator.isValidPropertyChain(binderLookup.getDataModelType(), property)) {
        throw new GenerationException("Invalid binding of field " + ctx.getMemberName()
            + " in class " + ctx.getInjector().getInjectedType() + "! Property " + property
            + " not resolvable from class " + binderLookup.getDataModelType()
            + "! Hint: Is " + binderLookup.getDataModelType() + " marked as @Bindable? When binding to a "
            + "property chain, all properties but the last in a chain must be of a @Bindable type!");
      }

      Statement widget = ctx.getValueStatement();
      // Ensure the @Bound field or method provides a widget or DOM element
      MetaClass widgetType = ctx.getElementTypeOrMethodReturnType();
      if (widgetType.isAssignableTo(Widget.class)) {
        // Ensure @Bound widget field is initialized
        if (!ctx.isAnnotationPresent(Inject.class) && ctx.getField() != null && widgetType.isDefaultInstantiable()) {
          Statement widgetInit = Stmt.invokeStatic(
              ctx.getInjectionContext().getProcessingContext().getBootstrapClass(),
              PrivateAccessUtil.getPrivateFieldInjectorName(ctx.getField()),
              Refs.get(ctx.getInjector().getInstanceVarName()),
              ObjectBuilder.newInstanceOf(widgetType));

          statements.add(If.isNull(widget).append(widgetInit).finish());
        }
      }
      else if (widgetType.isAssignableTo(Element.class)) {
        widget = Stmt.invokeStatic(ElementWrapperWidget.class, "getWidget", widget);
      }
      else {
        throw new GenerationException("@Bound field or method " + ctx.getMemberName()
            + " in class " + ctx.getInjector().getInjectedType()
            + " must provide a widget or DOM element type but provides: "
            + widgetType.getFullyQualifiedName());
      }


      // Generate the binding
      Statement conv = bound.converter().equals(Bound.NO_CONVERTER.class) ? null : Stmt.newObject(bound.converter());
      Statement onKeyUp = Stmt.load(bound.onKeyUp());
      statements.add(Stmt.loadVariable("binder").invoke("bind", widget, property, conv, onKeyUp));
    }
    else {
      throw new GenerationException("No @Model or @AutoBound data binder found for @Bound field or method "
          + ctx.getMemberName() + " in class " + ctx.getInjector().getInjectedType());
    }

    // The first decorator to run will generate the initialization callback, the subsequent
    // decorators (for other bound widgets of the same class) will just amend the block.
    if (initBlock == null) {
      initBlock = createInitCallback(ctx.getEnclosingType(), "obj");
      initBlockCache.put(targetClass, initBlock);

      ctx.getTargetInjector().setAttribute(DataBindingUtil.BINDER_MODEL_TYPE_VALUE, binderLookup.getDataModelType());
      ctx.getTargetInjector().addStatementToEndOfInjector(
          Stmt.loadVariable("context").invoke("addInitializationCallback",
                    Refs.get(ctx.getInjector().getInstanceVarName()),
                    initBlock.appendAll(statements).finish().finish()));

      ctx.getTargetInjector().addStatementToEndOfInjector(
          Stmt.loadVariable("context").invoke("addDestructionCallback",
                    Refs.get(ctx.getInjector().getInstanceVarName()),
                    createDestructionCallback(ctx.getEnclosingType(), "obj", binderLookup.getValueAccessor())));
    }
    else {
      initBlock.appendAll(statements);
    }

    return Collections.emptyList();
  }

  /**
   * Generates an anonymous {@link InitializationCallback} that will contain the auto binding logic.
   */
  private BlockBuilder<AnonymousClassStructureBuilder> createInitCallback(final MetaClass type, final String initVar) {
    BlockBuilder<AnonymousClassStructureBuilder> block =
        Stmt.newObject(parameterizedAs(InitializationCallback.class, typeParametersOf(type)))
            .extend()
            .publicOverridesMethod("init", Parameter.of(type, initVar, true));

    return block;
  }

  /**
   * Generates an anonymous {@link DestructionCallback} that will unbind all widgets.
   */
  private Statement createDestructionCallback(final MetaClass type, final String initVar, final Statement binder) {
    List<Statement> destructionStatements = 
      Collections.singletonList((Statement) Stmt.nestedCall(binder).invoke("unbind"));

    return InjectUtil.createDestructionCallback(type, initVar, destructionStatements);
  }
  
}