package org.jboss.errai.ioc.rebind.decorators.builtin;

import com.google.gwt.core.ext.typeinfo.JField;
import org.jboss.errai.bus.client.api.annotations.ToSubject;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.Decorator;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;

/**
 * @author Mike Brock .
 */
@CodeDecorator
public class ToSubjectDecorator extends Decorator<ToSubject> {
    public ToSubjectDecorator(Class<ToSubject> decoratesWith) {
        super(decoratesWith);
    }

    @Override
    public String generateDecorator(InjectionPoint<ToSubject> injectionPoint) {
        final InjectionContext ctx = injectionPoint.getInjectionContext();

        final JField field = injectionPoint.getField();
        final ToSubject context = field.getAnnotation(ToSubject.class);

        return injectionPoint.getValueExpression() + ".setToSubject(\"" + context.value() + "\");";
    }
}
