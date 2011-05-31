package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class VariableReference implements Statement {
    public abstract String getName();
    public abstract Statement getValue();

    public MetaClass getType() {
        return null;
    }

    public Context getContext() {
        return null;
    }
    
    public String generate() {
        return getName();
    }
}