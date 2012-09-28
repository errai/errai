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

package org.jboss.errai.bus.client.json;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndListener;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.marshalling.client.MarshallingSessionProviderFactory;
import org.jboss.errai.marshalling.client.api.json.impl.gwt.GWTJSON;
import org.jboss.errai.marshalling.client.marshallers.ErraiProtocolEnvelopeNoAutoMarshaller;
import org.jboss.errai.marshalling.client.protocols.ErraiProtocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONUtilCli {
  private static boolean autoDemarshall = true;

  public static List<MarshalledMessage> decodePayload(final String value) {
    if (value == null || value.trim().length() == 0) return Collections.emptyList();

    /**
     * We have to do a two-stage decoding of the message.  We cannot fully decode the message here, as we
     * cannot be sure the destination endpoint exists within this Errai bundle.  So we extract the ToSubject
     * field and send the un-parsed JSON object onwards.
     *
     */
    JSONValue val;

    try {
      val = JSONParser.parseStrict(value);
    }
    catch (ClassCastException e) {
      if (!GWT.isProdMode()) {
        System.out.println("*** working around devmode bug ***");
        val = JSONParser.parseStrict(value);
      }
      else {
        val = null;
      }
    }

    if (val == null) {
      return Collections.emptyList();
    }
    final JSONArray arr = val.isArray();
    if (arr == null) {
      throw new RuntimeException("unrecognized payload" + val.toString());
    }
    final ArrayList<MarshalledMessage> list = new ArrayList<MarshalledMessage>();
    unwrap(list, arr);
    return list;

  }

  private static void unwrap(final List<MarshalledMessage> messages, final JSONArray val) {
    for (int i = 0; i < val.size(); i++) {
      final JSONValue v = val.get(i);
      if (v.isArray() != null) {
        unwrap(messages, v.isArray());
      }
      else {
        messages.add(new MarshalledMessageImpl((JSONObject) v));
      }
    }
  }

  public static class MarshalledMessageImpl implements MarshalledMessage {
    public final JSONObject o;

    public MarshalledMessageImpl(JSONObject o) {
      this.o = o;
    }

    public Object getMessage() {
      return o;
    }

    public String getSubject() {
      return o.get("ToSubject").isString().stringValue();
    }
  }

  @SuppressWarnings({"unchecked"})
  public static Map<String, Object> decodePayload(final Object value) {
    if (value == null) {
      return null;
    }
    if (!(value instanceof JSONObject)) {
      throw new RuntimeException("bad payload: " + value);
    }

    if (autoDemarshall) {
      return ErraiProtocol.decodePayload(GWTJSON.wrap((JSONObject) value));
    }
    else {
      nativeLog("using no-auto envelope demarshaller");
      return ErraiProtocolEnvelopeNoAutoMarshaller.INSTANCE.demarshall(
              GWTJSON.wrap((JSONObject) value), MarshallingSessionProviderFactory.getEncoding());
    }
  }

  private static final QueueSession clientSession = new QueueSession() {
    private Map<String, Object> attributes = new HashMap<String, Object>();

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
    public void addSessionEndListener(SessionEndListener listener) {
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


  public static Message decodeCommandMessage(final Object value) {
    return CommandMessage.createWithParts(decodePayload(value))
        .setResource(RequestDispatcher.class.getName(), requestDispatcherProvider)
        .setResource("Session", clientSession);
  }

  public static void setAutoDemarshall(boolean autoDemarshall1) {
    autoDemarshall = autoDemarshall1;
  }

  private static native void nativeLog(String message) /*-{
    window.console.log(message);
  }-*/;

}
