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
import org.jboss.errai.marshalling.rebind.api.model.ConstructorMapping;
import org.jboss.errai.marshalling.rebind.api.model.Mapping;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.api.model.MemberMapping;
import org.jboss.errai.marshalling.rebind.api.model.impl.SimpleConstructorMapping;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.framework.util.Implementations.newStringBuilder;
import static org.jboss.errai.codegen.framework.util.Stmt.loadVariable;

/**
 * The Errai default Java-to-JSON-to-Java marshaling strategy.
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DefaultJavaMappingStrategy implements MappingStrategy {
  private MappingContext context;
  private MetaClass toMap;

  private static Map<String, MappingDefinition> mappingDefinitions = new HashMap<String, MappingDefinition>();

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
    final MappingDefinition mapping = findUsableMapping();

    final List<Statement> marshallers = new ArrayList<Statement>();

    for (Mapping m : mapping.getConstructorMapping().getMappings()) {
      if (context.hasProvidedOrGeneratedMarshaller(m.getType())) {
        if (m.getType().isArray()) {
          marshallers.add(context.getArrayMarshallerCallback()
                  .demarshall(m.getType(), extractJSONObjectProperty(m.getKey(), JSONObject.class)));
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

        BlockBuilder<?> methBuilder =
                classStructureBuilder.publicOverridesMethod("demarshall",
                        Parameter.of(Object.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

        methBuilder.append(Stmt.declareVariable(JSONObject.class).named("obj").finish());

        /**
         * Check to see if value is null. If so, return null.
         */
        methBuilder.append(
                Stmt.if_(Bool.or(Bool.isNull(loadVariable("a0")),
                        Bool.isNotNull(loadVariable("a0").invoke("isNull"))))
                        .append(Stmt.load(null).returnValue())
                        .finish()
                        .else_()
                        .append(loadVariable("obj").assignValue(loadVariable("a0").invoke("isObject")))
                        .finish()
        );

        // String objId = a0.get(SerializationParts.OBJECTID).isString().stringValue();
        methBuilder.append(Stmt.declareVariable(String.class).named("objId")
                .initializeWith(loadVariable("obj")
                        .invoke("get", SerializationParts.OBJECT_ID)
                        .invoke("isString").invoke("stringValue")));

        methBuilder.append(
                Stmt.if_(Bool.expr(loadVariable("a1").invoke("hasObjectHash", loadVariable("objId"))))
                        .append(loadVariable("a1").invoke("getObject", toMap, loadVariable("objId"))
                                .returnValue()).finish());

        methBuilder.append(Stmt.declareVariable(toMap).named("entity")
                .initializeWith(Stmt.newObject(toMap)
                        .withParameters(marshallers.toArray(new Object[marshallers.size()]))));

        methBuilder.append(loadVariable("a1").invoke("recordObjectHash",
                loadVariable("objId"), loadVariable("entity")));

        methBuilder.append(loadVariable("entity").returnValue());

        methBuilder.finish();

        BlockBuilder<?> marshallMethodBlock = classStructureBuilder.publicOverridesMethod("marshall",
                Parameter.of(Object.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

        marshallMethodBlock.append(
                Stmt.if_(Bool.isNull(loadVariable("a0")))
                        .append(Stmt.load("null").returnValue()).finish()
        );

        if (!mapping.canMarshal()) {
          marshallMethodBlock.append(Stmt.load(null).returnValue());
        }
        else {
          marshallToJSON(marshallMethodBlock, toMap);
        }

        marshallMethodBlock.finish();

        classStructureBuilder.publicOverridesMethod("handles", Parameter.of(Object.class, "a0"))
                .append(Stmt.nestedCall(Bool.and(
                        Bool.notEquals(loadVariable("a0").invoke("isObject"), null),
                        loadVariable("a0").invoke("isObject").invoke("get", SerializationParts.ENCODED_TYPE)
                                .invoke("equals", loadVariable("this").invoke("getTypeHandled").invoke("getName"))
                )).returnValue()).finish();

        return classStructureBuilder.finish();
      }
    };
  }

  private ObjectMapper generateJavaBeanMapper() {
    final MappingDefinition mapping = finaUsuableBeanMapping();

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


        tryBuilder.append(Stmt.declareVariable(JSONObject.class).named("obj")
                .initializeWith(loadVariable("a0").invoke("isObject")));


        tryBuilder.append(Stmt.declareVariable(String.class).named("objId")
                .initializeWith(loadVariable("obj")
                        .invoke("get", SerializationParts.OBJECT_ID)
                        .invoke("isString").invoke("stringValue")));


        tryBuilder.append(
                Stmt.if_(Bool.expr(loadVariable("a1").invoke("hasObjectHash", loadVariable("objId"))))
                        .append(loadVariable("a1").invoke("getObject", toMap, loadVariable("objId")).returnValue()).finish());

        tryBuilder.append(Stmt.declareVariable(toMap).named("entity").initializeWith(Stmt.nestedCall(Stmt.newObject(toMap))));

        tryBuilder.append(loadVariable("a1").invoke("recordObjectHash",
                loadVariable("objId"), loadVariable("entity")));

        /**
         * Start binding of fields here.
         */
        for (MemberMapping memberMapping : mapping.getMemberMappings()) {
          Statement bindingStatement;
          Statement val;
          if (memberMapping.getType().isArray()) {
            val = context.getArrayMarshallerCallback()
                    .demarshall(memberMapping.getType(), extractJSONObjectProperty(memberMapping.getKey(), JSONValue.class));
          }
          else {
            val = fieldDemarshall(memberMapping.getKey(), MetaClassFactory.get(JSONValue.class), memberMapping.getType().asBoxed());
          }

          if (memberMapping.getMember() instanceof MetaField) {
            MetaField field = (MetaField) memberMapping.getMember();

            // handle long case -- GWT does not support long in JSNI
            if (field.isPublic()) {
              tryBuilder.append(loadVariable("entity").loadField(field.getName()).assignValue(val));
              continue;
            }
            else {
              MetaMethod setterMeth = GenUtil.findCaseInsensitiveMatch(null,
                      field.getDeclaringClass(), "set" + field.getName(),
                      field.getType());

              if (setterMeth != null) {
                // Bind via setter
                bindingStatement = loadVariable("entity").invoke(setterMeth, val);
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

                // Bind via JSNI
                bindingStatement = Stmt.invokeStatic(context.getGeneratedBootstrapClass(),
                        GenUtil.getPrivateFieldInjectorName(field),
                        loadVariable("entity"), val);
              }

            }
          }
          else if (memberMapping.getMember() instanceof MetaMethod) {
            bindingStatement = loadVariable("entity").invoke(((MetaMethod) memberMapping.getMember()), val);
          }
          else {
            throw new RuntimeException("unknown member mapping type: " + memberMapping.getType());
          }


          tryBuilder.append(
                  Stmt.if_(Bool.and(
                          Bool.expr(loadVariable("obj").invoke("containsKey", memberMapping.getKey())),
                          Bool.isNull(loadVariable("obj").invoke("get", memberMapping.getKey()).invoke("isNull"))

                  )).append(bindingStatement).finish());

        }


        tryBuilder.append(loadVariable("entity").returnValue());

        tryBuilder.finish()
                .catch_(Throwable.class, "t")
                .append(loadVariable("t").invoke("printStackTrace"))
                .append(Stmt.throw_(RuntimeException.class,
                        "error demarshalling entity: " + toMap.getFullyQualifiedName(), loadVariable("t")))
                .finish();

        builder.append(tryBuilder.finish()).finish();

        BlockBuilder<?> marshallMethodBlock = classStructureBuilder.publicOverridesMethod("marshall",
                Parameter.of(Object.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

        marshallToJSON(marshallMethodBlock, toMap);

        marshallMethodBlock.finish();

        classStructureBuilder.publicOverridesMethod("handles", Parameter.of(Object.class, "a0"))
                .append(Stmt.nestedCall(Bool.and(
                        Bool.notEquals(loadVariable("a0").invoke("isObject"), null),
                        loadVariable("a0").invoke("isObject").invoke("get", SerializationParts.ENCODED_TYPE)
                                .invoke("equals", loadVariable("this").invoke("getTypeHandled").invoke("getName"))
                )).returnValue()).finish();

        return classStructureBuilder.finish();
      }
    };
  }

  private MappingDefinition finaUsuableBeanMapping() {
    MetaConstructor constructor = toMap.getConstructor(new MetaClass[0]);

    MappingDefinition definition = new MappingDefinition(toMap);

    if (constructor == null) {
      throw new InvalidMappingException("cannot find a default, no-argument constructor or field-mapped constructor in: "
              + toMap.getFullyQualifiedName());
    }

    List<MetaField> mappings = new ArrayList<MetaField>();

    MetaClass c = toMap;

    do {
      for (final MetaField field : c.getDeclaredFields()) {
        if (field.isTransient() || field.isStatic()) {
          continue;
        }

        MetaClass type = field.getType();

        if (!type.isEnum() && !context.hasProvidedOrGeneratedMarshaller(type)) {
          throw new InvalidMappingException("portable entity " + toMap.getFullyQualifiedName()
                  + " contains a field (" + field.getName() + ") that is not known to the marshaller: "
                  + type.getFullyQualifiedName());
        }


        definition.addMemberMapping(new MemberMapping() {
          @Override
          public MetaClassMember getMember() {
            return field;
          }

          @Override
          public String getKey() {
            return field.getName();
          }

          @Override
          public MetaClass getType() {
            return field.getType();
          }
        });

        // mappings.add(field);
      }
    }
    while ((c = c.getSuperClass()) != null);

    return definition;
    //  return new BeanMapping(mappings);
  }

  private MappingDefinition findUsableMapping() {
    if (mappingDefinitions.containsKey(toMap.getCanonicalName())) {
      return mappingDefinitions.get(toMap.getCanonicalName());
    }

    Set<MetaConstructor> constructors = new HashSet<MetaConstructor>();
    // List<ConstructorFieldMapping> mappings = new ArrayList<ConstructorFieldMapping>();

    SimpleConstructorMapping simpleConstructorMapping = new SimpleConstructorMapping(toMap);

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
                simpleConstructorMapping.mapParmToIndex(fieldName, i, c.getParameters()[i].getType());

                //  mappings.add(new ConstructorFieldMapping(fieldName, c.getParameters()[i].getType()));
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

    MappingDefinition definition = new MappingDefinition(toMap);
    definition.setConstructorMapping(simpleConstructorMapping);

    return definition;

    //  return new AutoConstructorMapping(ConstructionType.Mapped, mappings);
  }

  private static enum ConstructionType {
    Mapped, Custom
  }

  private static class BeanMapping {
    List<MetaField> mappings;
    private boolean usePrivateInjectionOnly = false;

    private BeanMapping(List<MetaField> mappings) {
      this.mappings = mappings;
    }

    public List<MetaField> getMappings() {
      return mappings;
    }

    public boolean isUsePrivateInjectionOnly() {
      return usePrivateInjectionOnly;
    }

    public void setUsePrivateInjectionOnly(boolean usePrivateInjectionOnly) {
      this.usePrivateInjectionOnly = usePrivateInjectionOnly;
    }
  }

//
//  private static class AutoConstructorMapping implements ConstructorMapping {
//    ConstructionType type;
//    List<ConstructorFieldMapping> mappings;
//    private boolean demarshallOnly;
//
//    private AutoConstructorMapping(ConstructionType type, List<ConstructorFieldMapping> mappings) {
//      this.type = type;
//      this.mappings = mappings;
//    }
//
//    public ConstructionType getType() {
//      return type;
//    }
//
//    public List<ConstructorFieldMapping> getMappings() {
//      return mappings;
//    }
//
//    public boolean isDemarshallOnly() {
//      return demarshallOnly;
//    }
//
//    public void setDemarshallOnly(boolean demarshallOnly) {
//      this.demarshallOnly = demarshallOnly;
//    }
//
//    @Override
//    public MetaClass[] getConstructorParmTypes() {
//      return new MetaClass[0];
//    }
//
//    @Override
//    public String[] getKeyNames() {
//      return new String[0];
//    }
//  }
//
//  private static class ConstructorFieldMapping {
//    String fieldName;
//    MetaClass type;
//
//    private ConstructorFieldMapping(String fieldName, MetaClass type) {
//      this.fieldName = fieldName;
//      this.type = type;
//    }
//
//    public String getFieldName() {
//      return fieldName;
//    }
//
//    public MetaClass getType() {
//      return type;
//    }
//  }

  private boolean isJavaBean(MetaClass toMap) {
    return toMap.getConstructor(new MetaClass[0]) != null;
  }

  public Statement fieldDemarshall(Mapping mapping, Class<?> fromType) {
    return fieldDemarshall(mapping, MetaClassFactory.get(fromType));
  }

  public Statement fieldDemarshall(Mapping mapping, MetaClass fromType) {
    return fieldDemarshall(mapping.getKey(), fromType, mapping.getType());
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
      return loadVariable("obj").invoke("get", fieldName);
    }
    else {
      return Stmt.nestedCall(Cast.to(fromType, loadVariable("a0"))).invoke("get", fieldName);
    }
  }

  public void marshallToJSON(BlockBuilder<?> builder, MetaClass toType) {
    if (!context.hasProvidedOrGeneratedMarshaller(toType)) {
      throw new NoAvailableMarshallerException(toType.getName());
    }

    builder.append(
            Stmt.if_(Bool.isNull(loadVariable("a0")))
                    .append(Stmt.load("null").returnValue()).finish()
    );

    builder.append(Stmt.declareVariable(String.class).named("objId")
            .initializeWith(Stmt.invokeStatic(String.class, "valueOf", loadVariable("a0").invoke("hashCode"))));

    Implementations.StringBuilderBuilder sb = newStringBuilder().append("{")
            .append(keyValue(SerializationParts.ENCODED_TYPE, string(toType.getFullyQualifiedName()))).append(",")
            .append(string(SerializationParts.OBJECT_ID) + ":\"").append(loadVariable("objId")).append("\"");

    builder.append(
            Stmt.if_(Bool.expr(loadVariable("a1").invoke("hasObjectHash", loadVariable("objId"))))
                    .append(Stmt.nestedCall(newStringBuilder().append("{")
                            .append(keyValue(SerializationParts.ENCODED_TYPE, string(toType.getFullyQualifiedName()))).append(",")
                            .append(string(SerializationParts.OBJECT_ID) + ":\"")
//                            .append("$")
                            .append(loadVariable("objId"))
                            .append("\"}")).invoke("toString").returnValue())
                    .finish());

    builder.append(loadVariable("a1").invoke("recordObjectHash", loadVariable("objId"),
            loadVariable("objId")));

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
          sb.append(loadVariable(MarshallingGenUtil.getVarName(targetType))
                  .invoke("marshall", valueStatement, loadVariable("a1")));
        }

        i++;
      }
    }
    while ((c = c.getSuperClass()) != null);

    if (i == 0) {
      sb.append(",\"" + SerializationParts.INSTANTIATE_ONLY + "\":true");
    }

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
        return loadVariable("a0").invoke(getterMethod);
      }
      else {
        if (!context.isExposed(field)) {
          GenUtil.addPrivateAccessStubs(true, context.getClassStructureBuilder(), field);
          context.markExposed(field);
        }

        return Stmt.invokeStatic(context.getGeneratedBootstrapClass(), GenUtil.getPrivateFieldInjectorName(field),
                loadVariable("a0"));
      }
    }
    else {
      return loadVariable("a0").loadField(field.getName());
    }
  }

  public Statement unwrapJSON(Statement valueStatement, MetaClass toType) {
    if (toType.isEnum()) {
      return Stmt.invokeStatic(MarshallUtil.class, "demarshalEnum", toType,
              Stmt.nestedCall(valueStatement).invoke("isObject"),
              SerializationParts.ENUM_STRING_VALUE);
    }
    else {
      return Stmt.create(context.getCodegenContext())
              .loadVariable(MarshallingGenUtil.getVarName(toType))
              .invoke("demarshall", valueStatement, loadVariable("a1"));
    }
  }

  private static void loadDefaultMappings() {
    SimpleConstructorMapping simpleConstructorMapping = new SimpleConstructorMapping(StackTraceElement.class);
    simpleConstructorMapping.mapParmToIndex("declaringClass", 0, String.class);
    simpleConstructorMapping.mapParmToIndex("methodName", 1, String.class);
    simpleConstructorMapping.mapParmToIndex("fileName", 2, String.class);
    simpleConstructorMapping.mapParmToIndex("lineNumber", 3, Integer.class);

    MappingDefinition definition = new MappingDefinition(StackTraceElement.class);
    definition.setMarshal(false);
    definition.setConstructorMapping(simpleConstructorMapping);

    addMappingDefinition(definition);
  }

  public static void addMappingDefinition(MappingDefinition definition) {
    mappingDefinitions.put(definition.getMappingClass().getFullyQualifiedName(), definition);
  }
}
