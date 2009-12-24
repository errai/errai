package org.jboss.errai.bus.server;

public class ErraiBootstrapFailure extends RuntimeException {
    public ErraiBootstrapFailure() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ErraiBootstrapFailure(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ErraiBootstrapFailure(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ErraiBootstrapFailure(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
