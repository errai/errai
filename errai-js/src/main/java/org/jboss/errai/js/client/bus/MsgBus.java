package org.jboss.errai.js.client.bus;

import com.google.gwt.core.client.JavaScriptObject;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.js.client.bus.marshall.MsgTools;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

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
    bus.send(new CommandMessage().toSubject(subject).set(MessageParts.Value, value));
  }

  public void unsubscribeAll(String subject) {
    bus.unsubscribeAll(subject);
  }

  private static final class JsFunctionMessageCallback implements MessageCallback {
    private JavaScriptObject functionReference;

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
