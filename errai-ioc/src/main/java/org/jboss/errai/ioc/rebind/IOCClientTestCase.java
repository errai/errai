package org.jboss.errai.ioc.rebind;

import com.google.gwt.junit.client.GWTTestCase;
import org.jboss.errai.ioc.client.InterfaceInjectionContext;
import org.jboss.errai.ioc.rebind.MockIOCGenerator;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class IOCClientTestCase extends GWTTestCase {
  private String packageFilter;

  protected IOCClientTestCase() {
    setForcePureJava(true);
    packageFilter = getModuleName().substring(0, getModuleName().lastIndexOf('.'));
  }

  protected InterfaceInjectionContext bootstrapContainer() {
    try {
      MockIOCGenerator mockIOCGenerator = new MockIOCGenerator();
      mockIOCGenerator.setPackageFilter(packageFilter);
      return mockIOCGenerator.generate().newInstance().bootstrapContainer();
    }
    catch (Exception e) {
      throw new RuntimeException("failed to run in emulated mode", e);
    }
  }

  public void setPackageFilter(String packageFilter) {
    this.packageFilter = packageFilter;
  }
}
