/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.HasEncoded;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndListener;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.marshalling.client.MarshallingSessionProviderFactory;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.api.json.impl.gwt.GWTJSON;
import org.jboss.errai.marshalling.client.marshallers.ErraiProtocolEnvelopeNoAutoMarshaller;
import org.jboss.errai.marshalling.client.protocols.ErraiProtocol;
import org.slf4j.LoggerFactory;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class BusToolsCli {
  private static boolean autoDemarshall = true;

  public static boolean decodeToCallback(final String jsonString, final ClientMessageBus bus) {
    //LogUtil.log("[bus] RX: " + jsonString);
    final List<Message> messages = decodePayload(jsonString);

    for (final Message message : messages)  {
      bus.sendLocal(message);
    }

    return messages.size() > 0;
  }

  public static List<Message> decodePayload(final String jsonString) {
    if (jsonString == null || jsonString.trim().length() == 0) return Collections.emptyList();

    final JSONValue val = JSONParser.parseStrict(jsonString);

    if (val == null || val.isArray() == null) {
      throw new RuntimeException("illegal payload: must be JSONArray");
    }

    final JSONArray jsonArray = val.isArray();
    final List<Message> messageList = new ArrayList<Message>(jsonArray.size());
    for (int i = 0; i < jsonArray.size(); i++) {
       messageList.add(decodeCommandMessage(GWTJSON.wrap(jsonArray.get(i))));
    }

    return messageList;
  }

  public static String encodeMessage(final Message message) {
    if (message instanceof HasEncoded) {
      return ((HasEncoded) message).getEncoded();
    }
    else {
      return ErraiProtocol.encodePayload(message.getParts());
    }
  }

  public static String encodeMessages(final Collection<Message> messages) {
    final StringBuilder sbuf = new StringBuilder("[");
    boolean first = true;
    for (final Message m : messages) {
      if (!first) {
        sbuf.append(',');
      }
      sbuf.append(encodeMessage(m));
      first = false;
    }
    return sbuf.append("]").toString();
  }

  public static Message decodeCommandMessage(final EJValue value) {
    return CommandMessage.createWithParts(decodePayloadMap(value))
        .setResource(RequestDispatcher.class.getName(), requestDispatcherProvider)
        .setResource("Session", clientSession);
  }

  @SuppressWarnings({"unchecked"})
  private static Map<String, Object> decodePayloadMap(final EJValue value) {
    if (autoDemarshall) {
      return ErraiProtocol.decodePayload(value);
    }
    else {
      LoggerFactory.getLogger(BusToolsCli.class).info("using no-auto envelope demarshaller");
      return ErraiProtocolEnvelopeNoAutoMarshaller.INSTANCE.demarshall(value, MarshallingSessionProviderFactory.getEncoding());
    }
  }

  private static final QueueSession clientSession = new QueueSession() {
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    @Override
    public String getSessionId() {
      return "<InBus>";
    }

    @Override
    public String getParentSessionId() {
      return "<InBus>";
    }

    @Override
    public boolean endSession() {
      return false;
    }

    @Override
    public void setAttribute(final String attribute, final Object value) {
      attributes.put(attribute, value);
    }

    @Override
    public <T> T getAttribute(final Class<T> type, final String attribute) {
      return (T) attributes.get(attribute);
    }

    @Override
    public Collection<String> getAttributeNames() {
      return attributes.keySet();
    }

    @Override
    public boolean hasAttribute(final String attribute) {
      return attributes.containsKey(attribute);
    }

    @Override
    public Object removeAttribute(final String attribute) {
      return attributes.remove(attribute);
    }

    @Override
    public void addSessionEndListener(final SessionEndListener listener) {
    }

    @Override
    public boolean isValid() {
      return true;
    }
  };

  public static QueueSession getClientSession() {
    return clientSession;
  }

  private static final ResourceProvider<RequestDispatcher> requestDispatcherProvider
          = new ResourceProvider<RequestDispatcher>() {
    @Override
    public RequestDispatcher get() {
      return ErraiBus.getDispatcher();
    }
  };


  public static ResourceProvider<RequestDispatcher> getRequestDispatcherProvider() {
    return requestDispatcherProvider;
  }

  public static void setAutoDemarshall(final boolean autoDemarshall1) {
    autoDemarshall = autoDemarshall1;
  }

  /**
   * Sets the application root for the remote message bus endpoints.
   *
   * @param path
   *     path to use when sending requests to the JAX-RS endpoint
   */
  public static native void setApplicationRoot(final String path) /*-{
      if (path == null) {
          $wnd.erraiBusApplicationRoot = undefined;
      }
      else {
          $wnd.erraiBusApplicationRoot = path;
      }
  }-*/;

  /**
   * Returns the application root for the remote message bus endpoints.
   *
   * @return path with trailing slash, or empty string if undefined or
   *         explicitly set to empty
   */
  public static native String getApplicationRoot() /*-{
      //noinspection JSUnresolvedVariable
      if ($wnd.erraiBusApplicationRoot === undefined || $wnd.erraiBusApplicationRoot.length === 0) {
          return "";
      }
      else {
          //noinspection JSUnresolvedVariable
          if ($wnd.erraiBusApplicationRoot.substr(-1) !== "/") {
              //noinspection JSUnresolvedVariable
              return $wnd.erraiBusApplicationRoot + "/";
          }
          //noinspection JSUnresolvedVariable
          return $wnd.erraiBusApplicationRoot;
      }
  }-*/;

  /**
   * Checks whether remote bus communication is enabled.
   * <p/>
   * The JavaScript variable <code>erraiBusRemoteCommunicationEnabled</code> can
   * be used to control this value. If the variable is not present in the window
   * object, the default value <code>true</code> is returned.
   *
   * @return true if remote communication enabled, otherwise false.
   */
  public static native boolean isRemoteCommunicationEnabled() /*-{
      //noinspection JSUnresolvedVariable
      if ($wnd.erraiBusRemoteCommunicationEnabled === undefined || $wnd.erraiBusRemoteCommunicationEnabled.length === 0) {
          return true;
      }
      else {
          //noinspection JSUnresolvedVariable
          return $wnd.erraiBusRemoteCommunicationEnabled;
      }
  }-*/;
}
