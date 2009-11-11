package org.jboss.errai.common.client.types.handlers.numbers;

import org.jboss.errai.common.client.types.TypeHandler;

public class NumberToByte implements TypeHandler<Number, Byte> {
    public Byte getConverted(Number in) {
        return in.byteValue();
    }
}