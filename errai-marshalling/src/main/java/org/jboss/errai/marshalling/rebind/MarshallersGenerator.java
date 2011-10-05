package org.jboss.errai.marshalling.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

import java.io.PrintWriter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallersGenerator extends Generator {
  /**
   * Simple name of class to be generated
   */
  private String className = null;

  /**
   * Package name of class to be generated
   */
  private String packageName = null;
  private TypeOracle typeOracle;
  private String modulePackage;

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    try {
      typeOracle = context.getTypeOracle();

      JClassType classType = typeOracle.getType(typeName);
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      logger.log(TreeLogger.INFO, "Generating Marshallers Bootstrapper...");

      // Generate class source code
      generateMarshallerBootstrapper(logger, context);
    }
    catch (Throwable e) {
      // record sendNowWith logger that Map generation threw an exception
      e.printStackTrace();
      logger.log(TreeLogger.ERROR, "Error generating marshallers", e);
    }

    // return the fully qualified name of the class generated
    return packageName + "." + className;
  }

  public void generateMarshallerBootstrapper(TreeLogger logger, GeneratorContext context) {
    PrintWriter printWriter = context.tryCreate(logger, packageName, className);
    printWriter.write(new MarshallerGeneratorFactory().generate(packageName, className));

    context.commit(logger, printWriter);
  }
}
