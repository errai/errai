package org.jboss.errai.cdi.async.test.bm.client.res;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * @author Mike Brock
 */
@ApplicationScoped @Named("visa")
public class Visa implements CreditCard {
}
