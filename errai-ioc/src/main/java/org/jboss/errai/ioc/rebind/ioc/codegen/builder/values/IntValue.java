package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IntValue extends LiteralValue<Integer> {

    public IntValue(Integer value) {
        super(value);
    }

    @Override
    public String getCanonicalString() {
        return getValue().toString();
    }
}
