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

package org.jboss.errai.marshalling.client.marshallers;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJArray;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller @ServerMarshaller
@ImplementationAliases({AbstractSet.class, HashSet.class, LinkedHashSet.class})
public class SetMarshaller extends AbstractCollectionMarshaller<Set> {
  
  private static final HashSet[] EMPTY_ARRAY = new HashSet[0];
  
  @Override
  public Class<Set> getTypeHandled() {
    return Set.class;
  }

  @Override
  public Set[] getEmptyArray() {
    return EMPTY_ARRAY;
  }
  
  @Override
  public Set doDemarshall(EJArray o, MarshallingSession ctx) {
    return marshallToCollection(new HashSet<Object>(o.size()), o, ctx);
  }
}
