package org.jboss.errai.ioc.client.api.builtin;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Consumer;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.common.client.protocols.MessageParts;

final class ErraiMessageConsumer implements Consumer<Object> {
  private RequestDispatcher requestDispatcher = ErraiBus.getDispatcher();
  private final String toSubject;
  private final String replyTo;

  private ErraiMessageConsumer(String toSubject, String replyTo) {
    this.toSubject = toSubject;
    this.replyTo = replyTo;
  }

  public static ErraiMessageConsumer of(String toSubject, String replyTo) {
    return new ErraiMessageConsumer(toSubject, replyTo);
  }

  @Override
  public void consume(Object value) {
    if (replyTo != null) {
      MessageBuilder.createMessage()
          .toSubject(toSubject)
          .with(MessageParts.ReplyTo, replyTo)
          .with(MessageParts.Value, value)
          .done().sendNowWith(requestDispatcher);
    }
    else {
      MessageBuilder.createMessage()
          .toSubject(toSubject)
          .with(MessageParts.Value, value)
          .done().sendNowWith(requestDispatcher);
    }
  }

  public void setRequestDispatcher(RequestDispatcher dispatcher) {
    if (requestDispatcher != null) throw new IllegalStateException("requestDispatcher is already set");
    this.requestDispatcher = dispatcher;
  }

  @Override
  public void setToSubject(String subjectName) {
    if (this.toSubject != null) throw new IllegalStateException("toSubject is already set");
    this.toSubject = subjectName;
  }

  @Override
  public void setReplyTo(String subjectName) {
    if (this.replyTo != null) throw new IllegalStateException("replyTo is already set");
    this.replyTo = subjectName;
  }
}