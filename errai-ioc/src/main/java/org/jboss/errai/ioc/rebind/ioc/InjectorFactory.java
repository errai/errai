package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.rebind.ProcessingContext;
import org.jboss.errai.bus.server.annotations.Service;

import java.lang.annotation.Annotation;


public class InjectorFactory {
    private final InjectionContext ctx;

    public InjectorFactory(ProcessingContext ctx) {
        this.ctx = new InjectionContext(ctx);

        /**
         * Create a decorator that is capable of registering services on injected classes.
         */
        this.ctx.registerDecorator(new Decorator<Service>(Service.class) {
            @Override
            public String generateDecorator(Service annotation, TaskType taskType, JMethod method, JField field,
                                            JClassType type, Injector injector, InjectionContext ctx) {
                String inj = ctx.getInjector(ctx.getProcessingContext().loadClassType(MessageBus.class)).getType(ctx);
                String expr = null;
                String svcName = annotation.value();
                switch (taskType) {
                    case Field:
                        if ("".equals(svcName)) {
                            svcName = field.getName();
                        }

                        expr = injector.getVarName() + "." + field.getName();

                        break;

                    case Method:
                        if ("".equals(svcName)) {
                            svcName = field.getName();
                        }
                        expr = injector.getVarName() + "." + method.getName() + "()";
                        break;

                    case Type:
                        if ("".equals(svcName)) {
                            svcName = type.getName();
                        }
                        expr = injector.getVarName();

                        break;
                }

                return inj + ".subscribe(\"" + svcName + "\", " + expr + ");";
            }
        });
    }

    public String generate(JClassType type) {
        return ctx.getInjector(type).getType(ctx);
    }

    public void addType(JClassType type) {
        ctx.registerInjector(new TypeInjector(type));
    }

    public void addInjector(Injector injector) {
        ctx.registerInjector(injector);
    }
}
