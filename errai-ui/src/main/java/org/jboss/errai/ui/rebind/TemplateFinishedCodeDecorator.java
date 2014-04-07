package org.jboss.errai.ui.rebind;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.shared.api.annotations.style.TemplateFinishedBinding;
import org.jboss.errai.ui.shared.api.style.TemplatingFinishedRegistry;

/**
 * ClassDescription for TemplateFinishedCodeDecorator
 * 
 * @author Dennis Schumann <dennis.schumann@devbliss.com>
 */
@CodeDecorator
public class TemplateFinishedCodeDecorator extends
        IOCDecoratorExtension<TemplateFinishedBinding> {
  private static final Map<String, List<String>> alreadyAdded = new HashMap<String, List<String>>();

  public TemplateFinishedCodeDecorator(
          Class<TemplateFinishedBinding> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(
          InjectableInstance<TemplateFinishedBinding> ctx) {
    final Statement valueAccessor;

    switch (ctx.getTaskType()) {
    case Field:
    case PrivateField:
      valueAccessor = InjectUtil.getPublicOrPrivateFieldValue(
              ctx.getInjectionContext(),
              Refs.get(ctx.getInjector().getInstanceVarName()), ctx.getField());
      break;
    default:
      throw new RuntimeException(
              "problem with template finished binding. element target type is invalid: "
                      + ctx.getTaskType() + " . Only Fields are supported.");
    }
    final List<Statement> stmts = new ArrayList<Statement>();

    // Check that it is not already done with this field. That is a workaround
    // to fix that having more than one annotation on a field that uses the
    // TemplateFinishedBinding is called twice with the same instead different
    if (alreadyAdded.containsKey(ctx.getField().getDeclaringClass()
            .getFullyQualifiedName())) {
      if (alreadyAdded.get(
              ctx.getField().getDeclaringClass().getFullyQualifiedName())
              .contains(ctx.getField().getName())) {
        return stmts;
      }
    }
    else {
      alreadyAdded.put(ctx.getField().getDeclaringClass()
              .getFullyQualifiedName(), new ArrayList<String>());
    }

    for (Annotation annotation : ctx.getAnnotations()) {
      if (annotation.annotationType().isAnnotationPresent(
              TemplateFinishedBinding.class)) {
        stmts.add(Stmt.invokeStatic(TemplatingFinishedRegistry.class, "get")
                .invoke("addBeanElement",
                        Refs.get(ctx.getInjector().getInstanceVarName()),
                        Stmt.nestedCall(valueAccessor).invoke("getElement"),
                        annotation));
      }
    }

    alreadyAdded
            .get(ctx.getField().getDeclaringClass().getFullyQualifiedName())
            .add(ctx.getField().getName());

    return stmts;
  }

}
