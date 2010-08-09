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
import org.mvel2.util.StringAppender;

public class InjectionTask {
    protected final TaskType injectType;
    protected final Injector injector;

    protected JConstructor constructor;
    protected JField field;
    protected JMethod method;
    protected JClassType type;
    protected JParameter parm;


    public InjectionTask(Injector injector, JField field) {
        this.injectType = TaskType.Field;
        this.injector = injector;
        this.field = field;
    }

    public InjectionTask(Injector injector, JMethod method) {
        this.injectType = TaskType.Method;
        this.injector = injector;
        this.method = method;
    }

    public InjectionTask(Injector injector, JParameter parm) {
        this.injectType = TaskType.Method;
        this.injector = injector;
        this.parm = parm;
    }

    public InjectionTask(Injector injector, JClassType type) {
        this.injectType = TaskType.Type;
        this.injector = injector;
        this.type = type;
    }

    public String doTask(InjectionContext ctx) {
        StringAppender appender = new StringAppender();
        InjectionPoint injectionPoint
                = new InjectionPoint(null, injectType, constructor, method, field, type, parm, injector, ctx);
        switch (injectType) {
            case Field:

                appender.append(injector.getVarName()).append('.').append(field.getName()).append(" = ")
                        .append(ctx.getInjector(field.getType().isClassOrInterface()).getType(ctx, injectionPoint))
                        .append(";\n");
                break;

            case Method:
                appender.append(injector.getVarName()).append('.')
                        .append(method.getName()).append('(');

                String[] vars = InjectUtil.resolveInjectionDependencies(method.getParameters(), ctx, injectionPoint);

                appender.append(InjectUtil.commaDelimitedList(vars)).append(");\n");
                break;
        }

        return appender.toString();
    }

    public TaskType getInjectType() {
        return injectType;
    }

    public Injector getInjector() {
        return injector;
    }

    public JField getField() {
        return field;
    }

    public JMethod getMethod() {
        return method;
    }

    public void setMethod(JMethod method) {
        if (this.method == null)
            this.method = method;
    }

    public void setField(JField field) {
        if (this.field == null)
            this.field = field;
    }
}
