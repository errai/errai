package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaGenericDeclaration;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionGenericDeclaration implements MetaGenericDeclaration {
    private GenericDeclaration genericDeclaration;

    public JavaReflectionGenericDeclaration(GenericDeclaration genericDeclaration) {
        this.genericDeclaration = genericDeclaration;
    }

    public MetaTypeVariable[] getTypeParameters() {
        List<MetaTypeVariable> metaTypeVariableList = new ArrayList<MetaTypeVariable>();

        for (TypeVariable<?> typeVariable : genericDeclaration.getTypeParameters()) {
            metaTypeVariableList.add(new JavaReflectionTypeVariable(typeVariable));
        }

        return metaTypeVariableList.toArray(new MetaTypeVariable[metaTypeVariableList.size()]);
    }
}
