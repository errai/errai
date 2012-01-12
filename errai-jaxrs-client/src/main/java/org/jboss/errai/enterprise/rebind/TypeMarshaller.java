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

package org.jboss.errai.enterprise.rebind;

import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.marshalling.client.Marshalling;

/**
 * Generates the required {@link Statement}s for type marshalling.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class TypeMarshaller {

  public static Statement marshal(Parameter param) {
    return marshal(param.getType(), Variable.get(param.getName()));
  }
  
  public static Statement marshal(MetaClass type, Statement statement) {
    Statement marshallingStatement = null;
    if (type.asUnboxed().isPrimitive() || type.equals(MetaClassFactory.get(String.class))) {
      marshallingStatement =  PrimitiveTypeMarshaller.marshal(type, statement);
    }
    else {
      marshallingStatement = Stmt.invokeStatic(Marshalling.class, "toJSON", statement);
    }
    return marshallingStatement;
  }
  
  public static Statement demarshal(Parameter param) {
    return demarshal(param.getType(), Variable.get(param.getName()));
  }
  
  public static Statement demarshal(MetaClass type, Statement statement) {
    Statement demarshallingStatement = null;
    if (type.asUnboxed().isPrimitive() || type.equals(MetaClassFactory.get(String.class))) {
      demarshallingStatement =  PrimitiveTypeMarshaller.demarshal(type, statement);
    }
    else {
      demarshallingStatement = Stmt.invokeStatic(Marshalling.class, "fromJSON", statement);
    }
    return demarshallingStatement;
  }
  
  /**
   * Works for all types that have a 'copy constructor', a toString() representation
   * and a valueOf() method (all primitive wrapper types and java.lang.String).
   */
  private static class PrimitiveTypeMarshaller {

    private static Statement marshal(MetaClass type, Statement statement) {
      return Stmt.nestedCall(Stmt.newObject(type.asBoxed()).withParameters(statement)).invoke("toString");
    }
    
    private static Statement demarshal(MetaClass type, Statement statement) {
      if (MetaClassFactory.get(void.class).equals(type))
        return Stmt.load(null);
      
      return Stmt.invokeStatic(type.asBoxed(), "valueOf", statement);
    }
  }
}