package org.jboss.errai.enterprise.rebind;

import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Stmt;

/**
 * Generates the required {@link Statement}s for type marshalling.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class TypeMarshaller {

  public static Statement marshal(Parameter parm) {
    return marshal(parm.getType(), Variable.get(parm.getName()));
  }
  
  public static Statement marshal(MetaClass type, Statement statement) {
    Statement marshallingStatement;
    if (type.asUnboxed().isPrimitive() || type.equals(MetaClassFactory.get(String.class))) {
      marshallingStatement =  PrimitiveTypeMarshaller.marshal(type, statement);
    }
    else {
      //TODO invoke Errai default marshaller
      marshallingStatement = Stmt.newObject(String.class);
    }
    return marshallingStatement;
  }
  
  public static Statement demarshal(Parameter parm) {
    return demarshal(parm.getType(), Variable.get(parm.getName()));
  }
  
  public static Statement demarshal(MetaClass type, Statement statement) {
    Statement demarshallingStatement;
    if (type.asUnboxed().isPrimitive() || type.equals(MetaClassFactory.get(String.class))) {
      demarshallingStatement =  PrimitiveTypeMarshaller.demarshal(type, statement);
    }
    else {
      //TODO invoke Errai default demarshaller
      demarshallingStatement = Stmt.newObject(type);
    }
    return demarshallingStatement;
  }
  
  /**
   * Marshaller for primitive types. 
   * 
   * Works for all types that have a copy constructor, a toString() 
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