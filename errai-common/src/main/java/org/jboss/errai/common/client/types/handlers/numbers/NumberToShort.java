package org.jboss.errai.common.client.types.handlers.numbers;

import org.jboss.errai.common.client.types.TypeHandler;

public class NumberToShort implements TypeHandler<Number, Short> {
    public Short getConverted(Number in) {
        return in.shortValue();
    }
}