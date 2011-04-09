package org.jboss.errai.ioc.rebind.decorators.builtin;

import com.google.gwt.core.ext.typeinfo.JField;
import org.jboss.errai.bus.client.api.annotations.ReplyTo;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.Decorator;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;

/**
 * @author Mike Brock .
 */
@CodeDecorator
public class ReplyToDecorator extends Decorator<ReplyTo> {
    public ReplyToDecorator(Class<ReplyTo> decoratesWith) {
        super(decoratesWith);
    }

    @Override
    public String generateDecorator(InjectionPoint<ReplyTo> injectionPoint) {
        final InjectionContext ctx = injectionPoint.getInjectionContext();

        final JField field = injectionPoint.getField();
        final ReplyTo context = field.getAnnotation(ReplyTo.class);

        return injectionPoint.getValueExpression()
                + ".setReplyTo(\"" + context.value() + "\");";
    }
}
