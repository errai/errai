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

package org.jboss.errai.codegen.meta;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class MetaParameter extends AbstractHasAnnotations {
  public abstract String getName();

  public abstract MetaClass getType();

  public abstract MetaClassMember getDeclaringMember();

  private String _hashString;

  public String hashString() {
    if (_hashString != null) return _hashString;
    return _hashString = MetaParameter.class.getName() + ":" + getName() + ":"
            + getType().getFullyQualifiedName();
  }
  
  public int hashCode() {
    return hashString().hashCode() * 31;
  }
  
  public boolean equals(Object o) {
    return o instanceof  MetaParameter && ((MetaParameter) o).hashString().equals(hashString());
  }
}
