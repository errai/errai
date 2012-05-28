package org.jboss.errai.cdi.injection.client.qualifier;

import org.jboss.errai.cdi.injection.client.CommonInterfaceB;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Mike Brock
 */
@ApplicationScoped
@QualV(value = QualEnum.APPLES, amount = 10)
public class QualParmAppScopeBeanApples implements CommonInterfaceB {
}
