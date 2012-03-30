package org.jboss.errai.jpa.gen;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.jboss.errai.codegen.SnapshotMaker;
import org.jboss.errai.codegen.SnapshotMaker.MethodBodyCallback;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.impl.gwt.GWTUtil;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.client.local.ErraiEntityType;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

public class ErraiEntityManagerGenerator extends Generator {

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
      String typeName) throws UnableToCompleteException {

    GWTUtil.populateMetaClassFactoryFromTypeOracle(context, logger);

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    properties.put("javax.persistence.validation.mode", "none");
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("ErraiJpaClientTests", properties);
    EntityManager em = emf.createEntityManager();
    Metamodel mm = em.getMetamodel();

    ClassStructureBuilder<?> classBuilder = Implementations.extend(ErraiEntityManager.class, "GeneratedErraiEntityManager");

    // pmm = "populate metamodel method"
    MethodBlockBuilder<?> pmm = classBuilder.protectedMethod(void.class, "populateMetamodel");

    for (final EntityType<?> et : mm.getEntities()) {

      // first, create a variable for the EntityType
      pmm.append(Stmt.codeComment(
          "**\n" +
          "** EntityType for " + et.getJavaType().getName() + "\n" +
          "**"));
      String entityTypeVarName = entitySnapshotVarName(et.getJavaType());
      pmm.append(Stmt.declareVariable(ErraiEntityType.class).asFinal()
          .named(entityTypeVarName)
          .initializeWith(Stmt.newObject(ErraiEntityType.class).withParameters(et.getName(), et.getJavaType())));

      MethodBodyCallback methodBodyCallback = new MethodBodyCallback() {

        @Override
        public Statement generateMethodBody(MetaMethod method, Object o) {
          // provide reference to declaring type (et) from its attributes
          if (o instanceof SingularAttribute
                  && method.getName().equals("getDeclaringType")
                  && ((SingularAttribute<?, ?>) o).getDeclaringType() == et) {
            return Stmt.loadVariable(entitySnapshotVarName(et.getJavaType())).returnValue();
          }
          return null;
        }
      };

      // now, snapshot all the EntityType's attributes, adding them as we go
      List<Statement> attributes = new ArrayList<Statement>();
      for (SingularAttribute<?, ?> attrib : et.getSingularAttributes()) {
        Statement attribSnapshot = SnapshotMaker.makeSnapshotAsSubclass(
            attrib, SingularAttribute.class, methodBodyCallback,
            EntityType.class, ManagedType.class, Type.class);
        pmm.append(Stmt.loadVariable(entityTypeVarName).invoke("addAttribute", attribSnapshot));
      }

      pmm.append(Stmt.loadVariable("metamodel").invoke("addEntityType", Variable.get(entityTypeVarName)));
      System.out.println("singular attributes of " + et + ": " + attributes);
//      metamodel.addEntityType(new ErraiEntityType(id, version, et.getSupertype()));

      //Stmt.loadVariable("metamodel").invoke("addEntityType", );
      //      pmm.append(Stmt.loadClassMember("metamodel").invoke("addEntityType", et));
    }

    pmm.append(Stmt.loadVariable("metamodel").invoke("freeze"));
    pmm.finish();

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
  static String entitySnapshotVarName(Class<?> forType) {
    return "et_" + forType.getCanonicalName().replace('.', '_');
  }


}
