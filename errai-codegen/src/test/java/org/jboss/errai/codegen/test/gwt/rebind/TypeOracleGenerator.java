package org.jboss.errai.codegen.test.gwt.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.test.gwt.client.TestInterface;
import org.jboss.errai.codegen.test.gwt.client.TypeOracleBootstrap;
import org.jboss.errai.codegen.test.gwt.client.TypeWithNestedClass;

import java.io.PrintWriter;

/**
 * @author Mike Brock
 */
public class TypeOracleGenerator extends Generator {
  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {

    try {
      JClassType testInterface = context.getTypeOracle().getType(TestInterface.class.getName());
      JClassType typeWithNType = context.getTypeOracle().getType(TypeWithNestedClass.class.getName());

      MetaClassFactory.emptyCache();
      MetaClassFactory.pushCache(GWTClass.newInstance(context.getTypeOracle(), testInterface));
      MetaClassFactory.pushCache(GWTClass.newInstance(context.getTypeOracle(), typeWithNType));

      MetaClass MC_TestInterface = MetaClassFactory.get(TestInterface.class);

      TypeWithNestedClass typeWithNestedClass = new TypeWithNestedClass();

      if (!MC_TestInterface.isAssignableFrom(typeWithNestedClass.getMyNested().getClass())) {
        System.out.println("BLOW UP!");
      }
    }
    catch (Exception e) {
       e.printStackTrace();
    }

    String packageName = TypeOracleBootstrap.class.getPackage().getName();
    String clazzName = "TypeOracleBootstrapImpl";

    final String cls = "package " + packageName + ";\n" +
            "public class " + clazzName + " implements org.jboss.errai.codegen.test.gwt.client.TypeOracleBootstrap { }";

    PrintWriter printWriter = context.tryCreate(logger, packageName, clazzName);
    printWriter.print(cls);

    context.commit(logger, printWriter);
    return packageName + "." + clazzName;
  }
}
