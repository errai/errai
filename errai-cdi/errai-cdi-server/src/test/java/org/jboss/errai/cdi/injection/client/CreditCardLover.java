package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class CreditCardLover {

  @Inject @Named("visa") CreditCard visaCard;
  @Inject @Named("amex") CreditCard amexCard;

  public CreditCard getVisaCard() {
    return visaCard;
  }

  public CreditCard getAmexCard() {
    return amexCard;
  }
}
