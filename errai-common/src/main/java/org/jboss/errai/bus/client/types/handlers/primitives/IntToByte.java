package org.jboss.errai.bus.client.types.handlers.primitives;

import org.jboss.errai.bus.client.types.TypeHandler;


public class IntToByte implements TypeHandler<Integer, Byte> {
    public Byte getConverted(Integer in) {
        return in.byteValue();
    }
}
