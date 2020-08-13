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

package org.jboss.errai.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.marshalling.server.ServerMarshalling;

/**
 * Provider for serialization/deserialization of Errai objects.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Provider
@Produces("application/*+json")
@Consumes("application/*+json")
public class ErraiProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

  static {
    MappingContextSingleton.get();
  }

  @Override
  public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return ServerMarshalling.canHandle(type);
  }

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return ServerMarshalling.canHandle(type);
  }

  @Override
  public long getSize(final Object t, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(final Object t, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType,
      final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream) throws IOException,
      WebApplicationException {
    entityStream.write(ServerMarshalling.toJSON(t).getBytes(Charset.forName("UTF-8")));
  }

  @Override
  public Object readFrom(final Class<Object> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType,
      final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream) throws IOException, WebApplicationException {

    if (type.isInterface()) {
      return ServerMarshalling.fromJSON(entityStream);
    }

    return ServerMarshalling.fromJSON(entityStream, type);
  }
}
