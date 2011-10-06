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
import org.jboss.errai.codegen.framework.util.GenUtil;
import org.jboss.errai.codegen.framework.util.Implementations;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.client.api.annotations.ExposeEntity;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.rebind.api.MappingContext;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;

import javax.enterprise.util.TypeLiteral;
import java.util.*;

import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.framework.util.Implementations.autoInitializedField;
import static org.jboss.errai.codegen.framework.util.Implementations.implement;
import static org.jboss.errai.codegen.framework.util.Stmt.loadVariable;
import static org.jboss.errai.marshalling.rebind.util.MarshallingUtil.getVarName;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallerGeneratorFactory {
  private static final String MARSHALLERS_VAR = "marshallers";
  private static final String ARRAY_MARSHALLERS_VAR = "arrayMarshallers";

  private MappingContext mappingContext;

  ClassStructureBuilder<?> classStructureBuilder;

  public String generate(String packageName, String clazzName) {
    classStructureBuilder = implement(MarshallerFactory.class, packageName, clazzName);

    Context classContext = ((BuildMetaClass) classStructureBuilder.getClassDefinition()).getContext();
    mappingContext = new MappingContext(classContext, classStructureBuilder.getClassDefinition(),
            classStructureBuilder);

    loadMarshallers();

    MetaClass javaUtilMap = MetaClassFactory.get(
            new TypeLiteral<Map<String, Marshaller>>() {
            }
    );

    autoInitializedField(classStructureBuilder, javaUtilMap, MARSHALLERS_VAR, HashMap.class);
    autoInitializedField(classStructureBuilder, javaUtilMap, ARRAY_MARSHALLERS_VAR, HashMap.class);

    ConstructorBlockBuilder<?> constructor = classStructureBuilder.publicConstructor();

    for (Map.Entry<String, Class<? extends Marshaller>> entry : mappingContext.getAllMarshallers().entrySet()) {
      String varName = getVarName(entry.getKey());
      String arrayVarName = "arrayOf_" + varName;
      classStructureBuilder.privateField(varName, entry.getValue()).finish();
      classStructureBuilder.privateField(arrayVarName, entry.getValue()).finish();

      constructor.append(Stmt.create(classContext)
              .loadVariable(varName).assignValue(Stmt.newObject(entry.getValue())));

      constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
              .invoke("put", entry.getKey(), loadVariable(varName)));
    }

    generateMarshallers(constructor, classContext);

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

  private void generateMarshallers(ConstructorBlockBuilder<?> constructor, Context classContext) {
    MetaDataScanner scanner = MetaDataScanner.createInstance();

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
      String arrayVarName = "arrayOf_" + varName;

      classStructureBuilder.privateField(varName, type).finish();
      classStructureBuilder.privateField(arrayVarName, type).finish();

      constructor.append(loadVariable(varName).assignValue(marshaller));

      constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
              .invoke("put", clazz.getName(), loadVariable(varName)));

      Statement arrayMarshaller = arrayMarshal(metaClazz.asArrayOf(2));

      constructor.append(Stmt.create(classContext).loadVariable(ARRAY_MARSHALLERS_VAR)
              .invoke("put", clazz.getName(), loadVariable(arrayVarName)));

      constructor.append(loadVariable(arrayVarName).assignValue(arrayMarshaller));
    }

    constructor.finish();
  }

  private Statement marshal(MetaClass cls) {
    boolean array = cls.isArray();

    MappingStrategy strategy = MappingStrategyFactory.createStrategy(mappingContext, cls);
    if (strategy == null) {
      throw new RuntimeException("no available marshaller for class: " + cls.getName());
    }
    return strategy.getMapper().getMarshaller();
  }

  private Statement arrayMarshal(MetaClass arrayType) {
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

    bBuilder.append(Stmt.invokeStatic(anonClass, "_demarshall" + dimensions, loadVariable("a0")).returnValue());
    bBuilder.finish();

    demarshalCode(toMap, dimensions, classStructureBuilder);

    BlockBuilder<?> marshallMethodBlock = classStructureBuilder.publicOverridesMethod("marshall",
            Parameter.of(Object.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

    marshallMethodBlock.append(Stmt.throw_(RuntimeException.class, "not implemented"));

    marshallMethodBlock.finish();

    return classStructureBuilder.finish();
  }

  private void demarshalCode(MetaClass toMap, int dim, ClassStructureBuilder<? extends ClassStructureBuilder> anonBuilder) {
    Object[] dimParms = new Object[dim];
    dimParms[0] = Stmt.loadVariable("a0").invoke("size");

    MetaClass arrayType = toMap.asArrayOf(dim);

    BlockBuilder<?> bBuilder =
            anonBuilder.privateMethod(arrayType, "_demarshall" + dim)
                    .modifiers(Modifier.Static)
                    .parameters(List.class).body();

    bBuilder.append(Stmt
            .declareVariable(arrayType).named("newArray")
            .initializeWith(Stmt.newArray(toMap, dimParms)));

    bBuilder.append(Implementations.autoForLoop("i", Stmt.loadVariable("newArray").loadField("length"))
            .append(dim == 1 ? loadVariable("newArray").assignValue(loadVariable("a0").invoke("get", loadVariable("i")))
                    : loadVariable("newArray").assignValue(
                    Stmt.invokeStatic(anonBuilder.getClassDefinition(), "_demarshall" + (dim - 1), loadVariable("a0"))))
            .finish());

    if (dim > 1) {
      demarshalCode(toMap, dim - 1, anonBuilder);
    }

    bBuilder.finish();
  }

  private void loadMarshallers() {
    Set<Class<?>> marshallers =
            ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(ClientMarshaller.class);

    for (Class<?> cls : marshallers) {
      if (Marshaller.class.isAssignableFrom(cls)) {
        try {
          Class<?> type = (Class<?>) Marshaller.class.getMethod("getTypeHandled").invoke(cls.newInstance());
          mappingContext.registerMarshaller(type.getName(), cls.asSubclass(Marshaller.class));
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
