package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DoubleValue extends LiteralValue<Double> {

    public DoubleValue(double value) {
        super(value);
    }

    @Override
    public String getCanonicalString() {
        return getValue().toString() + "d";
    }
}
