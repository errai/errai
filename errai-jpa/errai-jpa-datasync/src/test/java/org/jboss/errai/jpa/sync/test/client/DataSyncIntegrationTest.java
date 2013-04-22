package org.jboss.errai.jpa.sync.test.client;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.gwt.RunAsGwtClient;
import org.jboss.arquillian.gwt.client.ArquillianGwtTestCase;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DataSyncIntegrationTest extends ArquillianGwtTestCase {

  @Deployment
  public static WebArchive createDeployment() {
    return ShrinkWrap.create(WebArchive.class, "data-sync-test.war");
  }

  @Test
  public void testHelloWorldServer() throws Exception {
    System.out.println("This is executed on the server");
  }

  @Test
  @RunAsGwtClient(moduleName="org.jboss.errai.jpa.sync.test.DataSyncTests")
  public void testHelloWorldClient() throws Exception {
    System.out.println("This is executed on the client");
  }
}
