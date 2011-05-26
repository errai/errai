package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ShortValue extends LiteralValue<Short> {

    public ShortValue(Short value) {
        super(value);
    }

    @Override
    public String getCanonicalString() {
        return getValue().toString();
    }
}
