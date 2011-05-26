package org.jboss.errai.ioc.rebind.ioc.codegen.exception;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class OutOfScopeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OutOfScopeException() {
        super();
    }

    public OutOfScopeException(String msg) {
        super(msg);
    }
}
