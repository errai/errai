/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.forge.config;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
