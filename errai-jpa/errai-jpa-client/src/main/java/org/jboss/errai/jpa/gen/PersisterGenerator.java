package org.jboss.errai.jpa.gen;

import javax.persistence.Entity;

import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ClassBuilder;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

public class PersisterGenerator extends Generator {

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
      String typeName) throws UnableToCompleteException {
    return generate(context, logger);
  }

  private String generate(final GeneratorContext context, final TreeLogger logger) {
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    ClassStructureBuilder<?> classBuilder = ClassBuilder.implement(ErraiEntityManager.class);

    //    MethodBlockBuilder<?> loadProxies = classBuilder.publicMethod(void.class, "persist", Object.class);
    for (Class<?> entity : scanner.getTypesAnnotatedWith(Entity.class, RebindUtils.findTranslatablePackages(context))) {

      classBuilder.publicMethod(void.class, persistMethodName(entity));
    }

    String out = classBuilder.toJavaString();

    if (Boolean.getBoolean("errai.codegen.printOut")) {
      System.out.println("---ErraiEntityManager-->");
      System.out.println(out);
      System.out.println("<--ErraiEntityManager---");
    }
    return out;
  }

  // TODO check what the other code generators do for class->method names
  static String persistMethodName(Class<?> forType) {
    return "persist_" + forType.getCanonicalName().replace('.', '_');
  }
}
