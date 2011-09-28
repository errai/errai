package org.jboss.errai.enterprise.rebind;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Stmt;

/**
 * Marshaller for primitive types. 
 * 
 * Works for all types that have a copy constructor, a toString() 
 * and a valueOf() method (all primitive wrapper types + String).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PrimitiveTypeMarshaller {

  public static Statement marshal(MetaClass type, String varName) {
    return Stmt.nestedCall(Stmt.newObject(type.asBoxed()).withParameters(Variable.get(varName))).invoke("toString");
  }
  
  public static Statement demarshal(MetaClass type, Statement statement) {
    if (MetaClassFactory.get(void.class).equals(type))
      return Stmt.load(null);
    
    return Stmt.invokeStatic(type.asBoxed(), "valueOf", statement);
  }
}