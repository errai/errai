package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;


/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LiteralFactory {
    public static LiteralValue<?> getLiteral(Object o) {
        if (o == null) {
            return NullLiteral.INSTANCE;
        }

        if (o instanceof String) {
            return new StringLiteral((String) o);
        } else if (o instanceof Integer) {
            return new IntValue((Integer) o);
        } else if (o instanceof Boolean) {
            return new BooleanValue((Boolean) o);
        } else if (o instanceof Short) {
            return new ShortValue((Short) o);
        } else if (o instanceof Long) {
            return new LongValue((Long) o);
        } else if (o instanceof Double) {
            return new DoubleValue((Double) o);
        } else if (o instanceof Float) {
            return new FloatValue((Float) o);
        } else if (o instanceof Byte) {
            return new ByteValue((Byte) o);
        } else if (o instanceof Class) {
            return new ClassLiteral((Class) o);
        } else if (o.getClass().isArray()) {
            return new ArrayLiteral(o);
        } else {
            throw new IllegalArgumentException("type cannot be converted to a literal: "
                    + o.getClass().getName());
        }
    }

}
