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

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.util.EncDecUtil;

/**
 * Used to wrap marshallers annotated with {@link org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify}
 *
 * @author Mike Brock
 */
public class QualifyingMarshallerWrapper<T> implements Marshaller<T> {
  private Marshaller<T> delegate;

  public QualifyingMarshallerWrapper(Marshaller<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Class<T> getTypeHandled() {
    return delegate.getTypeHandled();
  }

  @Override
  public String getEncodingType() {
    return delegate.getEncodingType();
  }

  @Override
  public T demarshall(EJValue o, MarshallingSession ctx) {
    return delegate.demarshall(o.isObject().get(SerializationParts.QUALIFIED_VALUE), ctx);
  }

  @Override
  public String marshall(T o, MarshallingSession ctx) {
    return EncDecUtil.wrapQualified(o, delegate.marshall(o, ctx), ctx);
  }

  @Override
  public boolean handles(EJValue o) {
    return delegate.handles(o);
  }
}
