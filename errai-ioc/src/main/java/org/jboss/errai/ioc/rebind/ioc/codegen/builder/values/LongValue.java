package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LongValue extends LiteralValue<Long> {

    public LongValue(long value) {
        super(value);
    }

    @Override
    public String getCanonicalString() {
        return getValue().toString() + "L";
    }
}
