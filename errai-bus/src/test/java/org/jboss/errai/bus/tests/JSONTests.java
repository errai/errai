package org.jboss.errai.bus.tests;

import junit.framework.TestCase;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.JSONMessage;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.tests.support.RandomProvider;
import org.jboss.errai.bus.client.tests.support.SType;
import org.jboss.errai.bus.server.io.JSONDecoder;
import org.jboss.errai.bus.server.io.JSONEncoder;
import org.jboss.errai.bus.server.io.JSONStreamDecoder;
import org.jboss.errai.common.client.protocols.SerializationParts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    inputParts.put("Num", 123l);

    Message msg = MessageBuilder.createMessage().getMessage();

    for (Map.Entry<String, Object> entry : inputParts.entrySet()) {
      msg.set(entry.getKey(), entry.getValue());
    }

    String encodedJSON = JSONEncoder.encode(msg.getParts());
    Map<String, Object> decoded = (Map<String, Object>) JSONDecoder.decode(encodedJSON);
    assertEquals(inputParts, decoded);
  }

  public void testStreamDecoder() {
    MessageBuilder.setMessageProvider(JSONMessage.PROVIDER);

    Map<String, Object> inputParts = new HashMap<String, Object>();
    inputParts.put("ToSubject", "Foo");
    inputParts.put("Message", "\"Hello, World\"");
    inputParts.put("Sentence", "He said he was \"okay\"!");
    inputParts.put("TestUnterminatedThings", "\" { [ ( ");
    inputParts.put("Num", 123l);

    Message msg = MessageBuilder.createMessage().getMessage();

    for (Map.Entry<String, Object> entry : inputParts.entrySet()) {
      msg.set(entry.getKey(), entry.getValue());
    }

    try {
      String encodedJSON = JSONEncoder.encode(msg.getParts());
      System.out.println(">" + encodedJSON);

      ByteArrayInputStream instream = new ByteArrayInputStream(encodedJSON.getBytes());

      Map<String, Object> decoded = (Map<String, Object>) JSONStreamDecoder.decode(instream);
      assertEquals(inputParts, decoded);

    } catch (IOException e) {
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

    System.out.println(jsonData);

    try {
      ByteArrayInputStream instream = new ByteArrayInputStream(jsonData.getBytes());
      Map<String, Object> decoded = (Map<String, Object>) JSONStreamDecoder.decode(instream);
      TType sType1 = (TType) decoded.get("SType");
      assertTrue(sType.equals(sType1));

    } catch (IOException e) {
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


    System.out.println("rSType:" + rSType);

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


  public static class TType {
    private String fieldOne;
    private String fieldTwo;
    private Date startDate;
    private Date endDate;
    private Boolean active;

    public String getFieldOne() {
      return fieldOne;
    }

    public void setFieldOne(String fieldOne) {
      this.fieldOne = fieldOne;
    }

    public String getFieldTwo() {
      return fieldTwo;
    }

    public void setFieldTwo(String fieldTwo) {
      this.fieldTwo = fieldTwo;
    }

    public Date getStartDate() {
      return startDate;
    }

    public void setStartDate(Date startDate) {
      this.startDate = startDate;
    }

    public Date getEndDate() {
      return endDate;
    }

    public void setEndDate(Date endDate) {
      this.endDate = endDate;
    }

    public Boolean getActive() {
      return active;
    }

    public void setActive(Boolean active) {
      this.active = active;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      TType sType = (TType) o;

      if (active != null ? !active.equals(sType.active) : sType.active != null) return false;
      if (endDate != null ? !endDate.equals(sType.endDate) : sType.endDate != null) return false;
      if (fieldOne != null ? !fieldOne.equals(sType.fieldOne) : sType.fieldOne != null) return false;
      if (fieldTwo != null ? !fieldTwo.equals(sType.fieldTwo) : sType.fieldTwo != null) return false;
      if (startDate != null ? !startDate.equals(sType.startDate) : sType.startDate != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = fieldOne != null ? fieldOne.hashCode() : 0;
      result = 31 * result + (fieldTwo != null ? fieldTwo.hashCode() : 0);
      result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
      result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
      result = 31 * result + (active != null ? active.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return fieldOne + "|" + fieldTwo + "|" + startDate + "|" + endDate + "|" + active;
    }
  }


}
