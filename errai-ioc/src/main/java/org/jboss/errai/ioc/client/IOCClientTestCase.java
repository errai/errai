package org.jboss.errai.ioc.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import org.jboss.errai.ioc.client.InterfaceInjectionContext;
import org.jboss.errai.ioc.client.api.Bootstrapper;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class IOCClientTestCase extends GWTTestCase {
  private ContainerBootstrapper initializer = new ContainerBootstrapper() {
    @Override
    public InterfaceInjectionContext bootstrap() {
      Bootstrapper bootstrapper = GWT.create(Bootstrapper.class);
      return bootstrapper.bootstrapContainer();
    }
  };

  protected IOCClientTestCase() {
  }

  protected InterfaceInjectionContext bootstrapContainer() {
    return initializer.bootstrap();
  }

  public void setInitializer(ContainerBootstrapper initializer) {
    this.initializer = initializer;
  }

  public String getModulePackage() {
    return getModuleName().substring(0, getModuleName().lastIndexOf('.'));
  }

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
    bootstrapContainer();
  }

  public static interface ContainerBootstrapper {
    public InterfaceInjectionContext bootstrap();
  }
}
