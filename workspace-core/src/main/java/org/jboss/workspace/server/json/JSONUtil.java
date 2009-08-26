package org.jboss.workspace.server.json;

import org.jboss.workspace.client.rpc.CommandMessage;

import java.util.Map;

public class JSONUtil {
    public static Map<String, Object> decodeToMap(String in) {
        return (Map<String, Object>) new JSONDecoder(in).parse();
    }
   
}
