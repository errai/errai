package org.jboss.errai.cdi.specialization.client;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Dependent
public class Shop {

    @Produces
    @Expensive
    @Named
    public Necklace getExpensiveGift() {
        return new Necklace(5);
    }

}