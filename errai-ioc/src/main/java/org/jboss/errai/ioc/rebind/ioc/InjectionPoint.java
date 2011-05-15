/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.*;
import org.mvel2.util.ReflectionUtil;

import javax.management.RuntimeErrorException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.jboss.errai.ioc.rebind.ioc.InjectUtil.getPrivateFieldInjectorName;

public class InjectionPoint<T extends Annotation> {
    private T annotation;
    private TaskType taskType;

    private JConstructor constructor;
    private JMethod method;
    private JField field;
    private JClassType type;
    private JParameter parm;
    private Injector injector;
    private InjectionContext injectionContext;

    public InjectionPoint(T annotation, TaskType taskType, JConstructor constructor, JMethod method,
                          JField field, JClassType type, JParameter parm, Injector injector, InjectionContext injectionContext) {
        this.annotation = annotation;
        this.taskType = taskType;
        this.constructor = constructor;
        this.method = method;
        this.field = field;
        this.type = type;
        this.parm = parm;
        this.injector = injector;
        this.injectionContext = injectionContext;
    }

    public T getAnnotation() {
        return annotation;
    }

    public JConstructor getConstructor() {
        return constructor;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public JMethod getMethod() {
        return method;
    }

    public JField getField() {
        return field;
    }

    public JClassType getType() {
        return type;
    }

    public JParameter getParm() {
        return parm;
    }

    public Injector getInjector() {
        return injector;
    }

    public InjectionContext getInjectionContext() {
        return injectionContext;
    }

    public void ensureFieldExposed() {
        if (!injectionContext.getPrivateFieldsToExpose().contains(field)) {
            injectionContext.getPrivateFieldsToExpose().add(field);
        }
    }

    public String getValueExpression() {
        switch (taskType) {
            case PrivateField:
                return getPrivateFieldInjectorName(field) + "(" + injector.getVarName() + ")";

            case Field:
                return injector.getVarName() + "." + field.getName();

            case Method:
                return injector.getVarName() + "." + method.getName() + "()";

            case Parameter:
            case Type:
                return injector.getVarName();

            default:
                return null;
        }
    }

    public String getMemberName() {
        switch (taskType) {
            case PrivateField:
                return getPrivateFieldInjectorName(field) + "(" + injector.getVarName() + ")";

            case Field:
                return field.getName();

            case Parameter:
            case Method:
                return method.getName();

            case Type:
                return type.getName();

            default:
                return null;
        }
    }

    public Annotation[] getAnnotations() {
        switch (taskType) {
            case PrivateField:
            case Field:
                return InjectUtil.getAnnotations(field);

            case Parameter:
                JMethod jMethod = parm.getEnclosingMethod().isMethod();

                int index = -1;
                for (int i = 0; i < jMethod.getParameters().length; i++) {
                    if (jMethod.getParameters()[i] == parm) {
                        index = i;
                    }
                }

                Method meth = InjectUtil.loadMethod(jMethod);

                if (method == null) return null;

                return meth.getParameterAnnotations()[index];
            case Method:
                return InjectUtil.loadMethod(method).getAnnotations();

            case Type:
                return InjectUtil.loadClass(type.getQualifiedSourceName()).getAnnotations();

            default:
                return null;
        }
    }

    public Annotation[] getAnnotations(Field field) {
        return field == null ? null : field.getAnnotations();
    }


}
