package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MetaGenericDeclaration extends MetaType {
    public MetaTypeVariable[] getTypeParameters();
}
