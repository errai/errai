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

package org.jboss.errai.js.client.bus;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.js.client.bus.marshall.MsgTools;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Mike Brock
 */
@ExportPackage("errai")
@Export
public class MsgBus implements Exportable {
  private static MessageBus bus = ErraiBus.get();

  public void subscribe(String subject, JavaScriptObject func) {
    bus.subscribe(subject, new JsFunctionMessageCallback(func));
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

  private static native void _callFunction(JavaScriptObject obj, Object value) /*-{
    obj(value);
  }-*/;
}
