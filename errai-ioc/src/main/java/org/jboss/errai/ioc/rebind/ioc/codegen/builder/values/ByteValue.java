package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ByteValue extends LiteralValue<Byte> {

    public ByteValue(Byte value) {
        super(value);
    }

    public ByteValue(Integer value) {
        super(value.byteValue());
    }

    @Override
    public String getCanonicalString() {
        return String.valueOf(getValue().intValue());
    }
}
