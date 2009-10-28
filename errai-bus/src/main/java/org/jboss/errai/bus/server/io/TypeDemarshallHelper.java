package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.CommandMessage;
import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;
import org.mvel2.MVEL;

import javax.swing.text.html.HTMLDocument;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
                command.set(t, _demarshallAll(command.get(Object.class, t)));
            }
        }
        catch (Exception e) {
            throw new RuntimeException("could not demarshall types", e);
        }
    }


    public static Object _demarshallAll(Object o) throws Exception {
        if (o instanceof String) {
            Object v;
            if ((v = new JSONDecoder((String) o).parse()) instanceof String ) {
                return o;
            }
            else {
                return _demarshallAll(v);
            }

        } else if (o instanceof Collection) {
            ArrayList newList = new ArrayList(((Collection)o).size());
            for (Object o2 : ((Collection) o)) {
                newList.add(_demarshallAll(o2));
            }
            return newList;
        } else if (o instanceof Map) {
            Map<?, ?> oMap = (Map) o;
            if (oMap.containsKey("__EncodedType")) {
                Object newInstance =  Class.forName((String) oMap.get("__EncodedType")).newInstance();

                for (Map.Entry<?, ?> entry : oMap.entrySet()) {
                    if ("__EncodedType".equals(entry.getKey())) continue;
                    MVEL.setProperty(newInstance, (String) entry.getKey(), _demarshallAll(entry.getValue()));
                }

                return newInstance;
            }
        }
        return o;
    }

    public static void main(String[] args) {
        System.out.println("a|b|c".split("\\|")[0]);
    }
}
