package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractStatement implements Statement {

    public Scope getScope() {
        return new Scope();
    }
    
    public MetaClass getType() {
        return MetaClassFactory.get(void.class);
    }
}
