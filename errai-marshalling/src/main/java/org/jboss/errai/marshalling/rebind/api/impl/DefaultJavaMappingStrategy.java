package org.jboss.errai.marshalling.rebind.api.impl;

import com.google.gwt.json.client.JSONObject;
import org.jboss.errai.codegen.framework.Cast;

import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.impl.AnonymousClassStructureBuilderImpl;
import org.jboss.errai.codegen.framework.util.Implementations;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.marshalling.client.api.MappedOrdered;
import org.jboss.errai.marshalling.client.api.MapsTo;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;
import org.jboss.errai.marshalling.client.api.exceptions.MarshallingException;
import org.jboss.errai.marshalling.client.api.exceptions.NoAvailableMarshallerException;
import org.jboss.errai.marshalling.rebind.api.MappingContext;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.api.ObjectMapper;
import org.jboss.errai.marshalling.rebind.util.MarshallingUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.typeParametersOf;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DefaultJavaMappingStrategy implements MappingStrategy {
  private MappingContext context;
  private Class<?> toMap;

  public DefaultJavaMappingStrategy(MappingContext context, Class<?> toMap) {
    this.context = context;
    this.toMap = toMap;
  }

  @Override
  public ObjectMapper getMapper() {
    if (isJavaBean(toMap)) {
      return generateJavaBeanMapper();
    }
    else {
      return generateImmutableMapper();
    }
  }

  private ObjectMapper generateImmutableMapper() {
    final ConstructorMapping mapping = findUsableConstructorMapping();

    final List<Statement> marshallers = new ArrayList<Statement>();
    for (FieldMapping m : mapping.getMappings()) {
      if (context.hasMarshaller(m.getType())) {
        marshallers.add(fieldDemarshall(m, JSONObject.class));
      }
      else {
        //
      }
    }

    return new ObjectMapper() {
      @Override
      public Statement getMarshaller() {
        AnonymousClassStructureBuilderImpl classStructureBuilder
                = Stmt.create(context.getCodegenContext())
                .newObject(parameterizedAs(Marshaller.class, typeParametersOf(JSONObject.class, toMap))).extend();

        classStructureBuilder.publicOverridesMethod("getTypeHandled")
                .append(Stmt.load(toMap).returnValue())
                .finish();

        classStructureBuilder.publicOverridesMethod("getEncodingType")
                .append(Stmt.load("json").returnValue())
                .finish();

        classStructureBuilder.publicOverridesMethod("demarshall",
                Parameter.of(Object.class, "a0"), Parameter.of(MarshallingContext.class, "a1"))
                .append(Stmt.nestedCall(Stmt.newObject(toMap)
                        .withParameters(marshallers.toArray(new Object[marshallers.size()]))).returnValue())
                .finish();

        classStructureBuilder.publicOverridesMethod("marshall",
                Parameter.of(Object.class, "a0"), Parameter.of(MarshallingContext.class, "a1"))
                .append(Stmt.loadVariable("a0").returnValue())
                .finish();

        return classStructureBuilder.finish();
      }
    };

  }

  private ObjectMapper generateJavaBeanMapper() {
    return null;
  }

  private ConstructorMapping findUsableConstructorMapping() {
    Set<Constructor<?>> constructors = new HashSet<Constructor<?>>();
    Set<FieldMapping> mappings = new HashSet<FieldMapping>();

    for (Constructor c : toMap.getConstructors()) {
      if (c.isAnnotationPresent(MappedOrdered.class)) {
        constructors.add(c);
      }
      else if (c.getParameterTypes().length != 0) {
        boolean satisifed = true;
        FieldScan:
        for (int i = 0; i < c.getParameterTypes().length; i++) {
          Annotation[] annotations = c.getParameterAnnotations()[i];
          if (annotations.length == 0) {
            satisifed = false;
          }
          else {
            for (Annotation a : annotations) {
              if (!MapsTo.class.isAssignableFrom(a.annotationType())) {
                satisifed = false;
                break FieldScan;
              }
              else {
                mappings.add(new FieldMapping(i, ((MapsTo) a).value(), c.getParameterTypes()[i]));
              }
            }
          }
        }

        if (satisifed) {
          constructors.add(c);
        }
      }
    }

    return new ConstructorMapping(constructors.iterator().next(), ConstructionType.Mapped, mappings);
  }

  private static enum ConstructionType {
    Mapped, Custom
  }

  private static class ConstructorMapping {
    Constructor<?> constructor;
    ConstructionType type;
    Set<FieldMapping> mappings;

    private ConstructorMapping(Constructor<?> constructor, ConstructionType type, Set<FieldMapping> mappings) {
      this.constructor = constructor;
      this.type = type;
      this.mappings = mappings;
    }

    public Constructor<?> getConstructor() {
      return constructor;
    }

    public ConstructionType getType() {
      return type;
    }

    public Set<FieldMapping> getMappings() {
      return mappings;
    }
  }

  private static class FieldMapping {
    int index;
    String fieldName;
    Class<?> type;

    private FieldMapping(int index, String fieldName, Class<?> type) {
      this.index = index;
      this.fieldName = fieldName;
      this.type = type;
    }

    public int getIndex() {
      return index;
    }

    public String getFieldName() {
      return fieldName;
    }

    public Class<?> getType() {
      return type;
    }
  }

  private boolean isJavaBean(Class<?> toMap) {
    try {
      toMap.getConstructor();
      return true;
    }
    catch (NoSuchMethodException e) {
      return false;
    }
  }

  public Statement fieldDemarshall(FieldMapping mapping, Class<?> fromType) {
    return fieldDemarshall(mapping.getFieldName(), fromType, mapping.getType());
  }


  public Statement fieldDemarshall(String fieldName, Class<?> fromType, Class<?> toType) {
    return unwrapJSON(Stmt.nestedCall(Cast.to(fromType, Stmt.loadVariable("a0"))).invoke("get", fieldName), toType);
  }

  public void marshallToJSON(BlockBuilder<?> builder, Class<?> toType) {
    Implementations.StringBuilderBuilder sb = Implementations.newStringBuilder();
    sb.append("{");

    for (Field field: toType.getDeclaredFields()) {
      if (Modifier.isTransient(field.getModifiers())) {
        continue;
      }

      if (!context.hasMarshaller(toType)) {
         throw new NoAvailableMarshallerException(toType);
      }
      
      sb.append("\"" + field.getName() + "\" : ");
      sb.append(null);
    }

  }


  
  public Statement unwrapJSON(Statement valueStatement, Class<?> toType) {
    if (String.class.isAssignableFrom(toType)) {
      return Stmt.create(context.getCodegenContext())
              .loadVariable(MarshallingUtil.getVarName(String.class))
              .invoke("demarshall", valueStatement, Stmt.loadVariable("a1"));
    }
    else {
      return null;
    }
  }
}
