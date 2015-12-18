/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server;

import org.jboss.errai.common.client.util.TimeUnit;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.marshalling.server.ServerMarshalling;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class MarshallingPerformanceTest {

  private static final int TEST_ITERATIONS = 100000;

  @Test
  public void testMarshall() throws IOException {
    // ensure the marshalling system has been setup.
    MappingContextSingleton.get();

    Map payload = null;
    long time = System.nanoTime();
    long encTime = 0;
    long decTime = 0;
    for (int i = 0; i < TEST_ITERATIONS; i++) {
      long eTime = System.nanoTime();
      StringWriter writer = new StringWriter();

      Map enc = new HashMap();
      enc.put("CommandType", "ConnectToQueue");
      enc.put("ToSubject", "ServerBus");
      enc.put("Extra", "Hello There!");

      ServerMarshalling.toJSON(writer, enc);
      writer.flush();
      encTime += (System.nanoTime() - eTime);

      long dTime = System.nanoTime();
      payload = (Map) ServerMarshalling.fromJSON(writer.toString());
      decTime += (System.nanoTime() - dTime);
    }
    time = System.nanoTime() - time;

    long millis = TimeUnit.NANOSECONDS.toMillis(time);
    double throughputPerSecond = (double) TEST_ITERATIONS / ((double) millis / 1000d);

    long encodingMillis = TimeUnit.NANOSECONDS.toMillis(encTime);
    long decodingMillis = TimeUnit.NANOSECONDS.toMillis(decTime);

    double encodingPctTm = ((double) encodingMillis / (double) millis) * 100;
    double decodingPctTm = ((double) decodingMillis / (double) millis) * 100;

    System.out.println("Finished: " + TEST_ITERATIONS + " iterations in : " + TimeUnit.NANOSECONDS.toMillis(time));
    System.out.println("           Round-trip Throughput: " + throughputPerSecond + " per sec.");
    System.out.println("           Encoding Time: " + encodingMillis + "ms (" + encodingPctTm + "%)");
    System.out.println("           Decoding Time: " + decodingMillis + "ms (" + decodingPctTm + "%)");

    Assert.assertNotNull(payload);
    Assert.assertFalse(payload.isEmpty());
    Assert.assertEquals("ConnectToQueue", payload.get("CommandType"));
    Assert.assertEquals("ServerBus", payload.get("ToSubject"));
    Assert.assertEquals("Hello There!", payload.get("Extra"));
  }
}
