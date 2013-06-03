package org.jboss.errai.validation.rebind;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.validation.client.AbstractGwtValidatorFactory;
import com.google.gwt.validation.client.impl.AbstractGwtValidator;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.GenerateAsync;

import javax.validation.ValidatorFactory;

/**
 * Generates an implementation of {@link ValidatorFactory} which provides a generated implementation
 * of a GWT {@link javax.validation.Validator}.
 *
 * @author Johannes Barop <jb@barop.de>
 */
@GenerateAsync(ValidatorFactory.class)
public class ValidatorFactoryGenerator extends AbstractAsyncGenerator {

  private final String packageName = "org.jboss.errai.validation.client";
  private final String className = ValidatorFactory.class.getSimpleName() + "Impl";

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    return startAsyncGeneratorsAndWaitFor(ValidatorFactory.class, context, logger, packageName, className);
  }

  @Override
  protected String generate(TreeLogger logger, GeneratorContext context) {
    ClassStructureBuilder<?> validatorInterface = new GwtValidatorGenerator().generate();
    ClassStructureBuilder<?> builder = ClassBuilder
            .define(packageName + "." + className, AbstractGwtValidatorFactory.class)
            .publicScope()
            .body()
            .publicMethod(AbstractGwtValidator.class, "createValidator")
            .append(
                    Stmt.invokeStatic(GWT.class, "create", validatorInterface.getClassDefinition()).returnValue()
            )
            .finish();
    builder.getClassDefinition().addInnerClass(new InnerClass(validatorInterface.getClassDefinition()));

    return builder.toJavaString();
  }

}
