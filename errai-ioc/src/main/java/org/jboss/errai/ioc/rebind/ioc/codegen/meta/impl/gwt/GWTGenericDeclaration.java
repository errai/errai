package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaGenericDeclaration;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTGenericDeclaration implements MetaGenericDeclaration {
    private JGenericType genericType;

    public GWTGenericDeclaration(JGenericType genericType) {
        this.genericType = genericType;
    }

    public MetaTypeVariable[] getTypeParameters() {
        List<MetaTypeVariable> typeVariables = new ArrayList<MetaTypeVariable>();

        for (JTypeParameter typeParameter : genericType.getTypeParameters()) {
            typeVariables.add(new GWTTypeVariable(typeParameter));
        }

        return typeVariables.toArray(new MetaTypeVariable[typeVariables.size()]);
    }
}
