package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ClassLiteral extends LiteralValue<Class<?>> {
    public ClassLiteral(Class<?> value) {
        super(value);
    }

    @Override
    public String getCanonicalString() {
        return getValue().getCanonicalName() + ".class";
    }
}
