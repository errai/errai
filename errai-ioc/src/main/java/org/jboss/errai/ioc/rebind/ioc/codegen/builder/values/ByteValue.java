package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ByteValue extends LiteralValue<Byte> {

    public ByteValue(int value) {
        super(new Integer(value).byteValue());
    }

    @Override
    public String getCanonicalString() {
        return String.valueOf(getValue().intValue());
    }
}
