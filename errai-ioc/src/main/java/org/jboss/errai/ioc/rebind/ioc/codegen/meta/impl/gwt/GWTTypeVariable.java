package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaGenericDeclaration;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTTypeVariable implements MetaTypeVariable {
    private JTypeParameter typeParameter;

    public GWTTypeVariable(JTypeParameter typeParameter) {
        this.typeParameter = typeParameter;
    }

    public MetaType[] getBounds() {
        return GWTUtil.fromTypeArray(typeParameter.getBounds());
    }

    public MetaGenericDeclaration getGenericDeclaration() {
        if (typeParameter.isGenericType() != null) {
            return new GWTGenericDeclaration(typeParameter.isGenericType());
        }
        return null;
    }

    public String getName() {
        return typeParameter.getName();
    }
}
