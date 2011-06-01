package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

import java.lang.reflect.Array;

/**
 * Renders an array back to it's canonical Java-based literal representation, assuming the contents
 * of the array can be represented as such.
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ArrayLiteral extends LiteralValue<Object> {
    private final int dimensions;
    private Class<?> arrayType;

    public ArrayLiteral(Object value) {
        super(value);

        Class type = value.getClass();
        int dim = 0;
        while (type.isArray()) {
            dim++;
            type = type.getComponentType();
        }

        this.dimensions = dim;
        this.arrayType = type;
    }

    @Override
    public String getCanonicalString() {
        StringBuilder buf = new StringBuilder("new " + arrayType.getCanonicalName());

        for (int i = 0; i < dimensions; i++) {
            buf.append("[]");
        }
        buf.append(" ");
        buf.append(renderInlineArrayLiteral(getValue()));

        return buf.toString();
    }

    private static String renderInlineArrayLiteral(Object arrayInstance) {
        StringBuilder builder = new StringBuilder("{");

        int length = Array.getLength(arrayInstance);
        Object element;

        for (int i = 0; i < length; i++) {
            element = Array.get(arrayInstance, i);
            if (element.getClass().isArray()) {
                builder.append(renderInlineArrayLiteral(element));

            } else {
                builder.append(LiteralFactory.getLiteral(element).generate(null));
            }

            if (i + 1 < length) {
                builder.append(", ");
            }
        }

        return builder.append("}").toString();
    }
}
