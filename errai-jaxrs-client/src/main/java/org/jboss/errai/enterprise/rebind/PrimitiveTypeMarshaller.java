package org.jboss.errai.enterprise.rebind;

import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Stmt;

/**
 * Marshaller for primitive types. 
 * 
 * Relies on the existence of a primitive wrapper class for the corresponding type.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PrimitiveTypeMarshaller {

  public static Statement marshal(MetaClass type, String varName) {
    return Stmt.nestedCall(Stmt.newObject(type.asBoxed()).withParameters(Variable.get(varName))).invoke("toString");
  }
  
  public static Statement marshal(Parameter parm) {
    return marshal(parm.getType(), parm.getName());
  }
 
  public static Statement demarshal(MetaClass type, Statement statement) {
    if (MetaClassFactory.get(void.class).equals(type))
      return Stmt.load(null);
    
    return Stmt.invokeStatic(type.asBoxed(), "valueOf", statement);
  }
  
  public static Statement demarshal(MetaClass type, String varName) {
    return demarshal(type, Variable.get(varName));
  }
  
  public static Statement demarshal(Parameter parm) {
    return demarshal(parm.getType(), parm.getName());
  }
}