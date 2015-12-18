package org.jboss.errai.cdi.injection.client;

import com.google.inject.Inject;
import org.jboss.errai.cdi.injection.client.qualifier.QualEnum;
import org.jboss.errai.cdi.injection.client.qualifier.QualV;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class QaulParamDependentBeanApples {
  @Inject @QualV(QualEnum.APPLES) private CommonInterfaceB commonInterfaceB;

  public CommonInterfaceB getCommonInterfaceB() {
    return commonInterfaceB;
  }
}
