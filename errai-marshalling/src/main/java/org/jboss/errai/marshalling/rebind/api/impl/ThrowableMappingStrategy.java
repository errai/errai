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

package org.jboss.errai.marshalling.rebind.api.impl;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.CatchBlockBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.util.Bool;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.rebind.api.MappingContext;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.api.ObjectMapper;

import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.framework.util.Stmt.loadVariable;

/**
 * @author Mike Brock
 */
public class ThrowableMappingStrategy implements MappingStrategy {
  private MappingContext context;
  private MetaClass toMap;

  public ThrowableMappingStrategy(MappingContext context, MetaClass toMap) {
    this.context = context;
    this.toMap = toMap;
  }

  @Override
  public ObjectMapper getMapper() {
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

     //   marshallToJSON(marshallMethodBlock, toMap);

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
}
