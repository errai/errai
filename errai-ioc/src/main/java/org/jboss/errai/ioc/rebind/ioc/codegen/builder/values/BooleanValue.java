package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BooleanValue extends LiteralValue<Boolean> {

    public BooleanValue(boolean value) {
        super(value);
    }

    @Override
    public String getCanonicalString() {
        return getValue().toString();
    }
}
