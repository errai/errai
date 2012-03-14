package org.jboss.errai.jpa.test.client;


import com.google.gwt.junit.client.GWTTestCase;

public class ErraiJpaTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.test.JpaTest";
  }

  public void testEntityManagerInjection() throws Exception {
    JpaTestClient testClient = JpaTestClient.INSTANCE;
    assertNotNull(testClient);
    assertNotNull(testClient.entityManager);
  }
}
