package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JGenericType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaGenericArrayType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTGenericArrayType implements MetaGenericArrayType {
    private JGenericType genericType;

    public GWTGenericArrayType(JGenericType genericType) {
        this.genericType = genericType;
    }

    public MetaType getGenericComponentType() {
        return GWTUtil.fromType(genericType.getErasedType());
    }
}
