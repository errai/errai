package org.jboss.errai.bus.client.types.handlers.numbers;

import org.jboss.errai.bus.client.types.TypeHandler;

public class NumberToLong implements TypeHandler<Number, Long> {
    public Long getConverted(Number in) {
        return in.longValue();
    }
}