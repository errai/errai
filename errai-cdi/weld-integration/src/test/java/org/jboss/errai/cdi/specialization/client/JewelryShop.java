package org.jboss.errai.cdi.specialization.client;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;

@Dependent
public class JewelryShop extends Shop {
    @Override
    @Produces
    @Specializes
    @Sparkly
    public Necklace getExpensiveGift() {
        return new Necklace(10);
    }
}