package org.jboss.errai.bus.client.types.handlers.numbers;

import org.jboss.errai.bus.client.types.TypeHandler;

import java.util.Date;

public class NumbertToDate implements TypeHandler<Number, Date> {
    public Date getConverted(Number in) {
        return new Date(in.longValue());
    }
}