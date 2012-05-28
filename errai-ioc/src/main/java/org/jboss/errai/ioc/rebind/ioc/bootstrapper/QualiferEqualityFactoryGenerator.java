package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

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
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.MethodCommentBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.ioc.client.AnnotationComparator;
import org.jboss.errai.ioc.client.QualifierEqualityFactory;
import org.jboss.errai.ioc.client.QualifierUtil;

import javax.inject.Qualifier;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Stmt.if_;
import static org.jboss.errai.codegen.util.Stmt.invokeStatic;

/**
 * @author Mike Brock
 */
public class QualiferEqualityFactoryGenerator extends Generator {
  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    try {
      // get classType and save instance variables

      final JClassType classType = context.getTypeOracle().getType(typeName);
      String packageName = classType.getPackage().getName();
      String className = classType.getSimpleSourceName() + "Impl";

      logger.log(TreeLogger.INFO, "Generating Extensions Bootstrapper...");

//      GWTUtil.populateMetaClassFactoryFromTypeOracle(context, logger);

      // Generate class source code
      generateQualifierEqualityFactory(packageName, className, logger, context);

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

  private static final String COMPARATOR_MAP_VAR = "comparatorMap";

  private void generateQualifierEqualityFactory(final String packageName,
                                                final String className,
                                                final TreeLogger logger,
                                                final GeneratorContext generatorContext) {

    final TypeOracle oracle = generatorContext.getTypeOracle();

    final ClassStructureBuilder<? extends ClassStructureBuilder<?>> builder = ClassBuilder.define(packageName + "." + className)
            .publicScope().implementsInterface(QualifierEqualityFactory.class)
            .body();

    final MetaClass mapStringAnnoComp
            = parameterizedAs(HashMap.class, typeParametersOf(String.class, AnnotationComparator.class));

    builder.privateField(COMPARATOR_MAP_VAR, mapStringAnnoComp)
            .initializesWith(Stmt.newObject(mapStringAnnoComp)).finish();

    final ConstructorBlockBuilder<? extends ClassStructureBuilder<?>> constrBuilder = builder.publicConstructor();

    final MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    final Set<Class<?>> typesAnnotatedWith = scanner.getTypesAnnotatedWith(Qualifier.class);


    for (Class<?> aClass : typesAnnotatedWith) {
      try {
        oracle.getType(aClass.getName());

        constrBuilder._(Stmt.loadVariable(COMPARATOR_MAP_VAR)
                .invoke("put", aClass.getName(), generateComparatorFor(aClass)));
      }
      catch (NotFoundException e) {
        System.out.println(aClass.getName() + " is not client translatable.");
        // ignore.
      }
    }

    // finish constructor
    constrBuilder.finish();

    builder.publicMethod(boolean.class, "isEqual",
            Parameter.of(Annotation.class, "a1"), Parameter.of(Annotation.class, "a2"))
            .body()
            ._(if_(Bool.and(Bool.expr(invokeStatic(QualifierUtil.class, "isSameType", Refs.get("a1"), Refs.get("a2"))),
                    Bool.expr(Stmt.loadVariable(COMPARATOR_MAP_VAR).invoke("containsKey",
                            Stmt.loadVariable("a1").invoke("annotationType").invoke("getName")))))
                    ._(Stmt.loadVariable(COMPARATOR_MAP_VAR).invoke("get", Stmt.loadVariable("a1").invoke("annotationType").invoke("getName"))
                            .invoke("isEqual", Refs.get("a1"), Refs.get("a2")).returnValue())
                    .finish()
                    .else_()
                    ._(Stmt.load(false).returnValue())
                    .finish()).finish();

    final PrintWriter printWriter = generatorContext.tryCreate(logger, packageName, className);
    String out = builder.toJavaString();

    System.out.println(out);

    printWriter.append(out);
    generatorContext.commit(logger, printWriter);
  }

  private Statement generateComparatorFor(final Class<?> annotationClass) {
    final MetaClass MC_annotationClass = MetaClassFactory.get(annotationClass);

    final MetaClass MC_annoComparator = parameterizedAs(AnnotationComparator.class, typeParametersOf(annotationClass));

    final MethodBlockBuilder<AnonymousClassStructureBuilder> builder = ObjectBuilder.newInstanceOf(MC_annoComparator).extend()
            .publicMethod(boolean.class, "isEqual",
                    Parameter.of(MC_annotationClass, "a1"), Parameter.of(MC_annotationClass, "a2"))
            .annotatedWith(new Override() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return Override.class;
              }
            });

    for (MetaMethod method : MC_annotationClass.getDeclaredMethods()) {
      if (method.isPrivate() || method.isProtected() || method.getName().equals("equals") ||
              method.getName().equals("hashCode")) continue;

      builder._(
              Stmt.if_(Bool.notExpr(Stmt.loadVariable("a1").invoke(method).invoke("equals", Stmt.loadVariable("a2").invoke(method))))
                      ._(Stmt.load(false).returnValue())
                      .finish()
      );
    }

    builder._(Stmt.load(true).returnValue());

    // finish method;
    final AnonymousClassStructureBuilder classStructureBuilder = builder.finish();

    return classStructureBuilder.finish();
  }
}
