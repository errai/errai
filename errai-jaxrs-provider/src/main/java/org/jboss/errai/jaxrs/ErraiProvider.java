package org.jboss.errai.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jboss.errai.bus.server.io.JSONStreamDecoder;
import org.jboss.errai.bus.server.io.JSONStreamEncoder;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Provider for serialization/deserialization of Errai objects.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Provider
@Produces("application/json")
@Consumes("application/json")
public class ErraiProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.isAnnotationPresent(Portable.class) || Collection.class.isAssignableFrom(type);
  }
  
  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.isAnnotationPresent(Portable.class) || Collection.class.isAssignableFrom(type);
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
  
    return JSONStreamDecoder.decode(entityStream);
  }
}