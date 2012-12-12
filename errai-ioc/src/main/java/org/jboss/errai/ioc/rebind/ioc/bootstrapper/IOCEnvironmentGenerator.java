package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Stmt.invokeStatic;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.ioc.client.AnnotationComparator;
import org.jboss.errai.ioc.client.QualifierEqualityFactory;
import org.jboss.errai.ioc.client.QualifierUtil;
import org.jboss.errai.ioc.client.container.IOCEnvironment;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.io.File;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class IOCEnvironmentGenerator extends Generator {


  @Override
  public String generate(final TreeLogger logger,
                         final GeneratorContext context,
                         final String typeName) throws UnableToCompleteException {
    try {
      final JClassType classType = context.getTypeOracle().getType(typeName);
      final String packageName = classType.getPackage().getName();
      final String className = classType.getSimpleSourceName() + "Impl";

      logger.log(TreeLogger.INFO, "Generating Extensions Bootstrapper...");

      // Generate class source code
      generateIOCEnvironment(packageName, className, logger, context);

      // return the fully qualified name of the class generated
      return packageName + "." + className;
    }
    catch (Throwable e) {
      // record sendNowWith logger that Map generation threw an exception
      e.printStackTrace();
      logger.log(TreeLogger.ERROR, "Error generating extensions", e);
      throw new RuntimeException("error generating", e);
    }
  }

  private void generateIOCEnvironment(final String packageName,
                                      final String className,
                                      final TreeLogger logger,
                                      final GeneratorContext generatorContext) {

    final PrintWriter printWriter = generatorContext.tryCreate(logger, packageName, className);
    if (printWriter == null) {
      return;
    }

    final boolean asyncBootstrap;

    final String s = EnvUtil.getEnvironmentConfig().getFrameworkOrSystemProperty("errai.ioc.async_bean_manager");
    asyncBootstrap = s != null && Boolean.parseBoolean(s);

    final ClassStructureBuilder<? extends ClassStructureBuilder<?>> builder
        = ClassBuilder.define(packageName + "." + className).publicScope()
        .implementsInterface(IOCEnvironment.class)
        .body()
        .publicMethod(boolean.class, "isAsync")
        .append(Stmt.load(asyncBootstrap).returnValue())
        .finish();

    final String csq = builder.toJavaString();
    printWriter.append(csq);
    generatorContext.commit(logger, printWriter);
  }
}
