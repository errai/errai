package org.jboss.errai.ioc.rebind.ioc.codegen.exception;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvalidTypeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidTypeException() {
        super();
    }

    public InvalidTypeException(Throwable t) {
        super(t);
    }
    
    public InvalidTypeException(String msg) {
        super(msg);
    }
}
