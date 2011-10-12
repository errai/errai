package org.jboss.errai.common.client.types;

import com.google.gwt.json.client.JSONValue;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MarshallerProvider {
  public boolean hasMarshaller(String fqcn);
  public <T> T demarshall(String fqcn, JSONValue o);
  public String marshall(String fqcn, Object o);
}
