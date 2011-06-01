package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JWildcardType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaWildcardType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTWildcardType implements MetaWildcardType {
    private JWildcardType wildcardType;

    public GWTWildcardType(JWildcardType wildcardType) {
        this.wildcardType = wildcardType;
    }

    public MetaType[] getLowerBounds() {
        return GWTUtil.fromTypeArray(wildcardType.getLowerBounds());
    }

    public MetaType[] getUpperBounds() {
        return GWTUtil.fromTypeArray(wildcardType.getUpperBounds());
    }
}
