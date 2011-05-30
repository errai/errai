package org.jboss.errai.ioc.rebind.ioc.codegen.exception;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class TypeNotIterableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TypeNotIterableException() {
        super();
    }

    public TypeNotIterableException(String msg) {
        super(msg);
    }
}
