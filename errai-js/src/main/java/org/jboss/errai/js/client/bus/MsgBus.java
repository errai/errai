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

package org.jboss.errai.js.client.bus;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractCDIEventCallback;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.js.client.bus.marshall.MsgTools;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ExportPackage("errai")
@Export
public class MsgBus implements Exportable {
  private static MessageBus bus = ErraiBus.get();

  public void subscribe(String subject, JavaScriptObject func) {
    bus.subscribe(subject, new JsFunctionMessageCallback(func));
  }
  
  public void subscribeCdi(String subject, JavaScriptObject func) {
    bus.subscribe(CDI.getSubjectNameByType(subject), CDI.ROUTING_CALLBACK);
    CDI.subscribe(subject, new CdiJsFunctionMessageCallback(func));
  }

  public void send(String subject, Object value) {
    bus.send(CommandMessage.createWithParts(MsgTools.jsObjToMap(value)).toSubject(subject));
  }

  public void unsubscribeAll(String subject) {
    bus.unsubscribeAll(subject);
  }

  private static final class JsFunctionMessageCallback implements MessageCallback {
    private final JavaScriptObject functionReference;

    private JsFunctionMessageCallback(JavaScriptObject functionReference) {
      this.functionReference = functionReference;
    }

    @Override
    public void callback(Message message) {
      _callFunction(functionReference, MsgTools.mapToJSPrototype(message.getParts()));
    }
  }
  
  private static final class CdiJsFunctionMessageCallback extends AbstractCDIEventCallback {
    private final JavaScriptObject functionReference;

    private CdiJsFunctionMessageCallback(JavaScriptObject functionReference) {
      // TODO configure qualifiers
      this.functionReference = functionReference;
    }

    @Override
    protected void fireEvent(Object event) {
      _callFunction(functionReference, event);
    }
  }

  private static native void _callFunction(JavaScriptObject obj, Object value) /*-{
    obj(value);
  }-*/;
}
