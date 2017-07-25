package org.jboss.errai.enterprise.jaxrs.client.test;

import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.CustomMarshallerService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.CustomMarshallerServicePojo;

public class CustomMarshallersIntegrationTest extends AbstractErraiJaxrsTest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  public void testDemarshallingWithCustomMarshallers() {
    CustomMarshallerServicePojo pojo = new CustomMarshallerServicePojo("foo", "bar");

    call(CustomMarshallerService.class,
            new SimpleAssertionCallback<>("@POST using custom marshaller failed", pojo.getFoo())).post(pojo);
  }

  public void testMarshallingWithCustomMarshallers() {
    CustomMarshallerServicePojo pojo = new CustomMarshallerServicePojo("foo", "bar");

    call(CustomMarshallerService.class,
            new SimpleAssertionCallback<>("@GET using custom marshaller failed", pojo)).get();
  }
}
