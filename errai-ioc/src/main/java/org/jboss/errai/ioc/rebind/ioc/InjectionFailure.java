package org.jboss.errai.ioc.rebind.ioc;

import org.jboss.errai.bus.server.ErraiBootstrapFailure;

public class InjectionFailure extends ErraiBootstrapFailure {
    public InjectionFailure(String message) {
        super(message);
    }

    public InjectionFailure(String message, Throwable cause) {
        super(message, cause);
    }
}
