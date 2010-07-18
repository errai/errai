package org.jboss.errai.bus.tests;

import junit.framework.TestCase;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.JSONMessage;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.io.JSONDecoder;
import org.jboss.errai.bus.server.io.JSONEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * User: christopherbrock
 * Date: 17-Jul-2010
 * Time: 7:42:27 PM
 */
public class JSONTests extends TestCase {
    public void testEncode() {
        MessageBuilder.setMessageProvider(JSONMessage.PROVIDER);

        Map<String, Object> inputParts = new HashMap<String, Object>();
        inputParts.put("ToSubject", "Foo");
        inputParts.put("Message", "\"Hello, World\"");
        inputParts.put("Sentence", "He said he was \"okay\"!");                                                                                       
        inputParts.put("TestUnterminatedThings", "\" { [ ( ");

        Message msg = MessageBuilder.createMessage().getMessage();

        for (Map.Entry<String, Object> entry : inputParts.entrySet()) {
            msg.set(entry.getKey(), entry.getValue());
        }
        
        String encodedJSON = JSONEncoder.encode(msg.getParts());
        Map<String, Object> decoded = (Map<String, Object>) JSONDecoder.decode(encodedJSON);
        assertEquals(inputParts, decoded);
    }


}
