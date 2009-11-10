package org.jboss.errai.bus.client.types.handlers.numbers;

import org.jboss.errai.bus.client.types.TypeHandler;

import java.sql.Date;

public class NumberToSQLDate implements TypeHandler<Number, Date> {
    public Date getConverted(Number in) {
        return new Date(in.longValue());
    }
}
