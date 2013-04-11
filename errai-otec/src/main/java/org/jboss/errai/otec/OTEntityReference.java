/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.otec;

/**
 * @author Mike Brock
 */
public final class OTEntityReference {
  private final RefType refType;
  private final Object reference;

  private OTEntityReference(RefType refType, Object reference) {
    this.refType = refType;
    this.reference = reference;
  }

  public static OTEntityReference createFromReference(Object reference) {
    return new OTEntityReference(RefType.OBJECT_REF, reference);
  }

  public static OTEntityReference createFromId(Integer id) {
    return new OTEntityReference(RefType.ENTITY_ID, id);
  }

  public RefType getRefType() {
    return refType;
  }

  public Object getReference() {
    return reference;
  }

  public String toString() {
    switch (refType) {
      case ENTITY_ID:
        return "entityId=" + reference;
      case OBJECT_REF:
        return "objectReference=" + reference;
    }
    return null;
  }

  public static enum RefType {
    OBJECT_REF, ENTITY_ID
  }
}
