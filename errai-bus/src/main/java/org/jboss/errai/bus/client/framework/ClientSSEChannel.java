/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.client.framework;

/**
 * @author Mike Brock
 */
public class ClientSSEChannel {
  public static native Object attemptSSEChannel(final ClientMessageBusImpl bus, final String sseAddress) /*-{
      var sseSource;
      if (!!window.EventSource) {
          sseSource = new EventSource(sseAddress);
          sseSource.addEventListener('message', function (e) {
              bus.@org.jboss.errai.bus.client.framework.ClientMessageBusImpl::procPayload(Ljava/lang/String;)(e.data);
          }, false);

          return socket;
      } else {
          return "NotSupportedByBrowser";
      }
  }-*/;
}
