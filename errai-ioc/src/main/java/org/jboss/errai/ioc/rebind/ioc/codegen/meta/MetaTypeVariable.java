package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MetaTypeVariable extends MetaType {
    public MetaType[] getBounds();

    public MetaGenericDeclaration getGenericDeclaration();

    public String getName();
}
