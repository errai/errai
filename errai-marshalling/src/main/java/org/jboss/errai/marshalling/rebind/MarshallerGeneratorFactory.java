package org.jboss.errai.marshalling.rebind;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Modifier;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.framework.util.Bool;
import org.jboss.errai.codegen.framework.util.GenUtil;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.client.api.annotations.ExposeEntity;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;
import org.jboss.errai.marshalling.rebind.api.ArrayMarshallerCallback;
import org.jboss.errai.marshalling.rebind.api.MappingContext;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.util.MarshallingUtil;

import javax.enterprise.util.TypeLiteral;
import java.util.*;

import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.framework.util.Implementations.*;
import static org.jboss.errai.codegen.framework.util.Stmt.loadVariable;
import static org.jboss.errai.marshalling.rebind.util.MarshallingUtil.getArrayVarName;
import static org.jboss.errai.marshalling.rebind.util.MarshallingUtil.getVarName;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallerGeneratorFactory {
  private static final String MARSHALLERS_VAR = "marshallers";
  private static final String ARRAY_MARSHALLERS_VAR = "arrayMarshallers";

  private MappingContext mappingContext;

  ClassStructureBuilder<?> classStructureBuilder;
  ConstructorBlockBuilder<?> constructor;
  Context classContext;

  Set<String> arrayMarshallers = new HashSet<String>();

  public String generate(String packageName, String clazzName) {
    classStructureBuilder = implement(MarshallerFactory.class, packageName, clazzName);

    classContext = ((BuildMetaClass) classStructureBuilder.getClassDefinition()).getContext();
    mappingContext = new MappingContext(classContext, classStructureBuilder.getClassDefinition(),
            classStructureBuilder, new ArrayMarshallerCallback() {
      @Override
      public Statement marshal(MetaClass type, Statement value) {
        createDemarshallerIfNeeded(type);
        return value;
      }

      @Override
      public Statement demarshall(MetaClass type, Statement value) {
        String variable = createDemarshallerIfNeeded(type);

        value = Stmt.loadVariable(getVarName(List.class)).invoke("demarshall", value, Stmt.loadVariable("a1"));

        return Stmt.loadVariable(variable).invoke("demarshall", value, Stmt.loadVariable("a1"));
      }

      private String createDemarshallerIfNeeded(MetaClass type) {
        String varName = getVarName(type);

        if (!arrayMarshallers.contains(varName)) {
          classStructureBuilder.privateField(varName,
                  parameterizedAs(Marshaller.class, typeParametersOf(List.class, type))).finish();
          Statement marshaller = generateArrayMarshaller(type);
          constructor.append(loadVariable(varName).assignValue(marshaller));

          constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
                  .invoke("put", type.getFullyQualifiedName(), loadVariable(varName)));

          arrayMarshallers.add(varName);
        }

        return varName;
      }
    });

    loadMarshallers();

    MetaClass javaUtilMap = MetaClassFactory.get(
            new TypeLiteral<Map<String, Marshaller>>() {
            }
    );

    autoInitializedField(classStructureBuilder, javaUtilMap, MARSHALLERS_VAR, HashMap.class);
    autoInitializedField(classStructureBuilder, javaUtilMap, ARRAY_MARSHALLERS_VAR, HashMap.class);

    constructor = classStructureBuilder.publicConstructor();

    for (Map.Entry<String, Class<? extends Marshaller>> entry : mappingContext.getAllMarshallers().entrySet()) {
      String varName = getVarName(entry.getKey());
      String arrayVarName = getArrayVarName(entry.getKey());
      classStructureBuilder.privateField(varName, entry.getValue()).finish();
      classStructureBuilder.privateField(arrayVarName, entry.getValue()).finish();

      constructor.append(Stmt.create(classContext)
              .loadVariable(varName).assignValue(Stmt.newObject(entry.getValue())));

      constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
              .invoke("put", entry.getKey(), loadVariable(varName)));

      for (String s : mappingContext.getReverseMappingAliasFor(entry.getKey())) {
        constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
                .invoke("put", s, loadVariable(varName)));
      }
    }

    generateMarshallers();

    classStructureBuilder.publicMethod(Marshaller.class, "getMarshaller").parameters(String.class, String.class)
            .body()
            .append(loadVariable(MARSHALLERS_VAR).invoke("get", loadVariable("a1")).returnValue())
            .finish();

    classStructureBuilder.publicMethod(Marshaller.class, "getArrayMarshaller").parameters(String.class, String.class)
            .body()
            .append(loadVariable(MARSHALLERS_VAR).invoke("get", loadVariable("a1")).returnValue())
            .finish();

    String generatedClass = classStructureBuilder.toJavaString();
    System.out.println(generatedClass);
    return generatedClass;
  }

  private void generateMarshallers() {
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();

    Set<Class<?>> exposed = new HashSet<Class<?>>(scanner.getTypesAnnotatedWith(Portable.class));
    exposed.addAll(scanner.getTypesAnnotatedWith(ExposeEntity.class));

    for (Class<?> clazz : exposed) {
      mappingContext.registerGeneratedMarshaller(clazz.getName());
    }

    for (Class<?> clazz : exposed) {
      MetaClass metaClazz = MetaClassFactory.get(clazz);
      Statement marshaller = marshal(metaClazz);
      MetaClass type = marshaller.getType();
      String varName = getVarName(clazz);

      classStructureBuilder.privateField(varName, type).finish();

      constructor.append(loadVariable(varName).assignValue(marshaller));

      constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
              .invoke("put", clazz.getName(), loadVariable(varName)));

      for (String s : mappingContext.getReverseMappingAliasFor(clazz.getName())) {
        constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
                .invoke("put", s, loadVariable(varName)));
      }
    }

    constructor.finish();
  }

  private Statement marshal(MetaClass cls) {
    MappingStrategy strategy = MappingStrategyFactory.createStrategy(mappingContext, cls);
    if (strategy == null) {
      throw new RuntimeException("no available marshaller for class: " + cls.getName());
    }
    return strategy.getMapper().getMarshaller();
  }

  private Statement generateArrayMarshaller(MetaClass arrayType) {
    MetaClass toMap = arrayType;
    while (toMap.isArray()) {
      toMap = toMap.getComponentType();
    }
    int dimensions = GenUtil.getArrayDimensions(arrayType);

    AnonymousClassStructureBuilder classStructureBuilder
            = Stmt.create(mappingContext.getCodegenContext())
            .newObject(parameterizedAs(Marshaller.class, typeParametersOf(List.class, arrayType))).extend();

    MetaClass anonClass = classStructureBuilder.getClassDefinition();

    classStructureBuilder.publicOverridesMethod("getTypeHandled")
            .append(Stmt.load(toMap).returnValue())
            .finish();

    classStructureBuilder.publicOverridesMethod("getEncodingType")
            .append(Stmt.load("json").returnValue())
            .finish();

    BlockBuilder<?> bBuilder = classStructureBuilder.publicOverridesMethod("demarshall",
            Parameter.of(Object.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

    bBuilder.append(Stmt.invokeStatic(anonClass, "_demarshall" + dimensions,
            loadVariable("a0"), loadVariable("a1")).returnValue());
    bBuilder.finish();

    demarshalCode(toMap, dimensions, classStructureBuilder);

    BlockBuilder<?> marshallMethodBlock = classStructureBuilder.publicOverridesMethod("marshall",
            Parameter.of(Object.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

    marshallMethodBlock.append(Stmt.invokeStatic(anonClass, "_marshall" + dimensions,
            loadVariable("a0"), loadVariable("a1")).returnValue());

    marshallMethodBlock.finish();

    return classStructureBuilder.finish();
  }

  private void demarshalCode(MetaClass toMap, int dim, ClassStructureBuilder<? extends ClassStructureBuilder> anonBuilder) {
    Object[] dimParms = new Object[dim];
    dimParms[0] = Stmt.loadVariable("a0").invoke("size");

    final MetaClass arrayType = toMap.asArrayOf(dim);

    final BlockBuilder<?> dmBuilder =
            anonBuilder.privateMethod(arrayType, "_demarshall" + dim)
                    .parameters(List.class, MarshallingSession.class).body();

    dmBuilder.append(Stmt
            .declareVariable(arrayType).named("newArray")
            .initializeWith(Stmt.newArray(toMap, dimParms)));

    dmBuilder.append(autoForLoop("i", Stmt.loadVariable("newArray").loadField("length"))
            .append(dim == 1 ? loadVariable("newArray", loadVariable("i")).assignValue(loadVariable("a0").invoke("get", loadVariable("i")))
                    : loadVariable("newArray", loadVariable("i")).assignValue(
                    Stmt.invokeStatic(anonBuilder.getClassDefinition(),
                            "_demarshall" + (dim - 1), loadVariable("a0"), loadVariable("a1"))))

            .finish())
            .append(Stmt.loadVariable("newArray").returnValue());


    dmBuilder.finish();

    final BlockBuilder<?> mBuilder = anonBuilder.privateMethod(String.class, "_marshall" + dim)
            .parameters(arrayType, MarshallingSession.class).body();

    mBuilder.append(Stmt.declareVariable(StringBuilder.class).named("sb")
            .initializeWith(Stmt.newObject(StringBuilder.class).withParameters("[")))
            .append(autoForLoop("i", Stmt.loadVariable("a0").loadField("length"))
                    .append(Stmt.if_(Bool.greaterThan(Stmt.loadVariable("i"), 0))
                            .append(Stmt.loadVariable("sb").invoke("append", ",")).finish())
                    .append(Stmt.loadVariable("sb").invoke("append", dim == 1 ?
                            Stmt.loadVariable(MarshallingUtil.getVarName(toMap))
                                    .invoke("marshall", Stmt.loadVariable("a0", Stmt.loadVariable("i")), Stmt.loadVariable("a1"))
                            :
                            Stmt.invokeStatic(anonBuilder.getClassDefinition(),
                                    "_marshall" + (dim - 1), Stmt.loadVariable("a0", Stmt.loadVariable("i")), loadVariable("a1"))))
                    .finish())
            .append(Stmt.loadVariable("sb").invoke("append", "]").returnValue())
            .finish();


    if (dim > 1) {
      demarshalCode(toMap, dim - 1, anonBuilder);
    }
  }

  private void loadMarshallers() {
    Set<Class<?>> marshallers =
            ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(ClientMarshaller.class);

    for (Class<?> cls : marshallers) {
      if (Marshaller.class.isAssignableFrom(cls)) {
        try {
          Class<?> type = (Class<?>) Marshaller.class.getMethod("getTypeHandled").invoke(cls.newInstance());
          mappingContext.registerMarshaller(type.getName(), cls.asSubclass(Marshaller.class));

          if (cls.isAnnotationPresent(ImplementationAliases.class)) {
            for (Class<?> c : cls.getAnnotation(ImplementationAliases.class).value()) {
              mappingContext.registerMappingAlias(c, type);
            }
          }
        }
        catch (Throwable t) {
          throw new RuntimeException("could not instantiate marshaller class: " + cls.getName(), t);
        }
      }
      else {
        throw new RuntimeException("class annotated with " + ClientMarshaller.class.getCanonicalName()
                + " does not implement " + Marshaller.class.getName());
      }
    }
  }
}
