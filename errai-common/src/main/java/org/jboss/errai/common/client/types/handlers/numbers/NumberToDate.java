package org.jboss.errai.common.client.types.handlers.numbers;

import org.jboss.errai.common.client.types.TypeHandler;

import java.util.Date;

public class NumberToDate implements TypeHandler<Number, Date> {
    public Date getConverted(Number in) {
        return new Date(in.longValue());
    }
}