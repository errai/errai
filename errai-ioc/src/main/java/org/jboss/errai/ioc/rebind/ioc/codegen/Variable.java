package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

import javax.enterprise.util.TypeLiteral;

/**
 * This class represents a variable.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Variable extends AbstractStatement {
    private String name;
    private MetaClass type;
    private Statement initialization;

    private Variable(String name, MetaClass type) {
        this.name = name;
        this.type = type;
    }

    public void initialize(Statement initialization) {
        this.initialization = initialization;
    }
    
    public static Variable get(String name, Class type) {
        return new Variable(name, MetaClassFactory.get(type));
    }

    public static Variable get(String name, TypeLiteral type) {
        return new Variable(name, MetaClassFactory.get(type));
    }

    public static Variable get(String name, MetaClass type) {
        return new Variable(name, type);
    }

    public static VariableReference get(final String name) {
        return new VariableReference() {
            public String getName() {
                return name;
            }

            public String generate() {
                return name;
            }

            public MetaClass getType() {
                return null;
            }

            public Context getContext() {
                return null;
            }
        };
    }

    public VariableReference getReference() {
        return new VariableReference() {
            public String getName() {
                return name;
            }

            public String generate() {
                return name;
            }

            public MetaClass getType() {
                return type;
            }

            public Context getContext() {
                return null;
            }
        };
    }
    
    public String getName() {
        return name;
    }

    public MetaClass getType() {
        return type;
    }

    private String hashString;

    private String hashString() {
        if (hashString == null) {
            hashString = "Variable:" + name + ":" + type.getFullyQualifedName();
        }
        return hashString;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Variable &&
                hashString().equals("Variable:" + name + ":" + ((Variable) o).type.getFullyQualifedName());
    }

    @Override
    public int hashCode() {
        return hashString().hashCode();
    }

    @Override
    public String toString() {
        return "Variable [name=" + name + ", type=" + type + "]";
    }

    public String generate() {
        StringBuilder buf = new StringBuilder();

        MetaClass inferredType = (initialization!=null)?initialization.getType():null;
        if (type==null) {
            if (inferredType==null) {
                throw new InvalidTypeException("No type and no initialization specified to infer the type.");
            } else {
                type = initialization.getType();
            }
        } 
        // use mvel instead and try to convert types
        GenUtil.assertAssignableTypes(inferredType, type);
        
        buf.append(type.getFullyQualifedName()).append(" ").append(name);
        if (initialization != null)
            buf.append(" = ").append(initialization.generate());
        buf.append(";");

        return buf.toString();
    }
}
