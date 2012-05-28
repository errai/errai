package org.jboss.errai.cdi.injection.client;

import com.google.inject.Inject;
import org.jboss.errai.cdi.injection.client.qualifier.QualEnum;
import org.jboss.errai.cdi.injection.client.qualifier.QualV;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class QaulParamDependentBeanOranges {
  @Inject @QualV(QualEnum.ORANGES) private CommonInterfaceB commonInterfaceB;

  public CommonInterfaceB getCommonInterfaceB() {
    return commonInterfaceB;
  }
}
