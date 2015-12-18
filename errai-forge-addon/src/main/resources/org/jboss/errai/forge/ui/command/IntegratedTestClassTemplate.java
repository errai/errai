package $$_testClassPackage_$$;

import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;

public class $$_testClassSimpleName_$$ extends AbstractErraiCDITest {

  /*
   * Use the lookup method to get dependency injected beans.
   */
  private SyncBeanManager beanManager;

  @Override
  public String getModuleName() {
    return "$$_moduleLogicalName_$$";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    beanManager = IOC.getBeanManager();
  }

  /*
   * This demonstrates how to write a test that runs after an Errai app has
   * initialized. Tests that use the messaing or CDI should use this method.
   */
  public void testAsyncExample() throws Exception {
    /*
     * This sets the test into async mode. The test will fail with an error if
     * not ended in 30 seconds.
     */
    delayTestFinish(30000);
 
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        // Test logic goes here.
        fail("Not yet implemented.");
      }
    });
  }
}
