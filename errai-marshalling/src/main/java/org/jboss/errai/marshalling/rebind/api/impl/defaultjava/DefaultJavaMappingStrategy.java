/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.marshalling.rebind.api.impl.defaultjava;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Implementations.newStringBuilder;
import static org.jboss.errai.codegen.util.Stmt.declareVariable;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.TernaryStatement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.CatchBlockBuilder;
import org.jboss.errai.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.exceptions.InvalidMappingException;
import org.jboss.errai.marshalling.client.api.exceptions.MarshallingException;
import org.jboss.errai.marshalling.client.api.exceptions.NoAvailableMarshallerException;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.rebind.api.GeneratorMappingContext;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.api.ObjectMapper;
import org.jboss.errai.marshalling.rebind.api.model.ConstructorMapping;
import org.jboss.errai.marshalling.rebind.api.model.FactoryMapping;
import org.jboss.errai.marshalling.rebind.api.model.InstantiationMapping;
import org.jboss.errai.marshalling.rebind.api.model.Mapping;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.api.model.MemberMapping;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;

/**
 * The Errai default Java-to-JSON-to-Java marshaling strategy.
 *
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class DefaultJavaMappingStrategy implements MappingStrategy {
  private GeneratorMappingContext context;
  private MetaClass toMap;
  private boolean gwtTarget;

  public DefaultJavaMappingStrategy(boolean gwtTarget, GeneratorMappingContext context, MetaClass toMap) {
    this.gwtTarget = gwtTarget;
    this.context = context;
    this.toMap = toMap;
  }

  @Override
  public ObjectMapper getMapper() {
    return generateJavaBeanMapper();
  }

  private ObjectMapper generateJavaBeanMapper() {
    final MappingDefinition mapping = context.getDefinitionsFactory().getDefinition(toMap);

    if (mapping == null) {
      throw new InvalidMappingException("no definition for: " + toMap.getFullyQualifiedName());
    }

    if (toMap.isAbstract() || toMap.isInterface()) {
      throw new RuntimeException("cannot map an abstract class or interface: " + toMap.getFullyQualifiedName());
    }

    return new ObjectMapper() {
      @Override
      public Statement getMarshaller() {
        AnonymousClassStructureBuilder classStructureBuilder
                = Stmt.create(context.getCodegenContext())
                .newObject(parameterizedAs(Marshaller.class, typeParametersOf(toMap))).extend();

        classStructureBuilder.publicOverridesMethod("getTypeHandled")
                .append(Stmt.load(toMap).returnValue())
                .finish();

        /**
         *
         * DEMARSHALL METHOD
         *
         */
        BlockBuilder<?> builder =
                classStructureBuilder.publicOverridesMethod("demarshall",
                        Parameter.of(EJValue.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

        BlockBuilder<CatchBlockBuilder> tryBuilder = Stmt.try_();

        tryBuilder.append(Stmt.if_(Bool.expr(Stmt.loadVariable("a0").invoke("isNull")))
                .append(Stmt.load(null).returnValue()).finish());

        tryBuilder.append(Stmt.declareVariable(EJObject.class).named("obj")
                .initializeWith(loadVariable("a0").invoke("isObject")));

        if (toMap.isEnum()) {
          tryBuilder.append(Stmt.declareVariable(toMap).named("entity")
                  .initializeWith(demarshallEnum(loadVariable("obj"), loadVariable("a0"), toMap)));
        }
        else {

          tryBuilder.append(Stmt.declareVariable(String.class).named("objId")
                  .initializeWith(loadVariable("obj")
                          .invoke("get", SerializationParts.OBJECT_ID)
                          .invoke("isString").invoke("stringValue")));

          tryBuilder.append(
                  Stmt.if_(Bool.expr(loadVariable("a1").invoke("hasObjectHash", loadVariable("objId"))))
                          .append(loadVariable("a1").invoke("getObject", toMap, loadVariable("objId")).returnValue()).finish());

          InstantiationMapping instantiationMapping = mapping.getInstantiationMapping();

          /**
           * Figure out how to construct this object.
           */
          Mapping[] cMappings = instantiationMapping.getMappings();
          if (cMappings.length > 0) {
            // use constructor mapping.

            final List<Statement> constructorParameters = new ArrayList<Statement>();

            for (Mapping m : mapping.getInstantiationMapping().getMappings()) {
              MetaClass type = m.getType().asBoxed();
              if (context.canMarshal(type.getFullyQualifiedName())) {
                if (type.isArray()) {
                  constructorParameters.add(context.getArrayMarshallerCallback()
                          .demarshall(type, extractJSONObjectProperty(m.getKey(), EJObject.class)));
                }
                else {
                  constructorParameters.add(fieldDemarshall(m, EJObject.class));
                }
              }
              else {
                throw new MarshallingException("no marshaller for type: " + type);
              }
            }

            if (instantiationMapping instanceof ConstructorMapping) {
              tryBuilder.append(Stmt.declareVariable(toMap).named("entity")
                      .initializeWith(Stmt.newObject(toMap)
                              .withParameters(constructorParameters.toArray(new Object[constructorParameters.size()]))));
            }
            else if (instantiationMapping instanceof FactoryMapping) {
              tryBuilder.append(Stmt.declareVariable(toMap).named("entity")
                      .initializeWith(Stmt.invokeStatic(toMap, ((FactoryMapping) instantiationMapping).getMember().getName(),
                              constructorParameters.toArray(new Object[constructorParameters.size()]))));
            }
          }
          else {
            // use default constructor

            tryBuilder.append(Stmt.declareVariable(toMap).named("entity").initializeWith(Stmt.nestedCall(Stmt.newObject(toMap))));
          }

          tryBuilder.append(loadVariable("a1").invoke("recordObjectHash",
                  loadVariable("objId"), loadVariable("entity")));
        }

        /**
         * Start binding of fields here.
         */
        for (MemberMapping memberMapping : mapping.getMemberMappings()) {
          if (!memberMapping.canWrite()) continue;

          Statement bindingStatement;
          Statement val;
          if (memberMapping.getType().isArray()) {
            val = context.getArrayMarshallerCallback()
                    .demarshall(memberMapping.getType(), extractJSONObjectProperty(memberMapping.getKey(), EJObject.class));
          }
          else {
            val = fieldDemarshall(memberMapping, MetaClassFactory.get(EJObject.class));
          }

          if (memberMapping.getBindingMember() instanceof MetaField) {
            MetaField field = (MetaField) memberMapping.getBindingMember();

            // handle long case -- GWT does not support long in JSNI
            if (field.isPublic()) {
              tryBuilder.append(loadVariable("entity").loadField(field.getName()).assignValue(val));
              continue;
            }
            else {
              MetaMethod setterMeth = GenUtil.findCaseInsensitiveMatch(null,
                      field.getDeclaringClass(), "set" + field.getName(),
                      field.getType());

              if (setterMeth != null && !setterMeth.isPrivate()) {
                // Bind via setter
                bindingStatement = loadVariable("entity").invoke(setterMeth, Cast.to(memberMapping.getTargetType(), val));
              }
              else if (field.getType().getCanonicalName().equals("long")) {
                throw new RuntimeException("cannot support private field marshalling of long type" +
                        " (not supported by JSNI) for field: "
                        + field.getDeclaringClass().getFullyQualifiedName() + "#" + field.getName());
              }
              else {
                if (!context.isExposed(field)) {
                  PrivateAccessUtil.addPrivateAccessStubs(gwtTarget, context.getClassStructureBuilder(), field);
                  context.markExposed(field);
                }

                // Bind via JSNI
                bindingStatement = Stmt.invokeStatic(context.getGeneratedBootstrapClass(),
                        PrivateAccessUtil.getPrivateFieldInjectorName(field),
                        loadVariable("entity"), val);
              }

            }
          }
          else if (memberMapping.getBindingMember() instanceof MetaMethod) {
            bindingStatement = loadVariable("entity").invoke(((MetaMethod) memberMapping.getBindingMember()),
                    Cast.to(memberMapping.getTargetType(), val));
          }
          else {
            throw new RuntimeException("unknown member mapping type: " + memberMapping.getType());
          }

          BlockBuilder<ElseBlockBuilder> ifBlockBuilder = Stmt.if_(Bool.and(
                  Bool.expr(loadVariable("obj").invoke("containsKey", memberMapping.getKey())),
                  Bool.notExpr(loadVariable("obj").invoke("get", memberMapping.getKey()).invoke("isNull"))));

          MetaClass elementType = MarshallingGenUtil.getConcreteCollectionElementType(memberMapping.getType());
          if (elementType != null) {
            ifBlockBuilder.append(Stmt.loadVariable("a1").invoke("setAssumedElementType", elementType.getFullyQualifiedName()));
          }

          ifBlockBuilder.append(bindingStatement);

          if (elementType != null) {
            ifBlockBuilder.append(Stmt.loadVariable("a1").invoke("setAssumedElementType", (String) null));
          }

          tryBuilder.append(ifBlockBuilder.finish());
        }

        tryBuilder.append(loadVariable("entity").returnValue());

        tryBuilder.finish()
                .catch_(Throwable.class, "t")
                .append(loadVariable("t").invoke("printStackTrace"))
                .append(Stmt.throw_(RuntimeException.class,
                        "error demarshalling entity: " + toMap.getFullyQualifiedName(), loadVariable("t")))
                .finish();

        builder.append(tryBuilder.finish()).finish();


        /**
         *
         * MARSHAL METHOD
         *
         */
        BlockBuilder<?> marshallMethodBlock = classStructureBuilder.publicOverridesMethod("marshall",
                Parameter.of(toMap, "a0"), Parameter.of(MarshallingSession.class, "a1"));

        marshallToJSON(marshallMethodBlock, toMap, mapping);

        marshallMethodBlock.finish();

        return classStructureBuilder.finish();
      }
    };
  }


  public Statement fieldDemarshall(Mapping mapping, Class<?> fromType) {
    return fieldDemarshall(mapping, MetaClassFactory.get(fromType));
  }

  public Statement fieldDemarshall(Mapping mapping, MetaClass fromType) {
    Statement statement = unwrapJSON(extractJSONObjectProperty(mapping.getKey(), fromType), mapping.getType());
    if (!mapping.getTargetType().equals(mapping.getType())) {
      return Cast.to(mapping.getTargetType(), statement);
    }
    else {
      return statement;
    }
  }

  public Statement extractJSONObjectProperty(String fieldName, Class fromType) {
    return extractJSONObjectProperty(fieldName, MetaClassFactory.get(fromType));
  }

  public Statement extractJSONObjectProperty(String fieldName, MetaClass fromType) {
    if (fromType.getFullyQualifiedName().equals(EJObject.class.getName())) {
      return loadVariable("obj").invoke("get", fieldName);
    }
    else {
      return Stmt.nestedCall(Cast.to(fromType, loadVariable("a0"))).invoke("get", fieldName);
    }
  }

  private int calcBufferSize(List<MappingDefinition> stack, MappingDefinition definition) {
    int bufSize = 128;

    if (!stack.contains(definition)) {
      stack.add(definition);

      for (MemberMapping mapping : definition.getMemberMappings()) {
        MappingDefinition def = context.getDefinitionsFactory().getDefinition(mapping.getType());

        if (def == null) {
          if (mapping.getType().isArray()) {
            def = context.getDefinitionsFactory().getDefinition(mapping.getType().getOuterComponentType().asBoxed());

            if (def == null) {
              System.out.println("not found: " + mapping.getType().getOuterComponentType());
            }

            bufSize += (calcBufferSize(stack, def)) * 4;
          }

          continue;
        }

        bufSize += calcBufferSize(stack, def);
      }
    }
    return bufSize;
  }

  public void marshallToJSON(BlockBuilder<?> builder, MetaClass toType, MappingDefinition definition) {
    if (!context.canMarshal(toType.getFullyQualifiedName())) {
      throw new NoAvailableMarshallerException(toType.getName());
    }

    builder.append(
            Stmt.if_(Bool.isNull(loadVariable("a0")))
                    .append(Stmt.load("null").returnValue()).finish()
    );

    if (toMap.isEnum()) {
      builder.append(Stmt.nestedCall(marshallEnum(newStringBuilder(256), Stmt.loadVariable("a0"), toMap))
              .invoke("toString").returnValue());
      return;
    }

    int bufSize = calcBufferSize(new ArrayList<MappingDefinition>(), definition);

    Implementations.StringBuilderBuilder sb = newStringBuilder(bufSize)
            .append("{" + keyValue(SerializationParts.ENCODED_TYPE, string(toType.getFullyQualifiedName())) + "," +
                    string(SerializationParts.OBJECT_ID) + ":\"").append(loadVariable("objId")).append("\"");

    builder.append(
            Stmt.if_(Bool.expr(loadVariable("a1").invoke("hasObjectHash", loadVariable("a0"))))
                    .append(declareVariable(String.class).named("objId").initializeWith(loadVariable("a1").invoke("getObjectHash", Stmt.loadVariable("a0"))))
                    .append(Stmt.nestedCall(newStringBuilder(128).append("{"
                            + keyValue(SerializationParts.ENCODED_TYPE, string(toType.getFullyQualifiedName()))).append(",")
                            .append(string(SerializationParts.OBJECT_ID) + ":\"")
                            .append(loadVariable("objId"))
                            .append("\"}")).invoke("toString").returnValue())
                    .finish());

    builder.append(declareVariable(String.class).named("objId").initializeWith(loadVariable("a1").invoke("getObjectHash", Stmt.loadVariable("a0"))));

    builder.append(loadVariable("a1").invoke("recordObjectHash", loadVariable("objId"),
            loadVariable("objId")));

    boolean hasEncoded = false;

    int i = 0;
    for (MemberMapping mapping : definition.getMemberMappings()) {
      if (!mapping.canRead()) {
        continue;
      }

      if (!hasEncoded) {
        sb.append(",");
        hasEncoded = true;
      }
      else if (i > 0) {
        sb.append(",");
      }

      MetaClass targetType = GenUtil.getPrimitiveWrapper(mapping.getType());

      MetaClass compType = targetType.isArray() ? targetType.getOuterComponentType().asBoxed() : targetType.asBoxed();

      if (!targetType.isEnum() && !context.canMarshal(compType.getFullyQualifiedName())) {
        throw new NoAvailableMarshallerException(compType.getFullyQualifiedName());
      }

      Statement valueStatement = valueAccessorFor(mapping.getReadingMember());
      if (targetType.isArray()) {
        valueStatement = context.getArrayMarshallerCallback().marshal(targetType, valueStatement);
      }
      sb.append("\"" + mapping.getKey() + "\" : ");

      if (targetType.isEnum()) {
        marshallEnum(sb, valueStatement, targetType);
      }
      else {
        sb.append(loadVariable(MarshallingGenUtil.getVarName(targetType))
                .invoke("marshall", valueStatement, loadVariable("a1")));
      }

      i++;
    }

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

  public Statement valueAccessorFor(MetaClassMember member) {
    if (member instanceof MetaField) {
      MetaField field = (MetaField) member;
      if (!field.isPublic()) {
        MetaMethod getterMethod = GenUtil.findCaseInsensitiveMatch(field.getType(),
                field.getDeclaringClass(), "get" + field.getName());

        if (getterMethod != null) {
          return loadVariable("a0").invoke(getterMethod);
        }
        else {
          if (!context.isExposed(field)) {
            PrivateAccessUtil.addPrivateAccessStubs(gwtTarget, context.getClassStructureBuilder(), field);
            context.markExposed(field);
          }

          return Stmt.invokeStatic(context.getGeneratedBootstrapClass(), PrivateAccessUtil.getPrivateFieldInjectorName(field),
                  loadVariable("a0"));
        }
      }
      else {
        return loadVariable("a0").loadField(field.getName());
      }
    }
    else {
      MetaMethod method = (MetaMethod) member;
      return loadVariable("a0").invoke(method);
    }
  }

  public Statement demarshallEnum(Statement objStatement, Statement valStatement, MetaClass toType) {
    Statement trueStatement = Stmt.invokeStatic(Enum.class, "valueOf", toType,
            Stmt.nestedCall(objStatement)
                    .invoke("get", SerializationParts.ENUM_STRING_VALUE).invoke("isString").invoke("stringValue"));

    Statement falseStatement = (valStatement != null) ? Stmt.invokeStatic(Enum.class, "valueOf", toType,
            Stmt.nestedCall(valStatement).invoke("isString").invoke("stringValue")) : Stmt.load(null);

    TernaryStatement ternaryStatement =
            new TernaryStatement(Bool.isNotNull(objStatement), trueStatement, falseStatement);

    return ternaryStatement;
  }

  public Implementations.StringBuilderBuilder marshallEnum(Implementations.StringBuilderBuilder sb,
                                                           Statement valueStatement,
                                                           MetaClass toType) {

    Implementations.StringBuilderBuilder internalSBB = Implementations.newStringBuilder()
            .append("{\"" + SerializationParts.ENCODED_TYPE
                    + "\":\"" + toType.getFullyQualifiedName() + "\",\"" + SerializationParts.ENUM_STRING_VALUE + "\":\"")
            .append(Stmt.nestedCall(valueStatement).invoke("name")).append("\"}");

    TernaryStatement ternaryStatement = new TernaryStatement(
            Bool.isNotNull(valueStatement), internalSBB, Stmt.load("null"));

    return sb.append(ternaryStatement);
  }


  public Statement unwrapJSON(Statement valueStatement, MetaClass toType) {
    if (toType.isEnum()) {
      return demarshallEnum(Stmt.nestedCall(valueStatement).invoke("isObject"), valueStatement, toType);
    }
    else {
      return Stmt.create(context.getCodegenContext())
              .loadVariable(MarshallingGenUtil.getVarName(toType))
              .invoke("demarshall", valueStatement, loadVariable("a1"));
    }
  }
}