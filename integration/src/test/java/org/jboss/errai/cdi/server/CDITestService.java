package org.jboss.errai.cdi.server;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class CDITestService {

	public void handleNewSubscription(@Observes String test) {
		System.out.println(test);
	}
}
