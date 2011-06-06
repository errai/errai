package org.jboss.errai.ioc.rebind.ioc.codegen.exception;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvalidExpressionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidExpressionException() {
        super();
    }

    public InvalidExpressionException(Throwable t) {
        super(t);
    }
    
    public InvalidExpressionException(String msg) {
        super(msg);
    }
}
