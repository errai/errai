package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.MetaType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTParameterizedType implements MetaParameterizedType {
    private JParameterizedType parameterizedType;

    public GWTParameterizedType(JParameterizedType parameterizedType) {
        this.parameterizedType = parameterizedType;
    }

    public MetaType[] getTypeParameters() {
        List<MetaType> types = new ArrayList<MetaType>();
        for (JClassType parm : parameterizedType.getTypeArgs()) {
            types.add(MetaClassFactory.get(parm));
        }
        return types.toArray(new MetaType[types.size()]);
    }

    public MetaType getOwnerType() {
        return MetaClassFactory.get(parameterizedType.getEnclosingType());
    }

    public MetaType getRawType() {
        return MetaClassFactory.get(parameterizedType.getRawType());
    }
}
