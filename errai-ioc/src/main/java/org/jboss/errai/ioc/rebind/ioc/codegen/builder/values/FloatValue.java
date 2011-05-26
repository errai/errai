package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class FloatValue extends LiteralValue<Float> {

    public FloatValue(Float value) {
        super(value);
    }

    @Override
    public String getCanonicalString() {
        return getValue().toString() + "f";
    }
}
