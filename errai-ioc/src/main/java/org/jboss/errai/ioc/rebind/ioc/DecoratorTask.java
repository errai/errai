package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import org.mvel2.util.StringAppender;

import java.lang.annotation.Annotation;

public class DecoratorTask extends InjectionTask {
    private final Decorator[] decorators;

    public DecoratorTask(Injector injector, JClassType type, Decorator[] decs) {
        super(injector, type);
        this.decorators = decs;
    }


    public DecoratorTask(Injector injector, JField field, Decorator[] decs) {
        super(injector, field);
        this.decorators = decs;
    }

    public DecoratorTask(Injector injector, JMethod method, Decorator[] decs) {
        super(injector, method);
        this.decorators = decs;
    }


    @Override
    public String doTask(InjectionContext ctx) {
        StringAppender appender = new StringAppender();
        Annotation anno = null;

        for (Decorator dec : decorators) {
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

           appender.append(dec.generateDecorator(anno, injectType, method, field, type, injector, ctx));
        }
        return appender.toString();
    }
}
