package org.jboss.errai.security;

import com.google.gwt.core.client.GWT;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.gwt.RunAsGwtClient;
import org.jboss.arquillian.gwt.client.ArquillianGwtTestCase;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.errai.security.client.SecurityTestModule;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author edewit@redhat.com
 */
@RunWith(Arquillian.class)
public class IntegrationTest extends ArquillianGwtTestCase {

  @Deployment
  public static WebArchive createDeployment() {
    return ShrinkWrap.create(WebArchive.class, "test.war")
            .addPackage("org.jboss.errai.security.client")
            .addPackage("org.jboss.errai.security.shared")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
  }

  @Test
  @RunAsGwtClient(moduleName = "org.jboss.errai.security.SecurityIntegrationTest")
  public void testGreetingService() {
    System.out.println("IntegrationTest.testGreetingService");
//    SecurityTestModule module = GWT.create(SecurityTestModule.class);
//    delayTestFinish(5000);
  }
}
