package org.jboss.errai.ioc.rebind.ioc.codegen;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractStatement implements Statement{

    public Scope getScope() {
        return new Scope();
    }
}
