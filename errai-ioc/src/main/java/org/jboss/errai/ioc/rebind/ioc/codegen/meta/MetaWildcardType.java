package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MetaWildcardType extends MetaType {
    public MetaType[] getLowerBounds();

    public MetaType[] getUpperBounds();
}
