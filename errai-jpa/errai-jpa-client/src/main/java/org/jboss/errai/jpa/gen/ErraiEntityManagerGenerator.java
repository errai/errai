package org.jboss.errai.jpa.gen;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.SnapshotMaker;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.meta.impl.gwt.GWTUtil;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;

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
    System.out.println("EntityManagerFactory is " + emf);
    EntityManager em = emf.createEntityManager();
    System.out.println("EntityManager is " + em);

    // XXX this is strange: get a NoSuchMethodError on a normal call to getMetamodel, but reflective call works?!
    Metamodel mm;
    try {
      mm = (Metamodel) em.getClass().getMethod("getMetamodel").invoke(em);
      System.out.println("Metamodel via reflection: " + mm);
    } catch (Exception e) {
      UnableToCompleteException ex = new UnableToCompleteException();
      ex.initCause(e);
      throw ex;
    }

    ClassStructureBuilder<?> classBuilder = Implementations.extend(ErraiEntityManager.class, "GeneratedErraiEntityManager");

    // pmm = "populate metamodel method"
    MethodBlockBuilder<?> pmm = classBuilder.packageMethod(void.class, "populateMetamodel", Parameter.of(Object.class, "entity"));

    for (EntityType<?> et : mm.getEntities()) {

      // first, create a variable for the EntityType
      Statement etVariable = Stmt.declareVariable(entitySnapshotVarName(et.getJavaType()), EntityType.class);
      Map<Object, Statement> entityTypeReference = Collections.singletonMap(
          (Object) et, (Statement) Variable.get(entitySnapshotVarName(et.getJavaType())));

      // now, snapshot all the EntityType's attributes, adding them as we go
      List<Statement> attributes = new ArrayList<Statement>();
      Statement id = null;
      Statement version = null;
      for (SingularAttribute<?, ?> attrib : et.getSingularAttributes()) {
        Statement attribSnapshot = SnapshotMaker.makeSnapshotAsSubclass(attrib, SingularAttribute.class, entityTypeReference, EntityType.class);
        if (attrib.isId()) id = attribSnapshot;
        if (attrib.isVersion()) version = attribSnapshot;

        pmm.append(Stmt.declareVariable(et.getName() + "_" + attrib.getName(), attribSnapshot));
      }

      System.out.println("singular attributes of " + et + ": " + attributes);
//      metamodel.addEntityType(new ErraiEntityType(id, version, et.getSupertype()));

      //Stmt.loadVariable("metamodel").invoke("addEntityType", );
      //      pmm.append(Stmt.loadClassMember("metamodel").invoke("addEntityType", et));
    }

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
