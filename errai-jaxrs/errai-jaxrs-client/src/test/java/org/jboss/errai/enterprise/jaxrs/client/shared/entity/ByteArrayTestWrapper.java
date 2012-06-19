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

package org.jboss.errai.enterprise.jaxrs.client.shared.entity;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class ByteArrayTestWrapper {

  private List<Byte> bytes = new ArrayList<Byte>();

  public ByteArrayTestWrapper() {
    bytes.add(new Byte("1"));
    bytes.add(new Byte("2"));
  }
  
  public List<Byte> getBytes() {
    return bytes;
  }

  public void setBytes(List<Byte> bytes) {
    this.bytes = bytes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bytes == null) ? 0 : bytes.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ByteArrayTestWrapper other = (ByteArrayTestWrapper) obj;
    if (bytes == null) {
      if (other.bytes != null)
        return false;
    }
    else if (!bytes.equals(other.bytes))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ByteArrayTestWrapper [bytes=" + bytes + "]";
  }
  
  
}
