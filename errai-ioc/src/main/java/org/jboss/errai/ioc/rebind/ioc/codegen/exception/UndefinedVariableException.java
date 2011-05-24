package org.jboss.errai.ioc.rebind.ioc.codegen.exception;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class UndefinedVariableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UndefinedVariableException() {
        super();
    }

    public UndefinedVariableException(String msg) {
        super(msg);
    }
}
