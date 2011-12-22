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

package org.jboss.errai.bus.server;

import junit.framework.TestCase;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.marshalling.server.ServerMarshalling;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * @author Mike Brock
 */
public class MarshallingTest extends TestCase {

  public void testMarshall() {
    MappingContextSingleton.get();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      OutputStreamWriter writer = new OutputStreamWriter(outputStream);

      try {
        ServerMarshalling.toJSON(writer, "text");
        writer.flush();
      }
      catch (IOException e) {
        e.printStackTrace();
      }

      String text = (String) ServerMarshalling.fromJSON(new String(outputStream.toByteArray()));

       System.out.println("return: '" + text + "'");
  }
}
