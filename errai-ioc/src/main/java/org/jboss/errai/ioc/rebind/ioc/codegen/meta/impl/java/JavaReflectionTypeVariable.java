package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaGenericDeclaration;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.lang.reflect.TypeVariable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionTypeVariable implements MetaTypeVariable {
    private TypeVariable variable;
    private MetaGenericDeclaration declaration;

    public JavaReflectionTypeVariable(TypeVariable variable) {
        this.variable = variable;
        this.declaration = new JavaReflectionGenericDeclaration(variable.getGenericDeclaration());
    }

    public MetaType[] getBounds() {
        return JavaReflectionUtil.fromTypeArray(variable.getBounds());
    }

    public MetaGenericDeclaration getGenericDeclaration() {
        return declaration;
    }

    public String getName() {
        return variable.getName();
    }
}
