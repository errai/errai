package org.jboss.errai.ui.rebind;

import java.util.ArrayList;
import java.util.List;
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
public class TemplateFinishedCodeDecorator extends IOCDecoratorExtension<TemplateFinishedBinding> {

  public TemplateFinishedCodeDecorator(Class<TemplateFinishedBinding> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<TemplateFinishedBinding> ctx) {
    System.out.println("TemplateFinishedCodeDecorator generateDecorator");
    final Statement valueAccessor;

    switch (ctx.getTaskType()) {
      case Field:
      case PrivateField:
        valueAccessor = InjectUtil.getPublicOrPrivateFieldValue(ctx.getInjectionContext(),
            Refs.get(ctx.getInjector().getInstanceVarName()), ctx.getField());
        break;
      default:
        throw new RuntimeException("problem with template finished binding. element target type is invalid: "
            + ctx.getTaskType() + ". Only Fields are supported.");
    }

    final List<Statement> stmts = new ArrayList<Statement>();

    System.out.println("add TemplateFinishedCodeDecorator statements");
    System.out.println("Annotation is " + ctx.getRawAnnotation().annotationType().getName());
    stmts.add(Stmt.invokeStatic(TemplatingFinishedRegistry.class, "get").invoke("addBeanElement",
        Refs.get(ctx.getInjector().getInstanceVarName()), Stmt.nestedCall(valueAccessor).invoke("getElement"),
        ctx.getRawAnnotation()));

    return stmts;
  }

}
