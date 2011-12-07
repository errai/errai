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

package org.jboss.errai.marshalling.server;

import org.jboss.errai.marshalling.client.api.AbstractMarshallingSession;
import org.jboss.errai.marshalling.client.api.Marshaller;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public abstract class AbstractServerMarshallingSession extends AbstractMarshallingSession {
  private static final Map<String, Marshaller<Object, Object>> cachedMarshallerInstances
          = new HashMap<String, Marshaller<Object, Object>>();

  public Marshaller<Object, Object> getMarshallerInstance(String fqcn) {


    Marshaller<Object, Object> m = cachedMarshallerInstances.get(fqcn);
    if (m == null) {
      Class<? extends Marshaller> cls = getMappingContext().getMarshallerClass(fqcn);
      if (cls != null) {
        try {
          m = cls.newInstance();
        }
        catch (Throwable t) {
          throw new RuntimeException("could not instantiate marshaller class", t);
        }

      }
    }
    return m;
  }
  
  public boolean hasMarshaller(String fqcn) {
    return getMappingContext().hasMarshaller(fqcn);
  }

}
