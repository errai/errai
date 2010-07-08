package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import org.mvel2.util.StringAppender;

public class InjectionTask {
    protected final TaskType injectType;
    protected final Injector injector;

    protected JField field;
    protected JMethod method;
    protected JClassType type;

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

    public InjectionTask(Injector injector, JClassType type) {
        this.injectType = TaskType.Type;
        this.injector = injector;
        this.type = type;
    }

    public String doTask(InjectionContext ctx) {
        StringAppender appender = new StringAppender();
        switch (injectType) {
            case Field:
                appender.append(injector.getVarName()).append('.').append(field.getName()).append(" = ")
                        .append(ctx.getInjector(field.getType().isClassOrInterface()).getType(ctx))
                        .append(";\n");
                break;

            case Method:
                appender.append(injector.getVarName()).append('.')
                        .append(method.getName()).append('(');

                String[] vars = InjectUtil.resolveInjectionDependencies(method.getParameters(), ctx);

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
