package org.jboss.errai.ioc.rebind.ioc.codegen.exception;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class UndefinedConstructorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private MetaClass type;
    private MetaClass[] parameterTypes;
    
    public UndefinedConstructorException() {
        super();
    }

    public UndefinedConstructorException(String msg) {
        super(msg);
    }
    
    public UndefinedConstructorException(MetaClass type, MetaClass... parameterTypes) {
        this.type = type;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        
        buf.append(super.toString()).append(": class:").append(type.getFullyQualifedName()).append(" parameterTypes:");
        for(MetaClass type : parameterTypes) {
            buf.append(type.getFullyQualifedName()).append(" ");
        }
        return buf.toString();
    }
}
