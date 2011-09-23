package org.jboss.errai.marshalling.rebind;

import org.jboss.errai.bus.server.annotations.ExposeEntity;
import org.jboss.errai.bus.server.annotations.Portable;
import org.jboss.errai.bus.server.service.metadata.MetaDataScanner;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.EmptyStatement;
import org.jboss.errai.codegen.framework.util.Implementations;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;

import javax.enterprise.util.TypeLiteral;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallerGeneratorFactory {
  private static final String MARSHALLERS_VAR = "marshallers";
  private static final String DEFAULT_ENCODING_TYPE = "json";

  public void generate() {
    ClassStructureBuilder<?> classStructureBuilder = Implementations.implement(MarshallerFactory.class);

    Context classContext = classStructureBuilder.getClassDefinition().getContext();

    MetaClass javaUtilMap = MetaClassFactory.get(
            new TypeLiteral<Map<String, Marshaller>>() {
            }
    );

    Implementations.autoInitializedField(classStructureBuilder, javaUtilMap, MARSHALLERS_VAR, HashMap.class);

    ConstructorBlockBuilder<?> constructor = classStructureBuilder.publicConstructor();
    generateMarshallers(constructor, classContext);

    System.out.println(classStructureBuilder.toJavaString());
  }

  private void generateMarshallers(ConstructorBlockBuilder<?> constructor, Context classContext) {
    MetaDataScanner scanner = MetaDataScanner.createInstance();

    Set<Class<?>> exposed = new HashSet<Class<?>>(scanner.getTypesAnnotatedWith(ExposeEntity.class));
    exposed.addAll(scanner.getTypesAnnotatedWith(Portable.class));

    for (Class<?> clazz : exposed) {
      constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
              .invoke("put", clazz.getName(), marshall(clazz)));
    }

    constructor.finish();
  }

  private Statement marshall(Class<?> cls) {
    return MappingStrategyFactory.createStrategy(cls).getMapper().getMarshaller();
  }
}
