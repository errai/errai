package org.jboss.errai.jpa.gen;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.framework.util.Implementations;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

public class ErraiEntityManagerGenerator extends Generator {

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
      String typeName) throws UnableToCompleteException {

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

      SingularAttribute<?, ?> id = null;
      SingularAttribute<?, ?> version = null;
      for (SingularAttribute<?, ?> attrib : et.getSingularAttributes()) {
        if (attrib.isId()) id = new ErraiSingularAttribute(et.getName(), et.get);
        if (attrib.isVersion()) version = attrib;
      }



      metamodel.addEntityType(new ErraiEntityType(id, version, et.getSupertype()));

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
  static String persistEntityMethodName(Class<?> forType) {
    return "persist_" + forType.getCanonicalName().replace('.', '_');
  }


}
