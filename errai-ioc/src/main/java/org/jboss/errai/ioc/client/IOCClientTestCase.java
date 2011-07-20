package org.jboss.errai.ioc.client;

import com.google.gwt.junit.client.GWTTestCase;
import org.jboss.errai.ioc.rebind.MockIOCGenerator;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class IOCClientTestCase extends GWTTestCase {


  protected IOCClientTestCase() {
    setForcePureJava(true);
  }

  protected InterfaceInjectionContext bootstrapContainer() {
    try {
      return new MockIOCGenerator().generate().newInstance().bootstrapContainer();
    }
    catch (Exception e) {
      throw new RuntimeException("failed to run in emulated mode", e);
    }
  }

}
