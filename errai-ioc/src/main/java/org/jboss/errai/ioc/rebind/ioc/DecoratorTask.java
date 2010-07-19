package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import org.mvel2.util.StringAppender;

import java.lang.annotation.Annotation;

public class DecoratorTask extends InjectionTask {
    private final Decorator[] decorators;

    public DecoratorTask(Injector injector, JField field, Decorator[] decs) {
        super(injector, field);
        this.decorators = decs;
    }

    public DecoratorTask(Injector injector, JMethod method, Decorator[] decs) {
        super(injector, method);
        this.decorators = decs;
    }

    public DecoratorTask(Injector injector, JParameter parm, Decorator[] decs) {
        super(injector, parm);
        this.decorators = decs;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public String doTask(InjectionContext ctx) {
        StringAppender appender = new StringAppender();
        Annotation anno = null;

        for (Decorator<?> dec : decorators) {
            switch (injectType) {
                case Field:
                    anno = field.getAnnotation(dec.decoratesWith());
                    break;
                case Method:
                    anno = method.getAnnotation(dec.decoratesWith());
                    if (anno == null && field != null) {
                        anno = field.getAnnotation(dec.decoratesWith());
                    }
                    break;
                case Type:
                    anno = type.getAnnotation(dec.decoratesWith());
                    break;
            }

            appender.append(dec.generateDecorator(new DecoratorContext(anno, injectType, method, field, type, parm, injector, ctx)));
        }
        return appender.toString();
    }
}
