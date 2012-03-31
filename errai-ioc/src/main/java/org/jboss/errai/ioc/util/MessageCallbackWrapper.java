package org.jboss.errai.ioc.util;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;

/**
 * @author Mike Brock
 */
public final class MessageCallbackWrapper {
  private MessageCallbackWrapper() {}

  public static Statement wrap(Statement statement, String messageVarName) {
    return ObjectBuilder.newInstanceOf(MessageCallback.class).extend()
            .publicOverridesMethod("callback", Parameter.of(Message.class, messageVarName, true))
            .append(statement)
            .finish().finish();

  }

  public static Statement wrapMessageCallbackInAsync(Statement statement) {
    return wrap(RunAsyncWrapper.wrap(Stmt.nestedCall(statement).invoke("callback", Refs.get("$_message_$"))), "$_message_$");
  }

}
