package org.jboss.errai.bus.server.json;

import java.util.Map;

public class TypeDemarshallHelper {
    public Object demarshall(String object) {
        Map<String, Object> obj = (Map<String,Object>) new JSONDecoder(object).parse();
        String clsName = (String) obj.get("__EncodedType");
        return null;
    }

}
