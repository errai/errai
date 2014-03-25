package org.jboss.errai.forge.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class SerializableSetTest {

  @Test
  public void testSerializeEmpty() {
    final SerializableSet set = new SerializableSet();
    
    assertEquals("", set.serialize());
  }
  
  @Test
  public void testSerializeSingle() throws Exception {
    final SerializableSet set = new SerializableSet();
    set.add("moogah");
    
    assertEquals("moogah", set.serialize());
  }

  @Test
  public void testSerializeMany() throws Exception {
    final SerializableSet set = new SerializableSet();
    final String[] values = new String[] {
            "hello",
            "world",
            "foobar",
            "moogah"
    };
    
    set.addAll(Arrays.asList(values));
    final String serialized = set.serialize();
    
    for (int i = 0; i < values.length; i++) {
      final String testRegex = "^(.*,)?" + values[i] + "(,.*)?$";
      assertTrue(serialized.matches(testRegex));
    }
  }
  
  @Test
  public void testdeserializeEmpty() throws Exception {
    final SerializableSet result = SerializableSet.deserialize("");
    
    assertEquals(0, result.size());
  }
  
  @Test
  public void testdeserializeSingle() throws Exception {
    final SerializableSet result = SerializableSet.deserialize("moogah");
    
    assertEquals(1, result.size());
    assertEquals("moogah", result.iterator().next());
  }
  
  @Test
  public void testdeserializeMany() throws Exception {
    final String[] values = new String[] {
            "hello",
            "world",
            "foobar",
            "moogah"
    };
    String testString = "";
    for (int i = 0; i < values.length - 1; i++) {
      testString += values[i] + ",";
    }
    testString += values[values.length - 1];
    
    final SerializableSet result = SerializableSet.deserialize(testString);
    
    assertEquals(values.length, result.size());
    
    for (int i = 0; i < values.length; i++) {
      assertTrue(result.contains(values[i]));
    }
  }
}
