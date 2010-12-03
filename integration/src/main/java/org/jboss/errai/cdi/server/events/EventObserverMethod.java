package org.jboss.errai.cdi.server.events;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.cdi.client.CDICommands;
import org.jboss.errai.cdi.client.CDIProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Filip Rogaczewski
 */
public class EventObserverMethod implements ObserverMethod {

	private static final Logger log = LoggerFactory.getLogger(EventObserverMethod.class);

	private Type type;
	private MessageBus bus;

	public EventObserverMethod(Type type, MessageBus bus) {
		this.type = type;
		this.bus = bus;
	}

	public Class<?> getBeanClass() {
		return EventObserverMethod.class;
	}

	public Type getObservedType() {
		return type;
	}

	public Set<Annotation> getObservedQualifiers() {
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        return qualifiers;
	}

	public Reception getReception() {
		return Reception.ALWAYS;
	}

	public TransactionPhase getTransactionPhase() {
		return null; 
	}

	public void notify(Object event) {
        MessageBuilder.createMessage()
                .toSubject("cdi.event:"+event.getClass().getName())
                .command(CDICommands.CDI_EVENT)
                .with(CDIProtocol.TYPE, event.getClass().getName())
                .with(CDIProtocol.OBJECT_REF, event)
                .noErrorHandling().sendNowWith(bus);
	}
}
