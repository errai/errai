package org.jboss.errai.bus.client.types.handlers.numbers;

import org.jboss.errai.bus.client.types.TypeHandler;

public class NumberToFloat implements TypeHandler<Number, Float> {
    public Float getConverted(Number in) {
        return in.floatValue();
    }
}