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
package org.jboss.errai.enterprise.client.cdi;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;

import javax.enterprise.inject.Any;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public abstract class AbstractCDIEventCallback<T> implements MessageCallback {
  /**
   * The qualifiers a message must contain to be propagated.
   */
  protected final Set<String> qualifierSet = new HashSet<String>();

  public Set<String> getQualifiers() {
    return qualifierSet;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final void callback(final Message message) {
    final Set<String> msgQualifiers = message.get(Set.class, CDIProtocol.Qualifiers);

    if (msgQualifiers == null && !qualifierSet.isEmpty()) {
      // no match.
    }
    else if (msgQualifiers == null
        // if the event was fired from the client, then we apply a containsAll operation rather than an equals() operation.
        // this is because Weld on the server takes care of identifying the proper observer methods we have registered
        // to invoke, resulting in potential redundant event propagation. Thus, we only check for exact matches from
        // the service.

        // TODO: CDI 1.1 allows EventObservers to know what qualifiers were associated with firing the event
        //       a future version of Errai should be able to use containsAll() from the server as well, when
        //       Errai switches to CDI 1.1.
        || (message.hasPart(CDIProtocol.FromClient) ? msgQualifiers.containsAll(qualifierSet) : msgQualifiers.equals(qualifierSet))
        || qualifierSet.contains(Any.class.getName())) {

      GWT.runAsync(new RunAsyncCallback() {
        public void onFailure(final Throwable throwable) {
          throw new RuntimeException("failed to run asynchronously", throwable);
        }

        public void onSuccess() {
          fireEvent((T) message.get(Object.class, CDIProtocol.BeanReference));
        }
      });
    }
  }

  protected abstract void fireEvent(T event);
}
