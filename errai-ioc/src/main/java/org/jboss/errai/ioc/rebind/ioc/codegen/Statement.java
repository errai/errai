package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com> 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface Statement {
    public String generate();
    public MetaClass getType();
    public Context getContext();
}
