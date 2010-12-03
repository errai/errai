package org.jboss.errai.cdi.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.cdi.client.CDIProtocol;
import org.jboss.errai.cdi.server.events.EventObserverMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author: Filip Rogaczewski
 */
@ApplicationScoped
public class EventSubscriptionListener implements SubscribeListener {

	private static final Logger log = LoggerFactory.getLogger(EventSubscriptionListener.class);
	
	private MessageBus bus;
	private AfterBeanDiscovery abd;

	public EventSubscriptionListener(AfterBeanDiscovery abd, MessageBus bus) {
		this.abd = abd;
		this.bus = bus;
	}

	public void onSubscribe(SubscriptionEvent event) {
		try {
			if (event.getSubject().contains("cdi.event:")
					&& !event.getSubject().equals(EventDispatcher.NAME)) {
				final String className = event.getSubject().substring("cdi.event:".length());
				abd.addObserverMethod(new EventObserverMethod(this.getClass().getClassLoader().loadClass(className), bus));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
