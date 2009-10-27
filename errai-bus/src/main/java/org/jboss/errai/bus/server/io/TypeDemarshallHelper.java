package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.CommandMessage;
import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;
import org.mvel2.MVEL;

import java.util.Map;

public class TypeDemarshallHelper {
    static {
        DataConversion.addConversionHandler(java.sql.Date.class, new ConversionHandler() {
            public Object convertFrom(Object o) {
                return new java.sql.Date(((Number) o).longValue());
            }

            public boolean canConvertFrom(Class aClass) {
                return Number.class.isAssignableFrom(aClass);
            }
        });

        DataConversion.addConversionHandler(java.util.Date.class, new ConversionHandler() {
            public Object convertFrom(Object o) {
                return new java.util.Date(((Number) o).longValue());
            }

            public boolean canConvertFrom(Class aClass) {
                return Number.class.isAssignableFrom(aClass);
            }
        });

    }

    public static void demarshallAll(String object, CommandMessage command) {
        try {
            for (String t : object.split(",")) {
                String[] pair = t.split("\\|");

                @SuppressWarnings({"unchecked"})
                Map<String, Object> obj = (Map<String, Object>) new JSONDecoder(command.get(String.class, pair[0])).parse();
                Object newInstance = Class.forName(pair[1]).newInstance();

                for (Map.Entry<String, Object> entry : obj.entrySet()) {
                    if ("__EncodedType".equals(entry.getKey())) continue;

                    MVEL.setProperty(newInstance, entry.getKey(), entry.getValue());
                }
                command.set(pair[0], newInstance);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("could not demarshall types", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("a|b|c".split("\\|")[0]);
    }
}
