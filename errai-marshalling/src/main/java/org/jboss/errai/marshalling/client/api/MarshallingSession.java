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

package org.jboss.errai.marshalling.client.api;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MarshallingSession {
  public MappingContext getMappingContext();
  
  public Marshaller<Object> getMarshallerInstance(String fqcn);
  
  public String determineTypeFor(String formatType, Object o);
  
  public String getAssumedElementType();
  
  public void setAssumedElementType(String assumedElementType);

  /**
   * Records a new object to the session with the specified <tt>hashCode</tt> identifier.
   *
   * @param hashCode a unique identifier
   * @param instance the instance of the entity.
   */
  public void recordObjectHash(String hashCode, Object instance);

  /**
   * Checks if the object is already in the context based on the object reference.
   *
   * @param reference the entity reference
   * @return true if the session contains the object reference.
   */
  public boolean hasObjectHash(Object reference);

  /**
   * Checks if the object is already in the context based on the hash code.
   *
   * @param hashCode the hash code
   * @return true if the session contains the object reference.
   */
  public boolean hasObjectHash(String hashCode);

  /**
   * Returns a unique identifier for the specified object reference. Returns a new identifier if the object is unknown
   * to the session, or returns the existing one if it is known.
   *
   * @param reference the entity reference
   * @return a new or existing identifier within this session
   */
  public String getObjectHash(Object reference);

  /**
   * Looks up the object based on the specified <tt>hashCode</tt> identifier. Returns null if the specified identifier
   * does not exist.
   *
   * @param type the type of entity being looked up
   * @param hashCode the identifier of the entity within the session
   * @param <T>  the type of entity being looked up
   * @return the instance of the entity or null if not present
   */
  public <T> T getObject(Class<T> type, String hashCode);
}
