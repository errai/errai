package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JParameter;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.AbstractMetaMember;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTConstructor extends AbstractMetaMember implements MetaConstructor {
    private JConstructor constructor;
    private MetaClass declaringClass;
    private Annotation[] annotations;

    public GWTConstructor(JConstructor c) {
        this.constructor = c;
        this.declaringClass = new GWTClass(c.getEnclosingType());

        try {
            Class<?> cls = Class.forName(c.getEnclosingType().getQualifiedSourceName(), false,
                    Thread.currentThread().getContextClassLoader());

            Constructor constr = cls.getConstructor(InjectUtil.jParmToClass(c.getParameters()));

            annotations = constr.getAnnotations();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public MetaParameter[] getParameters() {
        List<MetaParameter> parameterList = new ArrayList<MetaParameter>();

        for (JParameter jParameter : constructor.getParameters()) {
            parameterList.add(new GWTParameter(jParameter, this));
        }

        return parameterList.toArray(new MetaParameter[parameterList.size()]);
    }

    public MetaClass getDeclaringClass() {
        return declaringClass;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }


}
