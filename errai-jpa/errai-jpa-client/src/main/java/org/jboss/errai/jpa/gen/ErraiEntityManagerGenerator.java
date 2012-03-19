package org.jboss.errai.jpa.gen;

import java.io.PrintWriter;

import javax.persistence.Entity;

import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.framework.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Bool;
import org.jboss.errai.codegen.framework.util.Implementations;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

public class ErraiEntityManagerGenerator extends Generator {

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
      String typeName) throws UnableToCompleteException {

    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    ClassStructureBuilder<?> classBuilder = ClassBuilder.implement(ErraiEntityManager.class);

    // mpm = "master persist method"
    MethodBlockBuilder<?> mpm = classBuilder.publicMethod(void.class, "persist", Parameter.of(Object.class, "entity"));

    for (Class<?> entityType : scanner.getTypesAnnotatedWith(Entity.class, RebindUtils.findTranslatablePackages(context))) {
      String pemName = persistEntityMethodName(entityType);

      // create persist entity method ("pem") for this entity
      MethodBlockBuilder<?> pem = classBuilder.publicMethod(void.class, pemName, Parameter.of(entityType, "e"));
      pem.finish();

      // add conditional call to pem from the master persist method
      ContextualStatementBuilder entityAsItsOwnType = Stmt.castTo(entityType, Stmt.loadVariable("entity"));
      mpm.append(Stmt.if_(Bool.instanceOf(Variable.get("entity"), MetaClassFactory.getAsStatement(entityType)))
          .append(Stmt.loadStatic(classBuilder.getClassDefinition(), "this").invoke(pemName, entityAsItsOwnType))
          // TODO: generate return statement here (need updates from master branch)
          .finish());
    }

    ContextualStatementBuilder exceptionMessage =
        Stmt.load(Implementations.newStringBuilder()
            .append(Stmt.loadVariable("entity").invoke("getClass"))
            .append(Stmt.loadLiteral(" is not a known entity type")))
            .invoke("toString");

    mpm.append(Stmt.throw_(IllegalArgumentException.class, exceptionMessage));

    mpm.finish();

    String out = classBuilder.toJavaString();

    if (Boolean.getBoolean("errai.codegen.printOut")) {
      System.out.println("---ErraiEntityManager-->");
      System.out.println(out);
      System.out.println("<--ErraiEntityManager---");
    }

    PrintWriter printWriter = context.tryCreate(
        logger,
        classBuilder.getClassDefinition().getPackageName(),
        classBuilder.getClassDefinition().getName());

    // printWriter is null if code has already been generated.
    if (printWriter != null) {
      printWriter.append(out);
      context.commit(logger, printWriter);
    }

    return classBuilder.getClassDefinition().getFullyQualifiedName();
  }

  // TODO check what the other code generators do for class->method names
  static String persistEntityMethodName(Class<?> forType) {
    return "persist_" + forType.getCanonicalName().replace('.', '_');
  }
}
