package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTUtil {
    public static MetaTypeVariable[] fromTypeVariable(JTypeParameter[] typeParameters) {
        List<MetaTypeVariable> typeVariableList = new ArrayList<MetaTypeVariable>();

        for (JTypeParameter typeVariable : typeParameters) {
            typeVariableList.add(new GWTTypeVariable(typeVariable));
        }

        return typeVariableList.toArray(new MetaTypeVariable[typeVariableList.size()]);
    }


    public static MetaType[] fromTypeArray(JType[] types) {
        List<MetaType> typeList = new ArrayList<MetaType>();

        for (JType t : types) {
            typeList.add(fromType(t));
        }

        return typeList.toArray(new MetaType[types.length]);
    }

    public static MetaType fromType(JType t) {
        if (t.isClassOrInterface() != null) {
            return MetaClassFactory.get(t.isClassOrInterface());
        } else if (t.isTypeParameter() != null) {
            return new GWTTypeVariable(t.isTypeParameter());
        } else if (t.isGenericType() != null) {
            if (t.isArray() != null) {
                return new GWTGenericArrayType(t.isGenericType());
            } else {
                return new GWTGenericDeclaration(t.isGenericType());
            }
        } else if (t.isParameterized() != null) {
            return new GWTParameterizedType(t.isParameterized());
        } else if (t.isWildcard() != null) {
            return new GWTWildcardType(t.isWildcard());
        } else {
            return null;
        }
    }
}
