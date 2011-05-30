package org.jboss.errai.ioc.rebind.ioc.codegen.exception;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class UndefinedMethodException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String methodName;
    private MetaClass[] parameterTypes;
    
    public UndefinedMethodException() {
        super();
    }

    public UndefinedMethodException(String msg) {
        super(msg);
    }
    
    public UndefinedMethodException(String methodName, MetaClass... parameterTypes) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        
        buf.append(super.toString()).append(": methodName:").append(methodName).append(" parameterTypes:");
        for(MetaClass type : parameterTypes) {
            buf.append(type.getFullyQualifedName()).append(" ");
        }
        return buf.toString();
    }
}
