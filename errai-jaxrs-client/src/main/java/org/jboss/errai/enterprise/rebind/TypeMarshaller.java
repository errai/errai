package org.jboss.errai.enterprise.rebind;

import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Stmt;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class TypeMarshaller {

  public static Statement marshal(Parameter parm) {
    return marshal(parm.getType(), parm.getName());
  }
  
  public static Statement marshal(MetaClass type, String varName) {
    Statement marshallingStatement;
    if (type.asUnboxed().isPrimitive() || type.equals(MetaClassFactory.get(String.class))) {
      marshallingStatement =  PrimitiveTypeMarshaller.marshal(type, varName);
    }
    else {
      //TODO invoke Errai default marshaller
      marshallingStatement = Stmt.newObject(String.class);
    }
    return marshallingStatement;
  }
  
  public static Statement demarshal(MetaClass type, String varName) {
    return demarshal(type, Variable.get(varName));
  }
  
  public static Statement demarshal(Parameter parm) {
    return demarshal(parm.getType(), parm.getName());
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
}