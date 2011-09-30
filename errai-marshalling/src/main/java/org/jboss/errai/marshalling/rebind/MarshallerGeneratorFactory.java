package org.jboss.errai.marshalling.rebind;

import org.jboss.errai.common.client.api.annotations.ExposeEntity;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.rebind.api.MappingContext;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;

import javax.enterprise.util.TypeLiteral;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jboss.errai.codegen.framework.util.Implementations.autoInitializedField;
import static org.jboss.errai.codegen.framework.util.Implementations.implement;
import static org.jboss.errai.codegen.framework.util.Stmt.loadVariable;
import static org.jboss.errai.marshalling.rebind.util.MarshallingUtil.getVarName;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallerGeneratorFactory {
  private static final String MARSHALLERS_VAR = "marshallers";

  private MappingContext mappingContext;

  ClassStructureBuilder<?> classStructureBuilder;

  public String generate(String packageName, String clazzName) {
    classStructureBuilder = implement(MarshallerFactory.class, packageName, clazzName);

    Context classContext = classStructureBuilder.getClassDefinition().getContext();
    mappingContext = new MappingContext(classContext, classStructureBuilder.getClassDefinition(),
            classStructureBuilder);

    loadMarshallers();

    MetaClass javaUtilMap = MetaClassFactory.get(
            new TypeLiteral<Map<String, Marshaller>>() {
            }
    );

    autoInitializedField(classStructureBuilder, javaUtilMap, MARSHALLERS_VAR, HashMap.class);

    ConstructorBlockBuilder<?> constructor = classStructureBuilder.publicConstructor();

    for (Map.Entry<String, Class<? extends Marshaller>> entry : mappingContext.getAllMarshallers().entrySet()) {
      String varName = getVarName(entry.getKey());
      classStructureBuilder.privateField(getVarName(entry.getKey()), entry.getValue()).finish();
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
      Statement marshaller = marshall(clazz);
      MetaClass type = marshaller.getType();
      String varName = getVarName(clazz);

      classStructureBuilder.privateField(varName, type).finish();

      constructor.append(loadVariable(varName).assignValue(marshaller));

      constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
              .invoke("put", clazz.getName(), loadVariable(getVarName(clazz))));
    }

    constructor.finish();
  }

  private Statement marshall(Class<?> cls) {
    MappingStrategy strategy = MappingStrategyFactory.createStrategy(mappingContext, cls);
    if (strategy == null) {
      throw new RuntimeException("no available marshaller for class: " + cls.getName());
    }
    return strategy.getMapper().getMarshaller();
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
