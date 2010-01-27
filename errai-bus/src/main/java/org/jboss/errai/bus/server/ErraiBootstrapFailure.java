package org.jboss.errai.bus.server;

/**
 * <tt>ErraiBootstrapFailure</tt> extends the <tt>RuntimeException</tt>. It is thrown when configurations and/or
 * initializations of the server fail
 */
public class ErraiBootstrapFailure extends RuntimeException {

    public ErraiBootstrapFailure() {
        super();
    }

    public ErraiBootstrapFailure(String message) {
        super(message);
    }

    public ErraiBootstrapFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public ErraiBootstrapFailure(Throwable cause) {
        super(cause);
    }
}
