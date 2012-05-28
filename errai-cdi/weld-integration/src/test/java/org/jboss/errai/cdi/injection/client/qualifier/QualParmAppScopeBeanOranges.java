package org.jboss.errai.cdi.injection.client.qualifier;

import org.jboss.errai.cdi.injection.client.CommonInterfaceB;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Mike Brock
 */
@ApplicationScoped @QualV(value = QualEnum.ORANGES, amount = 50)
public class QualParmAppScopeBeanOranges implements CommonInterfaceB {
}
