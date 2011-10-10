package org.jboss.errai.marshalling.rebind.api.impl;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.codegen.framework.Cast;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.CatchBlockBuilder;
import org.jboss.errai.codegen.framework.meta.*;
import org.jboss.errai.codegen.framework.util.Bool;
import org.jboss.errai.codegen.framework.util.GenUtil;
import org.jboss.errai.codegen.framework.util.Implementations;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.MappedOrdered;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;
import org.jboss.errai.marshalling.client.api.exceptions.InvalidMappingException;
import org.jboss.errai.marshalling.client.api.exceptions.MarshallingException;
import org.jboss.errai.marshalling.client.api.exceptions.NoAvailableMarshallerException;
import org.jboss.errai.marshalling.client.util.MarshallUtil;
import org.jboss.errai.marshalling.rebind.api.MappingContext;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.api.ObjectMapper;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.typeParametersOf;

/**
 * The Errai default Java-to-JSON-to-Java marshaling strategy.
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DefaultJavaMappingStrategy implements MappingStrategy {
  private MappingContext context;
  private MetaClass toMap;
  private static Map<String, ConstructorMapping> builtInConstructorMappings
          = new HashMap<String, ConstructorMapping>();

  static {
    loadDefaultMappings();
  }

  public DefaultJavaMappingStrategy(MappingContext context, MetaClass toMap) {
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
    for (ConstructorFieldMapping m : mapping.getMappings()) {
      if (context.hasProvidedOrGeneratedMarshaller(m.getType())) {
        if (m.getType().isArray()) {
          marshallers.add(context.getArrayMarshallerCallback()
                  .demarshall(m.getType(), extractJSONObjectProperty(m.getFieldName(), JSONObject.class)));
        }
        else {
          marshallers.add(fieldDemarshall(m, JSONObject.class));
        }
      }
      else {
        throw new MarshallingException("no marshaller for type: " + m.getType());
      }
    }

    return new ObjectMapper() {
      @Override
      public Statement getMarshaller() {
        AnonymousClassStructureBuilder classStructureBuilder
                = Stmt.create(context.getCodegenContext())
                .newObject(parameterizedAs(Marshaller.class, typeParametersOf(JSONValue.class, toMap))).extend();

        classStructureBuilder.publicOverridesMethod("getTypeHandled")
                .append(Stmt.load(toMap).returnValue())
                .finish();

        classStructureBuilder.publicOverridesMethod("getEncodingType")
                .append(Stmt.load("json").returnValue())
                .finish();

        classStructureBuilder.publicOverridesMethod("demarshall",
                Parameter.of(Object.class, "a0"), Parameter.of(MarshallingSession.class, "a1"))
                .append(Stmt.nestedCall(Stmt.newObject(toMap)
                        .withParameters(marshallers.toArray(new Object[marshallers.size()]))).returnValue())
                .finish();

        BlockBuilder<?> marshallMethodBlock = classStructureBuilder.publicOverridesMethod("marshall",
                Parameter.of(Object.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

        if (mapping.isDemarshallOnly()) {
          marshallMethodBlock.append(Stmt.load(null).returnValue());
        }
        else {
          marshallToJSON(marshallMethodBlock, toMap);
        }

        marshallMethodBlock.finish();

        classStructureBuilder.publicOverridesMethod("handles", Parameter.of(Object.class, "a0"))
                .append(Stmt.nestedCall(Bool.and(
                        Bool.notEquals(Stmt.loadVariable("a0").invoke("isObject"), null),
                        Stmt.loadVariable("a0").invoke("isObject").invoke("get", SerializationParts.ENCODED_TYPE)
                                .invoke("equals", Stmt.loadVariable("this").invoke("getTypeHandled").invoke("getName"))
                )).returnValue()).finish();

        return classStructureBuilder.finish();
      }
    };
  }

  private ObjectMapper generateJavaBeanMapper() {
    final BeanMapping mapping = finaUsuableBeanMapping();

    return new ObjectMapper() {
      @Override
      public Statement getMarshaller() {
        AnonymousClassStructureBuilder classStructureBuilder
                = Stmt.create(context.getCodegenContext())
                .newObject(parameterizedAs(Marshaller.class, typeParametersOf(JSONValue.class, toMap))).extend();

        classStructureBuilder.publicOverridesMethod("getTypeHandled")
                .append(Stmt.load(toMap).returnValue())
                .finish();

        classStructureBuilder.publicOverridesMethod("getEncodingType")
                .append(Stmt.load("json").returnValue())
                .finish();

        BlockBuilder<?> builder =
                classStructureBuilder.publicOverridesMethod("demarshall",
                        Parameter.of(Object.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

        BlockBuilder<CatchBlockBuilder> tryBuilder = Stmt.try_();

        tryBuilder.append(Stmt.declareVariable(JSONObject.class).named("obj").finish());
        
        /**
         * Check to see if value is null. If so, return null.
         */
        tryBuilder.append(
                Stmt.if_(Bool.or(Bool.isNull(Stmt.loadVariable("a0")),
                        Bool.isNotNull(Stmt.loadVariable("a0").invoke("isNull"))))
                        .append(Stmt.load(null).returnValue())
                        .finish()
                .else_()
                .append(Stmt.loadVariable("obj").assignValue(Stmt.loadVariable("a0").invoke("isObject")))
                .finish()
        );

        tryBuilder.append(Stmt.declareVariable(toMap).named("entity").initializeWith(Stmt.nestedCall(Stmt.newObject(toMap))));

        /**
         * Start binding of fields here.
         */
        for (MetaField field : mapping.getMappings()) {
          Statement val;
          if (field.getType().isArray()) {
            val = context.getArrayMarshallerCallback()
                    .demarshall(field.getType(), extractJSONObjectProperty(field.getName(), JSONValue.class));
          }
          else {
            val = fieldDemarshall(field.getName(), MetaClassFactory.get(JSONValue.class), field.getType().asBoxed());
          }

          // handle long case -- GWT does not support long in JSNI
          if (field.isPublic()) {
            tryBuilder.append(Stmt.loadVariable("entity").loadField(field.getName()).assignValue(val));
          }
          else {
            MetaMethod setterMeth = GenUtil.findCaseInsensitiveMatch(null,
                    field.getDeclaringClass(), "set" + field.getName(),
                    field.getType());

            if (setterMeth != null) {
              tryBuilder.append(Stmt.loadVariable("entity").invoke(setterMeth, val));
            }
            else if (field.getType().getCanonicalName().equals("long")) {
              throw new RuntimeException("cannot support private field marshalling of long type" +
                      " (not supported by JSNI) for field: "
                      + field.getDeclaringClass().getFullyQualifiedName() + "#" + field.getName());
            }
            else {
              if (!context.isExposed(field)) {
                GenUtil.addPrivateAccessStubs(true, context.getClassStructureBuilder(), field);
                context.markExposed(field);
              }

              tryBuilder.append(Stmt.invokeStatic(context.getGeneratedBootstrapClass(), GenUtil.getPrivateFieldInjectorName(field),
                      Stmt.loadVariable("entity"), val));
            }
          }
        }

        tryBuilder.append(Stmt.loadVariable("entity").returnValue());

        tryBuilder.finish()
                .catch_(Throwable.class, "t")
                .append(Stmt.loadVariable("t").invoke("printStackTrace"))
                .append(Stmt.throw_(RuntimeException.class, "error demarshalling entity: " + toMap.getFullyQualifiedName(),
                        Stmt.loadVariable("t")))
                .finish();

        builder.append(tryBuilder.finish()).finish();

        BlockBuilder<?> marshallMethodBlock = classStructureBuilder.publicOverridesMethod("marshall",
                Parameter.of(Object.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

        marshallToJSON(marshallMethodBlock, toMap);

        marshallMethodBlock.finish();

        classStructureBuilder.publicOverridesMethod("handles", Parameter.of(Object.class, "a0"))
                .append(Stmt.nestedCall(Bool.and(
                        Bool.notEquals(Stmt.loadVariable("a0").invoke("isObject"), null),
                        Stmt.loadVariable("a0").invoke("isObject").invoke("get", SerializationParts.ENCODED_TYPE)
                                .invoke("equals", Stmt.loadVariable("this").invoke("getTypeHandled").invoke("getName"))
                )).returnValue()).finish();

        return classStructureBuilder.finish();

      }
    };
  }

  private BeanMapping finaUsuableBeanMapping() {
    MetaConstructor constructor = toMap.getConstructor(new MetaClass[0]);
    if (constructor == null) {
      throw new InvalidMappingException("cannot find a default, no-argument constructor or field-mapped constructor in: "
              + toMap.getFullyQualifiedName());
    }

    List<MetaField> mappings = new ArrayList<MetaField>();

    MetaClass c = toMap;

    do {
      for (MetaField field : c.getDeclaredFields()) {
        if (field.isTransient() || field.isStatic()) {
          continue;
        }

        MetaClass type = field.getType();

        if (!type.isEnum() && !context.hasProvidedOrGeneratedMarshaller(type)) {
          throw new InvalidMappingException("portable entity " + toMap.getFullyQualifiedName()
                  + " contains a field (" + field.getName() + ") that is not known to the marshaller: "
                  + type.getFullyQualifiedName());
        }

        mappings.add(field);
      }
    }
    while ((c = c.getSuperClass()) != null);


    return new BeanMapping(mappings);
  }

  private ConstructorMapping findUsableConstructorMapping() {
    if (builtInConstructorMappings.containsKey(toMap.getCanonicalName())) {
      return builtInConstructorMappings.get(toMap.getCanonicalName());
    }

    Set<MetaConstructor> constructors = new HashSet<MetaConstructor>();
    List<ConstructorFieldMapping> mappings = new ArrayList<ConstructorFieldMapping>();

    for (MetaConstructor c : toMap.getConstructors()) {
      if (c.isAnnotationPresent(MappedOrdered.class)) {
        constructors.add(c);
      }
      else if (c.getParameters().length != 0) {
        boolean satisifed = true;
        FieldScan:
        for (int i = 0; i < c.getParameters().length; i++) {
          Annotation[] annotations = c.getParameters()[i].getAnnotations();
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
                MapsTo mapsTo = (MapsTo) a;
                String fieldName = mapsTo.value();

                if (toMap.getDeclaredField(fieldName) == null) {
                  throw new InvalidMappingException(MapsTo.class.getCanonicalName()
                          + " refers to a field ('" + fieldName + "') which does not exist in the class: "
                          + toMap.getName());
                }

                mappings.add(new ConstructorFieldMapping(fieldName, c.getParameters()[i].getType()));
              }
            }
          }
        }

        if (satisifed) {
          constructors.add(c);
        }
      }
    }

    if (constructors.isEmpty()) {
      throw new InvalidMappingException("unable to find a usable constructor for: " + toMap.getFullyQualifiedName());
    }

    return new ConstructorMapping(ConstructionType.Mapped, mappings);
  }

  private static enum ConstructionType {
    Mapped, Custom
  }

  private static class BeanMapping {
    List<MetaField> mappings;

    private BeanMapping(List<MetaField> mappings) {
      this.mappings = mappings;
    }

    public List<MetaField> getMappings() {
      return mappings;
    }
  }


  private static class ConstructorMapping {
    ConstructionType type;
    List<ConstructorFieldMapping> mappings;
    private boolean demarshallOnly;

    private ConstructorMapping(ConstructionType type, List<ConstructorFieldMapping> mappings) {
      this.type = type;
      this.mappings = mappings;
    }

    public ConstructionType getType() {
      return type;
    }

    public List<ConstructorFieldMapping> getMappings() {
      return mappings;
    }

    public boolean isDemarshallOnly() {
      return demarshallOnly;
    }

    public void setDemarshallOnly(boolean demarshallOnly) {
      this.demarshallOnly = demarshallOnly;
    }
  }

  private static class ConstructorFieldMapping {
    String fieldName;
    MetaClass type;

    private ConstructorFieldMapping(String fieldName, MetaClass type) {
      this.fieldName = fieldName;
      this.type = type;
    }

    public String getFieldName() {
      return fieldName;
    }

    public MetaClass getType() {
      return type;
    }
  }

  private boolean isJavaBean(MetaClass toMap) {
    return toMap.getConstructor(new MetaClass[0]) != null;
  }

  public Statement fieldDemarshall(ConstructorFieldMapping mapping, Class<?> fromType) {
    return fieldDemarshall(mapping, MetaClassFactory.get(fromType));
  }

  public Statement fieldDemarshall(ConstructorFieldMapping mapping, MetaClass fromType) {
    return fieldDemarshall(mapping.getFieldName(), fromType, mapping.getType());
  }

  public Statement fieldDemarshall(String fieldName, MetaClass fromType, Class<?> toType) {
    return unwrapJSON(extractJSONObjectProperty(fieldName, fromType), MetaClassFactory.get(toType));
  }

  public Statement fieldDemarshall(String fieldName, MetaClass fromType, MetaClass toType) {
    return unwrapJSON(extractJSONObjectProperty(fieldName, fromType), toType);
  }

  public Statement extractJSONObjectProperty(String fieldName, Class fromType) {
    return extractJSONObjectProperty(fieldName, MetaClassFactory.get(fromType));
  }

  public Statement extractJSONObjectProperty(String fieldName, MetaClass fromType) {
    if (fromType.getFullyQualifiedName().equals(JSONValue.class.getName())) {
      return Stmt.loadVariable("obj").invoke("get", fieldName);
     // return Stmt.invokeStatic(MarshallUtil.class, "nullSafe_JSONObject", Stmt.loadVariable("a0"), fieldName);
    }
    else {
      return Stmt.nestedCall(Cast.to(fromType, Stmt.loadVariable("a0"))).invoke("get", fieldName);
    }
  }


  public void marshallToJSON(BlockBuilder<?> builder, MetaClass toType) {
    if (!context.hasProvidedOrGeneratedMarshaller(toType)) {
      throw new NoAvailableMarshallerException(toType.getName());
    }

    Implementations.StringBuilderBuilder sb = Implementations.newStringBuilder();
    sb.append("{");
    sb.append(keyValue(SerializationParts.ENCODED_TYPE, string(toType.getFullyQualifiedName())));
    sb.append(",");
    sb.append(string(SerializationParts.OBJECT_ID)).append(":").append(Stmt.loadVariable("a0").invoke("hashCode"));

    boolean hasEncoded = false;

    int i = 0;

    MetaClass c = toType;
    do {
      for (MetaField metaField : c.getDeclaredFields()) {
        if (metaField.isTransient() || metaField.isStatic()) {
          continue;
        }

        if (!hasEncoded) {
          sb.append(",");
          hasEncoded = true;
        }
        else if (i > 0) {
          sb.append(",");
        }


        MetaClass targetType = GenUtil.getPrimitiveWrapper(metaField.getType());

        if (!targetType.isEnum() && !context.hasProvidedOrGeneratedMarshaller(targetType)) {
          throw new NoAvailableMarshallerException(targetType.getFullyQualifiedName());
        }

        Statement valueStatement = valueAccessorFor(metaField);
        if (targetType.isArray()) {
          valueStatement = context.getArrayMarshallerCallback().marshal(targetType, valueStatement);
        }

        sb.append("\"" + metaField.getName() + "\" : ");

        if (targetType.isEnum()) {
          sb.append("{\"" + SerializationParts.ENCODED_TYPE
                  + "\":\"" + targetType.getFullyQualifiedName() + "\",\"" + SerializationParts.ENUM_STRING_VALUE + "\":\"")
                  .append(Stmt.nestedCall(valueStatement).invoke("toString")).append("\"}");


        }
        else {
          sb.append(Stmt.loadVariable(MarshallingGenUtil.getVarName(targetType))
                  .invoke("marshall", valueStatement, Stmt.loadVariable("a1")));
        }

        i++;
      }
    }
    while ((c = c.getSuperClass()) != null);


    sb.append("}");

    builder.append(Stmt.nestedCall(sb).invoke("toString").returnValue());
  }

  private static String keyValue(String key, String value) {
    return "\"" + key + "\":" + value + "";
  }

  private static String string(String value) {
    return "\"" + value + "\"";
  }

  public Statement valueAccessorFor(MetaField field) {
    if (!field.isPublic()) {
      MetaMethod getterMethod = GenUtil.findCaseInsensitiveMatch(field.getType(),
              field.getDeclaringClass(), "get" + field.getName());

      if (getterMethod != null) {
        return Stmt.loadVariable("a0").invoke(getterMethod);
      }
      else {
        if (!context.isExposed(field)) {
          GenUtil.addPrivateAccessStubs(true, context.getClassStructureBuilder(), field);
          context.markExposed(field);
        }

        return Stmt.invokeStatic(context.getGeneratedBootstrapClass(), GenUtil.getPrivateFieldInjectorName(field),
                Stmt.loadVariable("a0"));
      }
    }
    else {
      return Stmt.loadVariable("a0").loadField(field.getName());
    }
  }

  public Statement unwrapJSON(Statement valueStatement, MetaClass toType) {
    if (toType.isEnum()) {
      return Stmt.invokeStatic(Enum.class, "valueOf", toType,
              Stmt.nestedCall(valueStatement).invoke("isObject")
                      .invoke("get", SerializationParts.ENUM_STRING_VALUE).invoke("isString").invoke("stringValue"));
    }
    else {
      return Stmt.create(context.getCodegenContext())
              .loadVariable(MarshallingGenUtil.getVarName(toType))
              .invoke("demarshall", valueStatement, Stmt.loadVariable("a1"));
    }
  }

  private static void loadDefaultMappings() {
    List<ConstructorFieldMapping> fieldMappings = new ArrayList<ConstructorFieldMapping>();
    fieldMappings.add(new ConstructorFieldMapping("declaringClass", MetaClassFactory.get(String.class)));
    fieldMappings.add(new ConstructorFieldMapping("methodName", MetaClassFactory.get(String.class)));
    fieldMappings.add(new ConstructorFieldMapping("fileName", MetaClassFactory.get(String.class)));
    fieldMappings.add(new ConstructorFieldMapping("lineNumber", MetaClassFactory.get(Integer.class)));

    ConstructorMapping mapping = new ConstructorMapping(ConstructionType.Mapped, fieldMappings);
    mapping.setDemarshallOnly(true);

    builtInConstructorMappings.put(StackTraceElement.class.getCanonicalName(), mapping);

  }
}
