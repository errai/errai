package org.jboss.errai.enterprise.jaxrs.server;

import org.jboss.errai.enterprise.jaxrs.client.shared.CustomMarshallerService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.CustomMarshallerServicePojo;

import java.util.Optional;

public class CustomMarshallerServiceImpl implements CustomMarshallerService {

  @Override
  public String post(CustomMarshallerServicePojo pojo) {
    return pojo.getFoo();
  }

  @Override
  public CustomMarshallerServicePojo get() {
    return new CustomMarshallerServicePojo("foo", "bar");
  }
}
