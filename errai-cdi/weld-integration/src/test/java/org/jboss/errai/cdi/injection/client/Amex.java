package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * @author Mike Brock
 */
@ApplicationScoped @Named("amex")
public class Amex implements CreditCard {
}
