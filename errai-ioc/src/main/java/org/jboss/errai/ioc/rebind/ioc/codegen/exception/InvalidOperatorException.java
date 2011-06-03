package org.jboss.errai.ioc.rebind.ioc.codegen.exception;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvalidOperatorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidOperatorException() {
        super();
    }

    public InvalidOperatorException(Throwable t) {
        super(t);
    }
    
    public InvalidOperatorException(String msg) {
        super(msg);
    }
}
