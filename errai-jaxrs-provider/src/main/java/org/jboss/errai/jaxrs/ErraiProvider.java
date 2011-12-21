/*
 * Copyright 2011 JBoss, a division of Red Hat, Inc
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

package org.jboss.errai.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jboss.errai.marshalling.server.DecodingSession;
import org.jboss.errai.marshalling.server.JSONStreamDecoder;
import org.jboss.errai.marshalling.server.JSONStreamEncoder;
import org.jboss.errai.marshalling.server.MappingContextSingleton;

/**
 * Provider for serialization/deserialization of Errai objects.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Provider
@Produces("application/*+json")
@Consumes("application/*+json")
public class ErraiProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return MappingContextSingleton.get().getDefinitionsFactory().hasDefinition(type);
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return MappingContextSingleton.get().getDefinitionsFactory().hasDefinition(type);
  }

  @Override
  public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
      WebApplicationException {

    JSONStreamEncoder.encode(t, entityStream);
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {

    return MappingContextSingleton.get().getDefinitionsFactory().getDefinition(type).getMarshallerInstance()
        .demarshall(JSONStreamDecoder.decode(entityStream), new DecodingSession(MappingContextSingleton.get()));
  }
}