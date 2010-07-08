package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;

import java.lang.annotation.Annotation;

public class DecoratorContext<T extends Annotation> {
    private T annotation;
    private TaskType taskType;
    private JMethod method;
    private JField field;
    private JClassType type;
    private Injector injector;
    private InjectionContext injectionContext;

    public DecoratorContext(T annotation, TaskType taskType, JMethod method,
                            JField field, JClassType type, Injector injector, InjectionContext injectionContext) {
        this.annotation = annotation;
        this.taskType = taskType;
        this.method = method;
        this.field = field;
        this.type = type;
        this.injector = injector;
        this.injectionContext = injectionContext;
    }

    public T getAnnotation() {
        return annotation;
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

    public Injector getInjector() {
        return injector;
    }

    public InjectionContext getInjectionContext() {
        return injectionContext;
    }

    public String getValueExpression() {
        switch (taskType) {
            case Field:
                return injector.getVarName() + "." + field.getName();

            case Method:
                return injector.getVarName() + "." + method.getName() + "()";

            case Type:
                return injector.getVarName();

            default:
                return null;
        }
    }

    public String getMemberName() {
        switch (taskType) {
            case Field:
                return field.getName();

            case Method:
                return method.getName();

            case Type:
                return type.getName();

            default:
                return null;
        }
    }
}
