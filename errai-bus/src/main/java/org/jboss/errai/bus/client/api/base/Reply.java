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

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.common.client.api.ResourceProvider;

/**
 * @author Mike Brock .
 */
public interface Reply {
  public void setValue(Object value);

  /**
   * Sets a Message part to the specified value.
   *
   * @param part  The <tt>String</tt> name of the message part
   * @param value the value to set the part to
   * @return the updated message
   */
  public void set(String part, Object value);

  /**
   * Sets a Message part to the specified value.
   *
   * @param part  The <tt>Enum</tt> representation of the message part
   * @param value the value to set the part to
   * @return the updated message
   */
  public void set(Enum<?> part, Object value);

  /**
   * @param part
   * @param provider
   * @return
   */
  public void setProvidedPart(String part, ResourceProvider provider);

  /**
   * @param part
   * @param provider
   * @return
   */
  public void setProvidedPart(Enum<?> part, ResourceProvider provider);

  public void reply();
}
