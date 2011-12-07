/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc.                              
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
 *
 */

package org.jboss.errai.marshalling.server;

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.common.client.types.UHashMap;
import org.jboss.errai.marshalling.rebind.api.model.Mapping;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.server.marshallers.DefaultDefinitionMarshaller;
import org.mvel2.util.InternalNumber;

import java.io.*;
import java.nio.CharBuffer;
import java.util.*;

import static org.jboss.errai.common.client.protocols.SerializationParts.ENCODED_TYPE;

/**
 * High-performance stream JSON parser. Provides the decoding algorithm to interpret the Errai Wire Protcol,
 * including serializable types.  This parser always assumes the outer payload is a Map. So it probably shouldn't
 * be used as a general parser.
 *
 * @author Mike Brock
 * @since 1.1
 */
public class JSONStreamDecoder {
  private CharBuffer buffer;
  private BufferedReader reader;

  private char carry;

  private int read;
  private boolean initial = true;

  private DecodingSession decodingContext = new DecodingSession(MappingContextSingleton.get());

  public JSONStreamDecoder(InputStream inStream) {
    this.buffer = CharBuffer.allocate(25);
    try {
      this.reader = new BufferedReader(
              new InputStreamReader(inStream, "UTF-8")
      );
    }
    catch (UnsupportedEncodingException e) {
      throw new Error("UTF-8 is not supported by this JVM?", e);
    }
  }

  public static Object decode(InputStream instream) throws IOException {
    return new JSONStreamDecoder(instream).parse();
  }

  public char read() throws IOException {
    if (carry != 0) {
      char c = carry;
      carry = 0;
      return c;
    }
    if (read <= 0) {
      if (!initial) buffer.rewind();
      initial = false;
      if ((read = reader.read(buffer)) <= 0) {
        return 0;
      }
      buffer.rewind();
    }
    read--;
    return buffer.get();
  }

  public Object parse() {
    try {
      return _parse(new Context(), null, false);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Object _parse(Context ctx, Object collection, boolean map) throws IOException {
    char c;
    StringBuilder appender;
    while ((c = read()) != 0) {
      switch (c) {
        case '[':
          ctx.addValue(_parse(new Context(), new ArrayList(), false));
          break;

        case '{':
          ctx.addValue(_parse(new Context(), new UHashMap(), true));
          break;

        case ']':
        case '}':
          if (map && ctx.encodedType) {

            try {
              return TypeDemarshallHelper.demarshallAll(ctx.record(collection), decodingContext);
            }
            catch (Exception e) {
              e.printStackTrace();
              throw new RuntimeException("Could not demarshall object", e);
            }
            finally {
              ctx.encodedType = false;
            }
          }
          else {
            return ctx.record(collection);
          }

        case ',':
          ctx.record(collection);
          break;

        case '"':
        case '\'':
          char term = c;
          appender = new StringBuilder();
          StrCapture:
          while ((c = read()) != 0) {
            switch (c) {
              case '\\':
                appender.append(handleEscapeSequence());
                break;
              case '"':
              case '\'':
                if (c == term) {
                  ctx.addValue(appender.toString());
                  term = 0;
                  break StrCapture;
                }
              default:
                appender.append(c);
            }
          }

          if (term != 0) {
            throw new RuntimeException("unterminated string literal");
          }

          break;

        case ':':
          continue;

        default:
          if (isValidNumberPart(c)) {
            ctx.addValue(parseNumber(c));
            break;
          }
          else if (Character.isJavaIdentifierPart(c)) {
            appender = new StringBuilder().append(c);

            while (((c = read()) != 0) && Character.isJavaIdentifierPart(c)) {
              appender.append(c);
            }

            String s = appender.toString();

            if (s.length() > 4) ctx.addValue(s);
            else if ("null".equals(s)) {
              ctx.addValue(null);
            }
            else if ("true".equals(s)) {
              ctx.addValue(Boolean.TRUE);
            }
            else if ("false".equals(s)) {
              ctx.addValue(Boolean.FALSE);
            }
            else {
              ctx.addValue(s);
            }

            if (c != 0) carry = c;
          }
      }
    }

    return ctx.record(collection);
  }

  public char handleEscapeSequence() throws IOException {
    char c;
    switch (c = read()) {
      case '\\':
        return '\\';
      case 'b':
        return '\b';
      case 'f':
        return '\f';
      case 't':
        return '\t';
      case 'r':
        return '\r';
      case 'n':
        return '\n';
      case '\'':
        return '\'';
      case '"':
        return '\"';
      default:
        throw new RuntimeException("illegal escape sequence: " + c);
    }
  }

  public Number parseNumber(char c) throws IOException {
    double val = 0;
    double dVal = 0;

    double factor = 1;

    char[] buf = new char[21];
    int len = 0;
    do {
      buf[len++] = c;
    }
    while ((c = read()) != 0 && isValidNumberPart(c));

    if (c != 0) {
      carry = c;
    }

    if (len == 1 && buf[0] == '-') return null;

    for (int i = len - 1; i != -1; i--) {
      switch (buf[i]) {
        case '.':
          dVal = val / factor;
          val = 0;
          factor = 1;
          continue;
        case '-':
          if (i != 0) {
            throw new NumberFormatException(new String(buf));
          }
          val = -val;
          break;
        case '1':
          val += factor;
          break;
        case '2':
          val += 2 * factor;
          break;
        case '3':
          val += 3 * factor;
          break;
        case '4':
          val += 4 * factor;
          break;
        case '5':
          val += 5 * factor;
          break;
        case '6':
          val += 6 * factor;
          break;
        case '7':
          val += 7 * factor;
          break;
        case '8':
          val += 8 * factor;
          break;
        case '9':
          val += 9 * factor;
          break;
      }

      factor *= 10;
    }

    return dVal + val;
  }

  private static boolean isValidNumberPart(char c) {
    switch (c) {
      case '.':
      case '-':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        return true;
      default:
        return false;
    }
  }

  private class Context {
    Object lhs;
    Object rhs;
    boolean encodedType = false;
    boolean finished = false;
    boolean noearlyInit = false;
    private Set<String> req;

    private Context() {
    }

    private Object addValue(Object val) {
      if (lhs == null) {
        return lhs = val;
      }
      else {
        return rhs = val;
      }
    }

    private Object getValue() {
      if (rhs != null) {
        return rhs;
      }
      else {
        return lhs;
      }
    }

    private void removeValue() {
      if (rhs != null) {
        rhs = null;
      }
      else {
        lhs = null;
      }
    }

    private boolean canInitialize() {
      return !finished && req != null && req.isEmpty();
    }

    private void markSatisfied(String key) {
      if (req != null && !req.isEmpty()) {
        req.remove(key);
      }
    }

    private void markUnsatisfied(String key) {
      if (req == null) req = new HashSet<String>();
      req.add(key);
    }


    public void setNoearlyInit(boolean noearlyInit) {
      this.noearlyInit = noearlyInit;
    }

    private boolean initReference(Map map) {
      if (canInitialize()) {
        String hash = (String) map.get(SerializationParts.OBJECT_ID);
        if (!decodingContext.hasObjectHash(hash)) {
          map.put(TypeDemarshallHelper.NO_AUTO_WIRE, "true");
          decodingContext.recordObjectHash((String) map.get(SerializationParts.OBJECT_ID),
                  TypeDemarshallHelper.instantiate(map, decodingContext));
          map.remove(TypeDemarshallHelper.NO_AUTO_WIRE);
        }

        return finished = true;
      }
      else {
        return false;
      }
    }

    private Object record(Object collection) {
      try {
        if (lhs != null) {
          if (collection instanceof Map) {
            if (!encodedType) {

              Map oMap = (Map) collection;

              if ((encodedType = ENCODED_TYPE.equals(lhs)) && decodingContext.getMappingContext()
                      .getDefinitionsFactory().hasDefinition((String) rhs)) {
                /**
                 * Handle the special case of immutable marshalling. This declared the keys required for
                 * the instantiating constructor.
                 */

                MappingDefinition definition = decodingContext.getMappingContext()
                        .getDefinitionsFactory().getDefinition((String) rhs);

                /**
                 * For custom marshallers -- excluding the default definition marshaller, do not early init
                 * the references.
                 */
                if (definition.isCachedMarshaller()
                        && !(definition.getMarshallerInstance() instanceof DefaultDefinitionMarshaller)) {
                  noearlyInit = true;
                }
                else {
                  for (Mapping m : definition.getInstantiationMapping().getMappings()) {

                    if (!oMap.containsKey(m.getKey())) {
                      markUnsatisfied(m.getKey());
                    }
                  }

                  if (!oMap.containsKey(SerializationParts.OBJECT_ID)) {
                    markUnsatisfied(SerializationParts.OBJECT_ID);
                  }

                  initReference(oMap);
                }

                ((Map) collection).put(lhs, rhs);

                return collection;
              }
            }

            if (lhs instanceof String && lhs.toString().startsWith(SerializationParts.EMBEDDED_JSON)) {
              lhs = JSONDecoder.decode(lhs.toString().substring(SerializationParts.EMBEDDED_JSON.length()));
            }

            ((Map) collection).put(lhs, rhs);

            if (!noearlyInit && encodedType) {
              markSatisfied((String) lhs);
              initReference((Map) collection);
            }
          }
          else {
            if (collection == null) return lhs;

            ((Collection) collection).add(lhs);
          }
        }
        return collection;
      }
      catch (ClassCastException e) {
        throw new RuntimeException("error building collection", e);
      }
      finally {
        lhs = rhs = null;
      }
    }
  }


}
