package org.jboss.errai.ioc.rebind.ioc.codegen;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * This class represents a variable.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Variable extends AbstractStatement {
    private String name;
    private MetaClass type;

    private Variable(String name, MetaClass type) {
        this.name = name;
        this.type = type;
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

    public String getName() {
        return name;
    }

    public MetaClass getType() {
        return type;
    }

    private String hashString;

    private String hashString() {
        if (hashString == null) {
            hashString = "Variable:"+name + ":" + type.getFullyQualifedName();
        }
        return hashString;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Variable && 
            hashString().equals("Variable:"+name + ":" + ((Variable) o).type.getFullyQualifedName());
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
        return name;
    }
}
