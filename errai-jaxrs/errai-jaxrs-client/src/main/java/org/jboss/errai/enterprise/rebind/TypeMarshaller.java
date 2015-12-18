/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.enterprise.rebind;

import java.util.Date;
import java.util.Map;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.TernaryStatement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;

/**
 * Generates the required {@link Statement}s for type marshalling.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class TypeMarshaller {

  public static Statement marshal(Statement statement) {
    return marshal(statement, "text/plain");
  }
  
  public static Statement marshal(MetaClass type, Statement statement) {
    return marshal(type, statement, "text/plain");
  }

  public static Statement marshal(Statement statement, String contentType) {
    if (statement instanceof Parameter) {
      Parameter param = (Parameter) statement;
      return marshal(param.getType(), Variable.get(param.getName()), contentType);
    }
    return marshal(statement.getType(), statement, contentType);
  }

  public static Statement marshal(MetaClass type, Statement statement, String contentType) {
    Statement marshallingStatement = null;
    if (PrimitiveTypeMarshaller.canHandle(type, contentType)) {
      marshallingStatement = PrimitiveTypeMarshaller.marshal(type, statement);
    }
    else {
      marshallingStatement = Stmt.invokeStatic(MarshallingWrapper.class, "toJSON", statement);
    }
    return marshallingStatement;
  }

  public static Statement demarshal(MetaClass type, Statement statement, String accepts) {
    Statement demarshallingStatement = null;
    if (PrimitiveTypeMarshaller.canHandle(type, accepts)) {
      demarshallingStatement = PrimitiveTypeMarshaller.demarshal(type, statement);
    }
    else {
      if (!type.equals(MetaClassFactory.get(void.class))) {
        if (type.isAssignableTo(Map.class)) {
          demarshallingStatement =
              Stmt.invokeStatic(MarshallingWrapper.class, "fromJSON", statement, type.asBoxed().asClass(),
                  MarshallingGenUtil.getConcreteMapKeyType(type.asBoxed()),
                  MarshallingGenUtil.getConcreteMapValueType(type.asBoxed()));
        }
        else {
          demarshallingStatement = Stmt.invokeStatic(MarshallingWrapper.class, "fromJSON", statement,
              type.asBoxed().asClass(), MarshallingGenUtil.getConcreteElementType(type.asBoxed()));
        }
      }
      else {
        demarshallingStatement = Stmt.invokeStatic(MarshallingWrapper.class, "fromJSON", statement);
      }
    }
    return demarshallingStatement;
  }

  /**
   * Works for all types that have a 'copy constructor', a toString() representation and a valueOf() method (all
   * primitive wrapper types and java.lang.String). Will only be used in case text/plain was specified as mime type.
   */
  public static class PrimitiveTypeMarshaller {

    public static boolean canHandle(MetaClass type, String mimeType) {
      boolean canHandle = false;
      if (("text/plain".equals(mimeType) && type.asUnboxed().isPrimitive())
          || type.equals(MetaClassFactory.get(String.class)) 
          || type.equals(MetaClassFactory.get(Date.class))
          || type.isEnum()) {
        canHandle = true;
      }
      return canHandle;
    }

    private static Statement marshal(MetaClass type, Statement statement) {
      ContextualStatementBuilder s = null;
      if (type.isPrimitive()) {
        s = Stmt.nestedCall(Stmt.newObject(type.asBoxed()).withParameters(statement)).invoke("toString");
      }
      else {
        s = Stmt.nestedCall(new TernaryStatement(Bool.isNull(statement), Stmt.load(""), Stmt.nestedCall(statement)));
        if (!type.equals(MetaClassFactory.get(String.class))) {
          s = s.invoke("toString");
        }
      }

      return s;
    }

    private static Statement demarshal(MetaClass type, Statement statement) {
      if (type.equals(statement.getType())) {
        return statement;
      }

      if (MetaClassFactory.get(void.class).equals(type)) {
        return Stmt.load(null);
      }
      
      if (MetaClassFactory.get(Date.class).equals(type)) {
        return Stmt.newObject(Date.class, statement);
      }

      return Stmt.invokeStatic(type.asBoxed(), "valueOf", statement);
    }
  }
}
