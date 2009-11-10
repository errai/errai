package org.jboss.errai.bus.client.types.handlers.numbers;

import org.jboss.errai.bus.client.types.TypeHandler;

public class NumberToShort implements TypeHandler<Number, Short> {
    public Short getConverted(Number in) {
        return in.shortValue();
    }
}