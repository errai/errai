package org.jboss.errai.bus.client.types.handlers.numbers;

import org.jboss.errai.bus.client.types.TypeHandler;

public class NumberToByte implements TypeHandler<Number, Byte> {
    public Byte getConverted(Number in) {
        return in.byteValue();
    }
}