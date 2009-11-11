package org.jboss.errai.common.client.types.handlers.numbers;

import org.jboss.errai.common.client.types.TypeHandler;

public class NumberToDouble implements TypeHandler<Number, Double> {
    public Double getConverted(Number in) {
        return in.doubleValue();
    }
}