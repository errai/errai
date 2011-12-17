/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.JSONMessage;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.tests.support.RandomProvider;
import org.jboss.errai.bus.client.tests.support.SType;
import org.jboss.errai.bus.client.tests.support.TType;
import org.jboss.errai.bus.client.tests.support.User;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.server.JSONDecoder;
import org.jboss.errai.marshalling.server.JSONEncoder;
import org.jboss.errai.marshalling.server.JSONStreamDecoder;

/**
 * @author Mike Brock
 */
public class JSONTests extends TestCase {
  public void testEncode() {
    MessageBuilder.setMessageProvider(JSONMessage.PROVIDER);

    Map<String, Object> inputParts = new HashMap<String, Object>();
    inputParts.put("ToSubject", "Foo");
    inputParts.put("Message", "\"Hello, World\"");
    inputParts.put("Sentence", "He said he was \"okay\"!");
    inputParts.put("TestUnterminatedThings", "\" { [ ( ");
    inputParts.put("Num", 123d);

    Message msg = MessageBuilder.createMessage().getMessage();

    for (Map.Entry<String, Object> entry : inputParts.entrySet()) {
      msg.set(entry.getKey(), entry.getValue());
    }

    String encodedJSON = JSONEncoder.encode(msg.getParts());

    System.out.println(encodedJSON);

    Map<String, Object> decoded = (Map<String, Object>) JSONDecoder.decode(encodedJSON);
    assertEquals(inputParts, decoded);
  }

  public void testDecoding() {
    MessageBuilder.setMessageProvider(JSONMessage.PROVIDER);

    Map<String, Object> inputParts = new HashMap<String, Object>();
    inputParts.put("ToSubject", "Foo");
    inputParts.put("Message", "\"Hello, World\"");
    inputParts.put("Sentence", "He said he was \"okay\"!");
    inputParts.put("TestUnterminatedThings", "' \" { [ ( ");
    inputParts.put("Num", 123d);

    Message msg = MessageBuilder.createMessage().getMessage();

    for (Map.Entry<String, Object> entry : inputParts.entrySet()) {
      msg.set(entry.getKey(), entry.getValue());
    }

    try {
      String encodedJSON = JSONEncoder.encode(msg.getParts());
      System.out.println(">" + encodedJSON);

      ByteArrayInputStream instream = new ByteArrayInputStream(encodedJSON.getBytes());

      Map<String, Object> decoded = (Map<String, Object>) JSONStreamDecoder.decode(instream);
      Map<String, Object> decoded2 = (Map<String, Object>) JSONDecoder.decode(encodedJSON);

      assertEquals("JSONStreamDecoder did not decode properly", inputParts, decoded);
      assertEquals("JSONDecoder did not decode properly", inputParts, decoded2);

    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void testMarshalling() {
    String jsonData = "{\"SType\":{" + SerializationParts.ENCODED_TYPE + " :\"" + TType.class.getName()
            + "\",startDate:1280250281006,fieldOne:\"One!\",active:true,endDate:1280251281006,fieldTwo:\"Two!!\"}," +
            "\"ReplyTo\":\"ClientReceiver\",\"ToSubject\":\"TestService1\",__MarshalledTypes:\"SType\"}";

    TType sType = new TType();
    sType.setActive(true);
    sType.setFieldOne("One!");
    sType.setFieldTwo("Two!!");
    sType.setStartDate(new Date(1280250281006l));
    sType.setEndDate(new Date(1280251281006l));

    try {
      ByteArrayInputStream instream = new ByteArrayInputStream(jsonData.getBytes());
      Map<String, Object> decoded = (Map<String, Object>) JSONStreamDecoder.decode(instream);
      TType sType1 = (TType) decoded.get("SType");
      assertTrue(sType.equals(sType1));

    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static class JavaRandomProvider implements RandomProvider {
    private static char[] CHARS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    private Random random = new Random(System.nanoTime());

    public boolean nextBoolean() {
      return random.nextBoolean();
    }

    public int nextInt(int upper) {
      return random.nextInt(upper);
    }

    public double nextDouble() {
      return new BigDecimal(random.nextDouble(), MathContext.DECIMAL32).doubleValue();
    }

    public char nextChar() {
      return CHARS[nextInt(1000) % CHARS.length];
    }

    public String randString() {
      StringBuilder builder = new StringBuilder();
      int len = nextInt(25) + 5;
      for (int i = 0; i < len; i++) {
        builder.append(nextChar());
      }
      return builder.toString();
    }
  }


  public void testMarshalling2() {
    SType type = SType.create(new JavaRandomProvider());
    System.out.println("type  :" + type);

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("SType", type);

    String json = JSONEncoder.encode(vars);

    System.out.println("---");
    System.out.println("json:" + json);
    System.out.println("----");

    Map<String, Object> result = (Map<String, Object>) JSONDecoder.decode(json);

    SType rSType = (SType) result.get("SType");

 //   System.out.println("rSType:" + rSType);

    assertEquals(type, rSType);
  }

  public void testMarshalling3() throws IOException {
    SType type = SType.create(new JavaRandomProvider());

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("SType", type);


    String json = JSONEncoder.encode(vars);
    ByteArrayInputStream instream = new ByteArrayInputStream(json.getBytes());


    Map<String, Object> result = (Map<String, Object>) JSONStreamDecoder.decode(instream);

    SType rSType = (SType) result.get("SType");

    System.out.println("type  :" + type);
    System.out.println("rSType:" + rSType);

    assertEquals(type, rSType);
  }

  public void testMarshalling4() {
    final User user = User.create();
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("user", user);

    String json = JSONEncoder.encode(vars);

    Map<String, Object> result = (Map<String, Object>) JSONDecoder.decode(json);

    User userDes = (User) result.get("user");

    assertEquals(user, userDes);
  }
}