package org.jboss.errai.common.client.types.handlers.numbers;

import org.jboss.errai.common.client.types.TypeHandler;

public class NumberToInt implements TypeHandler<Number, Integer> {
    public Integer getConverted(Number in) {
        return in.intValue();
    }
}
